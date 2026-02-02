import { watch } from 'vue'
import { Howl, Howler } from 'howler'
import { useSettingsStore } from '@/stores/settings'

/**
 * SoundManager - Howler.js 기반 사운드 시스템
 * PROJECTPLAN.md 11장 사운드 디자인 규격 준수
 *
 * BGM: 크로스페이드 + 루핑
 * SFX: 효과음 (주사위, 족보, 전투, UI)
 * Web Audio API: 에셋 파일 없이도 합성음으로 동작
 */

// --- Audio Context for synthesized sounds ---
let audioCtx: AudioContext | null = null

function getAudioCtx(): AudioContext {
  if (!audioCtx) {
    audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)()
  }
  if (audioCtx.state === 'suspended') {
    audioCtx.resume()
  }
  return audioCtx
}

// --- Synthesized Sound Generators ---

function playTone(
  frequency: number,
  duration: number,
  type: OscillatorType = 'sine',
  volume = 0.3,
  rampDown = true
) {
  const settings = useSettingsStore()
  if (settings.settings.muted) return

  const effectiveVol = volume * (settings.settings.sfxVolume / 100)
  if (effectiveVol <= 0) return

  const ctx = getAudioCtx()
  const osc = ctx.createOscillator()
  const gain = ctx.createGain()

  osc.type = type
  osc.frequency.setValueAtTime(frequency, ctx.currentTime)
  gain.gain.setValueAtTime(effectiveVol, ctx.currentTime)

  if (rampDown) {
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + duration)
  }

  osc.connect(gain)
  gain.connect(ctx.destination)
  osc.start(ctx.currentTime)
  osc.stop(ctx.currentTime + duration)
}

function playNoise(duration: number, volume = 0.15) {
  const settings = useSettingsStore()
  if (settings.settings.muted) return

  const effectiveVol = volume * (settings.settings.sfxVolume / 100)
  if (effectiveVol <= 0) return

  const ctx = getAudioCtx()
  const bufferSize = ctx.sampleRate * duration
  const buffer = ctx.createBuffer(1, bufferSize, ctx.sampleRate)
  const data = buffer.getChannelData(0)

  for (let i = 0; i < bufferSize; i++) {
    data[i] = (Math.random() * 2 - 1) * (1 - i / bufferSize) // decay
  }

  const source = ctx.createBufferSource()
  const gain = ctx.createGain()
  const filter = ctx.createBiquadFilter()

  source.buffer = buffer
  filter.type = 'lowpass'
  filter.frequency.setValueAtTime(2000, ctx.currentTime)
  gain.gain.setValueAtTime(effectiveVol, ctx.currentTime)
  gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + duration)

  source.connect(filter)
  filter.connect(gain)
  gain.connect(ctx.destination)
  source.start(ctx.currentTime)
}

// --- Sound Effect Functions (synthesized) ---

