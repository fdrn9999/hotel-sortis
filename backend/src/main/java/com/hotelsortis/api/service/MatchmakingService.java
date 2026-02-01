package com.hotelsortis.api.service;

import com.hotelsortis.api.entity.Player;
import com.hotelsortis.api.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * PvP 매칭 시스템 (Redis 기반)
 *
 * - ELO 기반 매칭 (±150 범위)
 * - 30초마다 범위 +50 확대
 * - 최대 3분 대기
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchmakingService {

    private final RedisTemplate<String, String> redisTemplate;
    private final PlayerRepository playerRepository;

    private static final String MATCHMAKING_QUEUE_KEY = "matchmaking:queue";
    private static final String MATCHMAKING_TIMESTAMP_KEY = "matchmaking:timestamp:";

    private static final int INITIAL_ELO_RANGE = 150;
    private static final int RANGE_EXPANSION_INTERVAL_SECONDS = 30;
    private static final int RANGE_EXPANSION_AMOUNT = 50;
    private static final int MAX_WAIT_TIME_SECONDS = 180; // 3분

    /**
     * Redis Lua Script for atomic find-and-match operation
     *
     * Race condition 방지: 후보 찾기 + 큐에서 제거를 원자적으로 처리
     */
    private static final String FIND_AND_MATCH_SCRIPT =
        "local queue_key = KEYS[1]\n" +
        "local timestamp_key_prefix = KEYS[2]\n" +
        "local player_id = ARGV[1]\n" +
        "local min_elo = tonumber(ARGV[2])\n" +
        "local max_elo = tonumber(ARGV[3])\n" +
        "\n" +
        "-- ELO 범위 내 후보 검색\n" +
        "local candidates = redis.call('ZRANGEBYSCORE', queue_key, min_elo, max_elo)\n" +
        "\n" +
        "-- 자신 제외하고 첫 번째 상대 선택\n" +
        "for i, candidate_id in ipairs(candidates) do\n" +
        "    if candidate_id ~= player_id then\n" +
        "        -- 매칭 확정: 두 플레이어 큐에서 원자적으로 제거\n" +
        "        redis.call('ZREM', queue_key, player_id)\n" +
        "        redis.call('ZREM', queue_key, candidate_id)\n" +
        "        redis.call('DEL', timestamp_key_prefix .. player_id)\n" +
        "        redis.call('DEL', timestamp_key_prefix .. candidate_id)\n" +
        "        \n" +
        "        return candidate_id\n" +
        "    end\n" +
        "end\n" +
        "\n" +
        "return nil";

    /**
     * 매칭 대기열에 플레이어 추가
     */
    public void joinQueue(Long playerId) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        // Redis Sorted Set에 추가 (score = ELO)
        redisTemplate.opsForZSet().add(
            MATCHMAKING_QUEUE_KEY,
            playerId.toString(),
            player.getElo()
        );

        // 대기 시작 시간 저장
        redisTemplate.opsForValue().set(
            MATCHMAKING_TIMESTAMP_KEY + playerId,
            Instant.now().toString(),
            MAX_WAIT_TIME_SECONDS,
            TimeUnit.SECONDS
        );

        log.info("Player {} joined matchmaking queue (ELO: {})", playerId, player.getElo());
    }

    /**
     * 매칭 대기열에서 플레이어 제거
     */
    public void leaveQueue(Long playerId) {
        redisTemplate.opsForZSet().remove(MATCHMAKING_QUEUE_KEY, playerId.toString());
        redisTemplate.delete(MATCHMAKING_TIMESTAMP_KEY + playerId);

        log.info("Player {} left matchmaking queue", playerId);
    }

    /**
     * 상대 찾기 (원자적 연산)
     *
     * Redis Lua Script를 사용하여 후보 찾기 + 큐에서 제거를 원자적으로 처리
     * Race condition 방지: 두 플레이어가 동시에 같은 상대와 매칭되는 것을 방지
     *
     * @param playerId 매칭을 찾을 플레이어 ID
     * @return 매칭된 상대 플레이어 ID (없으면 null)
     */
    public Long findOpponent(Long playerId) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        int playerElo = player.getElo();

        // 대기 시간 계산
        String timestampStr = redisTemplate.opsForValue().get(MATCHMAKING_TIMESTAMP_KEY + playerId);
        if (timestampStr == null) {
            log.warn("Player {} not in queue", playerId);
            return null;
        }

        Instant startTime = Instant.parse(timestampStr);
        long waitSeconds = Duration.between(startTime, Instant.now()).getSeconds();

        // ELO 범위 계산 (30초마다 +50 확대)
        int range = INITIAL_ELO_RANGE;
        if (waitSeconds >= RANGE_EXPANSION_INTERVAL_SECONDS) {
            int expansions = (int) (waitSeconds / RANGE_EXPANSION_INTERVAL_SECONDS);
            range += expansions * RANGE_EXPANSION_AMOUNT;
        }

        // 3분 초과 시 범위 무제한
        if (waitSeconds >= MAX_WAIT_TIME_SECONDS) {
            range = Integer.MAX_VALUE;
            log.info("Player {} wait time exceeded 3 minutes, removing ELO restriction", playerId);
        }

        // ELO 범위 계산
        int minElo = playerElo - range;
        int maxElo = playerElo + range;

        // Redis Lua Script 실행 (원자적 연산)
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptText(FIND_AND_MATCH_SCRIPT);
        script.setResultType(String.class);

        List<String> keys = Arrays.asList(
            MATCHMAKING_QUEUE_KEY,
            MATCHMAKING_TIMESTAMP_KEY
        );

        List<String> args = Arrays.asList(
            playerId.toString(),
            String.valueOf(minElo),
            String.valueOf(maxElo)
        );

        String opponentIdStr = redisTemplate.execute(script, keys, args.toArray());

        if (opponentIdStr == null) {
            log.debug("No opponent found for player {} (ELO: {}, range: ±{})", playerId, playerElo, range);
            return null;
        }

        Long opponentId = Long.parseLong(opponentIdStr);
        log.info("Match found (atomic): Player {} (ELO: {}) vs Player {} (range: ±{})",
            playerId, playerElo, opponentId, range);

        return opponentId;
    }

    /**
     * 매칭 확정 (두 플레이어를 큐에서 제거)
     *
     * 참고: findOpponent() 메서드가 Lua Script를 사용하여 자동으로 큐에서 제거하므로,
     * 이 메서드는 findOpponent() 호출 후 별도로 호출할 필요가 없습니다.
     * (레거시 호환성을 위해 남겨둠)
     */
    @Deprecated
    public void confirmMatch(Long player1Id, Long player2Id) {
        leaveQueue(player1Id);
        leaveQueue(player2Id);

        log.info("Match confirmed: Player {} vs Player {}", player1Id, player2Id);
    }

    /**
     * 현재 대기열 크기 조회
     */
    public long getQueueSize() {
        Long size = redisTemplate.opsForZSet().size(MATCHMAKING_QUEUE_KEY);
        return size != null ? size : 0;
    }

    /**
     * 플레이어가 대기열에 있는지 확인
     */
    public boolean isInQueue(Long playerId) {
        Double score = redisTemplate.opsForZSet().score(MATCHMAKING_QUEUE_KEY, playerId.toString());
        return score != null;
    }
}