export const SFX = {
  /** 주사위 굴림 시작 - 나무 탁자 위 구르는 소리 */
  diceRoll() {
    playNoise(0.8, 0.2)
    // Multiple short taps to simulate bouncing
    for (let i = 0; i < 5; i++) {
      setTimeout(() => {
        playTone(200 + Math.random() * 300, 0.05, 'square', 0.1)
      }, i * 80 + Math.random() * 40)
    }
  },

  /** 주사위 착지 - 멈추는 소리 */
  diceLand() {
    playTone(350, 0.15, 'triangle', 0.2)
    playNoise(0.1, 0.15)
  },

  /** 에이스 족보 완성 - 반짝이는 마법 소리 */
  aceComplete() {
    const notes = [1047, 1319, 1568, 2093] // C6, E6, G6, C7
    notes.forEach((freq, i) => {
      setTimeout(() => playTone(freq, 0.6 - i * 0.1, 'sine', 0.25), i * 150)
    })
  },

  /** 트리플 족보 완성 - 폭발적 소리 */
  tripleComplete() {
    playTone(880, 0.5, 'sawtooth', 0.2)
    setTimeout(() => playTone(1100, 0.4, 'sine', 0.25), 100)
    setTimeout(() => playTone(1320, 0.3, 'sine', 0.2), 200)
  },

  /** 스트레이트/스트라이크/슬래시 족보 완성 - 상승 음계 */
  straightComplete() {
    const notes = [523, 659, 784] // C5, E5, G5
    notes.forEach((freq, i) => {
      setTimeout(() => playTone(freq, 0.3, 'sine', 0.2), i * 120)
    })
  },

  /** 스톰 족보 완성 - 바람 + 천둥 */
  stormComplete() {
    playNoise(0.8, 0.2)
    setTimeout(() => playTone(80, 0.5, 'sawtooth', 0.3), 200)
    setTimeout(() => playNoise(0.3, 0.25), 400)
  },

  /** 페어 족보 완성 - 부드러운 벨 소리 */
  pairComplete() {
    playTone(659, 0.4, 'sine', 0.2) // E5
    setTimeout(() => playTone(784, 0.3, 'sine', 0.15), 100) // G5
  },

  /** 노 핸드 - 실망 소리 */
  noHandComplete() {
    playTone(262, 0.3, 'sine', 0.15) // C4
    setTimeout(() => playTone(220, 0.4, 'sine', 0.12), 150) // A3 (descending)
  },

  /** 데미지 처리 - 히트 사운드 */
  damageDealt() {
    playTone(150, 0.15, 'square', 0.2)
    playNoise(0.1, 0.2)
  },

  /** 피격 사운드 */
  damageTaken() {
    playTone(100, 0.2, 'sawtooth', 0.15)
    setTimeout(() => playNoise(0.15, 0.1), 50)
  },

  /** 승리 팡파르 */
  victory() {
    const fanfare = [523, 659, 784, 1047] // C5, E5, G5, C6
    fanfare.forEach((freq, i) => {
      setTimeout(() => playTone(freq, 0.5, 'sine', 0.25), i * 200)
    })
    setTimeout(() => {
      playTone(1047, 1.0, 'sine', 0.3)
      playTone(784, 1.0, 'sine', 0.15)
      playTone(523, 1.0, 'sine', 0.1)
    }, 800)
  },

  /** 패배 사운드 */
  defeat() {
    const notes = [440, 392, 349, 262] // A4, G4, F4, C4 (descending)
    notes.forEach((freq, i) => {
      setTimeout(() => playTone(freq, 0.5, 'sine', 0.2), i * 250)
    })
  },

  /** 스킬 발동 (희귀도별) */
  skillActivate(rarity: 'Common' | 'Rare' | 'Epic' | 'Legendary') {
    switch (rarity) {
      case 'Common':
        playTone(600, 0.3, 'sine', 0.12)
        break
      case 'Rare':
        playTone(700, 0.5, 'sine', 0.15)
        setTimeout(() => playTone(900, 0.3, 'sine', 0.12), 100)
        break
      case 'Epic':
        playTone(500, 0.3, 'sawtooth', 0.12)
        setTimeout(() => playTone(800, 0.4, 'sine', 0.18), 100)
        setTimeout(() => playTone(1000, 0.3, 'sine', 0.15), 200)
        break
      case 'Legendary':
        const freqs = [400, 600, 800, 1000, 1200]
        freqs.forEach((f, i) => {
          setTimeout(() => playTone(f, 0.6 - i * 0.08, 'sine', 0.15), i * 80)
        })
        break
    }
  },

  /** UI 버튼 클릭 */
  buttonClick() {
    playTone(800, 0.06, 'sine', 0.1)
  },

  /** UI 버튼 호버 */
  buttonHover() {
    playTone(600, 0.04, 'sine', 0.05)
  },

  /** 턴 전환 사운드 */
  turnChange() {
    playTone(440, 0.15, 'triangle', 0.12)
    setTimeout(() => playTone(550, 0.12, 'triangle', 0.1), 100)
  },

  /** 타이머 경고 (10초 이하) */
  timerWarning() {
    playTone(880, 0.1, 'square', 0.15)
  },

  /** 페이즈 전환 */
  phaseTransition() {
    playNoise(0.5, 0.2)
    setTimeout(() => {
      playTone(200, 0.8, 'sawtooth', 0.2)
      playTone(400, 0.6, 'sine', 0.15)
    }, 300)
    setTimeout(() => playTone(800, 0.5, 'sine', 0.2), 800)
  },

  /** 족보에 맞는 사운드 자동 재생 */
  handComplete(rank: string) {
    switch (rank) {
      case 'Ace':
        SFX.aceComplete()
        break
      case 'Triple':
        SFX.tripleComplete()
        break
      case 'Straight':
      case 'Strike':
      case 'Slash':
        SFX.straightComplete()
        break
      case 'Storm':
        SFX.stormComplete()
        break
      case 'Pair':
        SFX.pairComplete()
        break
      case 'NoHand':
        SFX.noHandComplete()
        break
    }
  }
}

// --- BGM System ---

let currentBgm: Howl | null = null
let currentBgmKey: string | null = null

/** BGM 키에 해당하는 파일 경로 매핑 (에셋 파일 존재 시 사용) */
const BGM_MAP: Record<string, string> = {
  menu: '/sounds/bgm/menu.mp3',
  'floor-1-5': '/sounds/bgm/floor_1_5.mp3',
  'floor-6-10': '/sounds/bgm/floor_6_10.mp3',
  'floor-11-15': '/sounds/bgm/floor_11_15.mp3',
  boss: '/sounds/bgm/boss.mp3'
}

export const BGM = {
  /**
   * BGM 재생 (크로스페이드)
   * 에셋 파일이 없으면 무시 (에러 없음)
   */
  play(key: string) {
    const settings = useSettingsStore()
    if (settings.settings.muted) return
    if (currentBgmKey === key && currentBgm?.playing()) return

    const path = BGM_MAP[key]
    if (!path) return

    const volume = settings.settings.bgmVolume / 100

    // Fade out current
    if (currentBgm) {
      const oldBgm = currentBgm
      oldBgm.fade(oldBgm.volume(), 0, 1000)
      setTimeout(() => {
        oldBgm.stop()
        oldBgm.unload()
      }, 1000)
    }

    // Start new BGM
    currentBgm = new Howl({
      src: [path],
      loop: true,
      volume: 0,
      onloaderror: () => {
        // BGM file not found - silent fail (placeholder mode)
        currentBgm = null
        currentBgmKey = null
      }
    })

    currentBgm.play()
    currentBgm.fade(0, volume, 1500)
    currentBgmKey = key
  },

  /** BGM 정지 */
  stop() {
    if (currentBgm) {
      currentBgm.fade(currentBgm.volume(), 0, 800)
      setTimeout(() => {
        currentBgm?.stop()
        currentBgm?.unload()
        currentBgm = null
        currentBgmKey = null
      }, 800)
    }
  },

  /** BGM 볼륨 업데이트 */
  setVolume(volume: number) {
    if (currentBgm) {
      currentBgm.volume(volume / 100)
    }
  },

  /** 현재 BGM 키 */
  getCurrentKey(): string | null {
    return currentBgmKey
  }
}

// --- Global Volume Sync ---

/**
 * useSound - 사운드 시스템 초기화 composable
 * App.vue에서 한 번 호출하여 설정 변경 감시
 */
export function useSound() {
  const settings = useSettingsStore()

  // Watch volume changes
  watch(
    () => settings.settings.bgmVolume,
    (vol) => BGM.setVolume(vol)
  )

  watch(
    () => settings.settings.muted,
    (muted) => {
      if (muted) {
        Howler.mute(true)
        BGM.stop()
      } else {
        Howler.mute(false)
      }
    }
  )

  return { SFX, BGM }
}
