-- =====================================================
-- HOTEL SORTIS - Initial Data Insert Script
-- 호텔 소르티스 - 초기 데이터 삽입 스크립트
-- =====================================================
-- 실행: mysql -u root -p hotel_sortis < insert.sql
-- 주의: create.sql 실행 후 실행해야 함
-- =====================================================

-- =====================================================
-- 1. 스킬 데이터 (Skills)
-- =====================================================

-- -----------------------------------------------------
-- 1.1 Common 스킬 (기본 제공, 5개)
-- -----------------------------------------------------
INSERT INTO skills (skill_code, name_en, name_ko, name_ja, name_zh, rarity, description_en, description_ko, description_ja, description_zh, trigger_type, effect_json, unlock_floor) VALUES

('lucky_reroll', 'Lucky Reroll', '행운의 재굴림', 'ラッキーリロール', '幸运重掷', 'Common',
 'Automatically reroll 1 die at battle start.',
 '전투 시작 시 주사위 1개를 자동으로 재굴림합니다.',
 '戦闘開始時にサイコロ1個を自動的に振り直します。',
 '战斗开始时自动重掷1个骰子。',
 'BATTLE_START',
 '{"type": "reroll", "count": 1}',
 NULL),

('steady_hand', 'Steady Hand', '안정된 손', '安定の手', '稳定之手', 'Common',
 'Convert all 1s to 2s after rolling.',
 '주사위 결과 중 1이 나오면 자동으로 2로 변경됩니다.',
 '1の目が出たら自動的に2に変更されます。',
 '将所有1点自动转换为2点。',
 'DICE_ROLL',
 '{"type": "convert", "from": 1, "to": 2}',
 NULL),

('safe_bet', 'Safe Bet', '안전한 베팅', '安全な賭け', '安全下注', 'Common',
 'Deal 2x damage with No Hand.',
 '노 핸드 족보일 때 데미지가 2배가 됩니다.',
 '役なしの時、ダメージが2倍になります。',
 '无手牌时造成2倍伤害。',
 'BEFORE_DAMAGE',
 '{"type": "damage_multiplier", "condition": "NoHand", "multiplier": 2}',
 NULL),

('quick_learner', 'Quick Learner', '빠른 학습', 'クイックラーナー', '快速学习', 'Common',
 'Gain +5 damage for each turn passed.',
 '턴이 지날 때마다 데미지가 +5 증가합니다.',
 'ターンごとにダメージが+5増加します。',
 '每回合伤害+5。',
 'BEFORE_DAMAGE',
 '{"type": "scaling_damage", "per_turn": 5}',
 NULL),

('lucky_start', 'Lucky Start', '행운의 시작', 'ラッキースタート', '幸运开局', 'Common',
 'First roll of the battle deals +20 damage.',
 '전투의 첫 번째 굴림에 데미지 +20.',
 '戦闘の最初のロールでダメージ+20。',
 '战斗首次掷骰伤害+20。',
 'BEFORE_DAMAGE',
 '{"type": "first_turn_bonus", "bonus": 20}',
 NULL),

-- -----------------------------------------------------
-- 1.2 Rare 스킬 (5층 보스 보상, 10개)
-- -----------------------------------------------------
('high_roller', 'High Roller', '하이 롤러', 'ハイローラー', '豪赌客', 'Rare',
 'One die always rolls 4 or higher.',
 '주사위 1개가 항상 4 이상으로 나옵니다.',
 'サイコロ1個が常に4以上になります。',
 '1个骰子总是掷出4点或更高。',
 'DICE_ROLL',
 '{"type": "min_value", "count": 1, "min": 4}',
 5),

('pair_master', 'Pair Master', '페어 마스터', 'ペアマスター', '对子大师', 'Rare',
 'Pair hands deal +30 damage.',
 '페어 족보 완성 시 공격력 +30.',
 'ペア役完成時に攻撃力+30。',
 '对子手牌额外造成+30伤害。',
 'BEFORE_DAMAGE',
 '{"type": "damage_bonus", "condition": "Pair", "bonus": 30}',
 5),

('double_or_nothing', 'Double or Nothing', '더블 오어 낫씽', 'ダブル・オア・ナッシング', '全赢或全输', 'Rare',
 '50% chance to deal 2x damage or 0.',
 '50% 확률로 데미지 2배 또는 0.',
 '50%の確率でダメージ2倍または0。',
 '50%几率造成2倍伤害或0伤害。',
 'BEFORE_DAMAGE',
 '{"type": "gamble", "chance": 0.5, "multiplier": 2}',
 5),

('iron_will', 'Iron Will', '강철 의지', '鉄の意志', '钢铁意志', 'Rare',
 'Take 20% less damage when HP is below 50.',
 'HP가 50 이하일 때 받는 데미지 20% 감소.',
 'HPが50以下の時、受けるダメージ20%減少。',
 'HP低于50时受到伤害减少20%。',
 'PASSIVE',
 '{"type": "damage_reduction", "hp_threshold": 50, "reduction": 0.2}',
 5),

('greed_dice', 'Greed Dice', '탐욕의 주사위', '強欲のダイス', '贪婪骰子', 'Rare',
 'Triple hands deal +20 damage.',
 '트리플 족보 완성 시 공격력 +20.',
 'トリプル役完成時に攻撃力+20。',
 '三同号额外造成+20伤害。',
 'BEFORE_DAMAGE',
 '{"type": "damage_bonus", "condition": "Triple", "bonus": 20}',
 5),

('second_chance', 'Second Chance', '두 번째 기회', 'セカンドチャンス', '第二次机会', 'Rare',
 'Once per battle, reroll all dice if No Hand.',
 '전투당 1회, 노 핸드일 때 모든 주사위를 재굴림.',
 '戦闘ごとに1回、役なしの時全てのサイコロを振り直す。',
 '每场战斗1次，无手牌时重掷所有骰子。',
 'DICE_ROLL',
 '{"type": "conditional_reroll", "condition": "NoHand", "uses": 1}',
 5),

('pressure_point', 'Pressure Point', '급소 공격', '急所攻撃', '要害攻击', 'Rare',
 'Deal +15 damage to enemies below 30 HP.',
 '적 HP가 30 이하일 때 데미지 +15.',
 '敵のHPが30以下の時ダメージ+15。',
 '敌人HP低于30时伤害+15。',
 'BEFORE_DAMAGE',
 '{"type": "execute_bonus", "threshold": 30, "bonus": 15}',
 5),

('dice_collector', 'Dice Collector', '주사위 수집가', 'ダイスコレクター', '骰子收藏家', 'Rare',
 'Each unique number in your roll adds +3 damage.',
 '굴림에서 서로 다른 숫자 하나당 데미지 +3.',
 'ロールで異なる数字ごとにダメージ+3。',
 '掷骰中每个不同数字伤害+3。',
 'BEFORE_DAMAGE',
 '{"type": "unique_bonus", "per_unique": 3}',
 5),

('momentum', 'Momentum', '기세', 'モメンタム', '气势', 'Rare',
 'Consecutive wins increase damage by +10 (max +30).',
 '연속 승리 시 데미지 +10 (최대 +30).',
 '連続勝利時にダメージ+10（最大+30）。',
 '连续胜利时伤害+10（最多+30）。',
 'BEFORE_DAMAGE',
 '{"type": "streak_bonus", "per_win": 10, "max": 30}',
 5),

('mirror_shield', 'Mirror Shield', '거울 방패', 'ミラーシールド', '镜盾', 'Rare',
 'Reflect 15% of damage taken.',
 '받는 데미지의 15%를 반사.',
 '受けたダメージの15%を反射。',
 '反射15%受到的伤害。',
 'AFTER_DAMAGE',
 '{"type": "reflect", "percentage": 0.15}',
 5),

-- -----------------------------------------------------
-- 1.3 Epic 스킬 (10층 보스 보상, 10개)
-- -----------------------------------------------------
('storm_seeker', 'Storm Seeker', '스톰 시커', 'ストームシーカー', '风暴追寻者', 'Epic',
 'Storm [1-2-3] deals +50 damage (total 200).',
 '스톰 [1-2-3] 완성 시 공격력 +50 (총 200).',
 'ストーム[1-2-3]完成時に攻撃力+50（合計200）。',
 '风暴[1-2-3]额外造成+50伤害（总计200）。',
 'BEFORE_DAMAGE',
 '{"type": "damage_bonus", "condition": "Storm", "bonus": 50}',
 10),

('perfect_defense', 'Perfect Defense', '완벽한 방어', '完全防御', '完美防御', 'Epic',
 'Take 50% less damage from No Hand attacks.',
 '노 핸드 공격에서 받는 데미지 50% 감소.',
 '役なし攻撃から受けるダメージ50%減少。',
 '受到无手牌攻击时伤害减少50%。',
 'PASSIVE',
 '{"type": "conditional_reduction", "enemy_condition": "NoHand", "reduction": 0.5}',
 10),

('counter_attack', 'Counter Attack', '반격', 'カウンター攻撃', '反击', 'Epic',
 'Reflect 30% of damage taken back to attacker.',
 '받은 데미지의 30%를 반격.',
 '受けたダメージの30%を反撃。',
 '反击30%受到的伤害。',
 'AFTER_DAMAGE',
 '{"type": "reflect", "percentage": 0.3}',
 10),

('straight_shooter', 'Straight Shooter', '스트레이트 슈터', 'ストレートシューター', '顺子射手', 'Epic',
 'Straight [4-5-6] deals +40 damage (total 220).',
 '스트레이트 [4-5-6] 완성 시 공격력 +40 (총 220).',
 'ストレート[4-5-6]完成時に攻撃力+40（合計220）。',
 '顺子[4-5-6]额外造成+40伤害（总计220）。',
 'BEFORE_DAMAGE',
 '{"type": "damage_bonus", "condition": "Straight", "bonus": 40}',
 10),

('vampiric_dice', 'Vampiric Dice', '흡혈 주사위', '吸血のダイス', '吸血骰子', 'Epic',
 'Heal 20% of damage dealt.',
 '가한 데미지의 20%만큼 회복.',
 '与えたダメージの20%回復。',
 '回复造成伤害的20%。',
 'AFTER_DAMAGE',
 '{"type": "lifesteal", "percentage": 0.2}',
 10),

('berserker', 'Berserker', '광전사', 'バーサーカー', '狂战士', 'Epic',
 'Deal +50% damage but take +25% damage.',
 '데미지 +50%, 받는 데미지 +25%.',
 'ダメージ+50%、受けるダメージ+25%。',
 '伤害+50%，受到伤害+25%。',
 'PASSIVE',
 '{"type": "trade_off", "damage_bonus": 0.5, "damage_taken": 0.25}',
 10),

('armor_piercing', 'Armor Piercing', '관통', '貫通', '穿甲', 'Epic',
 'Ignore 30% of enemy damage reduction.',
 '적의 데미지 감소 효과 30% 무시.',
 '敵のダメージ減少効果を30%無視。',
 '无视敌人30%伤害减免。',
 'BEFORE_DAMAGE',
 '{"type": "penetration", "percentage": 0.3}',
 10),

('last_stand', 'Last Stand', '최후의 저항', 'ラストスタンド', '背水一战', 'Epic',
 'When HP drops below 20, deal 2x damage for 2 turns.',
 'HP가 20 이하가 되면 2턴 동안 데미지 2배.',
 'HPが20以下になると2ターンダメージ2倍。',
 'HP低于20时，2回合内伤害翻倍。',
 'PASSIVE',
 '{"type": "low_hp_buff", "threshold": 20, "multiplier": 2, "duration": 2}',
 10),

('chain_reaction', 'Chain Reaction', '연쇄 반응', '連鎖反応', '连锁反应', 'Epic',
 'If you deal 100+ damage, deal additional 30 damage.',
 '100 이상의 데미지를 가하면 추가로 30 데미지.',
 '100以上のダメージを与えると追加で30ダメージ。',
 '造成100以上伤害时额外造成30伤害。',
 'AFTER_DAMAGE',
 '{"type": "threshold_bonus", "threshold": 100, "bonus": 30}',
 10),

('triple_threat', 'Triple Threat', '트리플 쓰렛', 'トリプルスレット', '三重威胁', 'Epic',
 'Triple hands have 20% chance to skip enemy turn.',
 '트리플 완성 시 20% 확률로 적 턴 스킵.',
 'トリプル完成時に20%の確率で敵のターンをスキップ。',
 '三同号完成时20%几率跳过敌人回合。',
 'AFTER_DAMAGE',
 '{"type": "stun_chance", "condition": "Triple", "chance": 0.2}',
 10),

-- -----------------------------------------------------
-- 1.4 Legendary 스킬 (15층 보스 보상, 10개)
-- -----------------------------------------------------
('fate_manipulation', 'Fate Manipulation', '운명 조작', '運命操作', '命运操控', 'Legendary',
 'After rolling, change 1 die to any number (1-6).',
 '굴린 후 주사위 1개를 원하는 숫자(1-6)로 변경 가능.',
 'サイコロを振った後、1個を1~6の好きな数字に変更できます。',
 '掷骰后可将1个骰子改为1-6之间任意数字。',
 'DICE_ROLL',
 '{"type": "manipulate", "count": 1}',
 15),

('probability_distortion', 'Probability Distortion', '확률 왜곡', '確率歪曲', '概率扭曲', 'Legendary',
 '2x probability for Ace and Triple hands.',
 '에이스와 트리플 확률이 2배.',
 'エースとトリプルの確率が2倍。',
 'Ace和三同号的概率翻倍。',
 'PASSIVE',
 '{"type": "probability_boost", "hands": ["Ace", "Triple"], "multiplier": 2}',
 15),

('cosmic_hand', 'Cosmic Hand', '우주의 손', '宇宙の手', '宇宙之手', 'Legendary',
 'Straight or Storm skips enemy next turn.',
 '스트레이트 또는 스톰 완성 시 적 다음 턴 스킵.',
 'ストレートまたはストーム完成時に敵の次のターンをスキップ。',
 '顺子或风暴完成时跳过敌人下回合。',
 'AFTER_DAMAGE',
 '{"type": "skip_turn", "condition": ["Straight", "Storm"]}',
 15),

('dimension_dice', 'Dimension Dice', '차원의 주사위', '次元のダイス', '维度骰子', 'Legendary',
 'Roll 4 dice instead of 3, choose best 3.',
 '주사위를 4개 굴리고 최적의 3개 선택.',
 'サイコロを4個振り、最良の3個を選択。',
 '掷4个骰子，选择最好的3个。',
 'DICE_ROLL',
 '{"type": "extra_dice", "roll": 4, "keep": 3}',
 15),

('time_rewind', 'Time Rewind', '시간 되감기', 'タイムリワインド', '时间倒流', 'Legendary',
 'Once per battle, redo your entire turn.',
 '전투당 1회, 턴 전체를 다시 진행.',
 '戦闘ごとに1回、ターン全体をやり直す。',
 '每场战斗1次，重做整个回合。',
 'PASSIVE',
 '{"type": "redo_turn", "uses": 1}',
 15),

('divine_blessing', 'Divine Blessing', '신의 축복', '神の祝福', '神圣祝福', 'Legendary',
 'Start battle with a shield that absorbs 50 damage.',
 '전투 시작 시 50 데미지를 흡수하는 보호막.',
 '戦闘開始時に50ダメージを吸収するシールド。',
 '战斗开始时获得吸收50伤害的护盾。',
 'BATTLE_START',
 '{"type": "shield", "amount": 50}',
 15),

('ace_hunter', 'Ace Hunter', '에이스 헌터', 'エースハンター', '王牌猎手', 'Legendary',
 'Ace [1-1-1] deals +70 damage (total 250).',
 '에이스 [1-1-1] 완성 시 공격력 +70 (총 250).',
 'エース[1-1-1]完成時に攻撃力+70（合計250）。',
 'Ace[1-1-1]额外造成+70伤害（总计250）。',
 'BEFORE_DAMAGE',
 '{"type": "damage_bonus", "condition": "Ace", "bonus": 70}',
 15),

('soul_exchange', 'Soul Exchange', '영혼 교환', '魂の交換', '灵魂交换', 'Legendary',
 'Once per battle, swap HP with enemy.',
 '전투당 1회, 적과 HP를 교환.',
 '戦闘ごとに1回、敵とHPを交換。',
 '每场战斗1次，与敌人交换HP。',
 'PASSIVE',
 '{"type": "hp_swap", "uses": 1}',
 15),

('infinite_luck', 'Infinite Luck', '무한한 행운', '無限の幸運', '无限幸运', 'Legendary',
 'All 6s in your roll deal triple damage.',
 '굴림에서 모든 6은 3배 데미지.',
 'ロールで全ての6は3倍ダメージ。',
 '掷骰中所有6造成3倍伤害。',
 'BEFORE_DAMAGE',
 '{"type": "six_bonus", "multiplier": 3}',
 15),

('chaos_lord', 'Chaos Lord', '혼돈의 군주', '混沌の君主', '混沌之主', 'Legendary',
 'Randomly apply one of: 2x damage, heal 30, skip enemy turn.',
 '랜덤하게 2배 데미지, 30 회복, 적 턴 스킵 중 하나 발동.',
 'ランダムで2倍ダメージ、30回復、敵ターンスキップのいずれかを発動。',
 '随机触发：2倍伤害、回复30、跳过敌人回合其一。',
 'AFTER_DAMAGE',
 '{"type": "random_effect", "effects": ["double_damage", "heal_30", "skip_turn"]}',
 15);

-- =====================================================
-- 2. 층 정보 데이터 (Floors)
-- =====================================================
INSERT INTO floors (id, floor_type, battle_count, boss_id, ai_level, enemy_skill_count, skill_reward_rarity, description_ko, description_en) VALUES
(1,  'NORMAL', 3, NULL,       0, 0, NULL,        '로비 - 첫 번째 시험',        'Lobby - First Trial'),
(2,  'NORMAL', 3, NULL,       0, 0, NULL,        '카지노 홀',                   'Casino Hall'),
(3,  'NORMAL', 4, NULL,       0, 0, NULL,        'VIP 라운지',                  'VIP Lounge'),
(4,  'ELITE',  1, NULL,       1, 1, NULL,        '경비실 - 엘리트 경비원',      'Security Room - Elite Guard'),
(5,  'BOSS',   1, 'mammon',   1, 2, 'Rare',      '금고 - 탐욕의 마몬',          'Vault - Mammon the Greedy'),
(6,  'NORMAL', 3, NULL,       1, 1, NULL,        '균열의 복도',                 'Cracked Corridor'),
(7,  'NORMAL', 4, NULL,       1, 1, NULL,        '거울의 방',                   'Hall of Mirrors'),
(8,  'NORMAL', 3, NULL,       1, 1, NULL,        '왜곡된 객실',                 'Distorted Rooms'),
(9,  'ELITE',  1, NULL,       2, 2, NULL,        '경비대장실 - 엘리트 대장',    'Captain''s Quarters - Elite Captain'),
(10, 'BOSS',   1, 'eligor',   2, 3, 'Epic',      '방어의 탑 - 철벽의 엘리고르', 'Tower of Defense - Eligor the Ironclad'),
(11, 'NORMAL', 4, NULL,       2, 2, NULL,        '차원의 틈',                   'Dimensional Rift'),
(12, 'ELITE',  1, NULL,       2, 3, NULL,        '공허의 감시자',               'Void Watcher'),
(13, 'NORMAL', 4, NULL,       2, 2, NULL,        '무한의 계단',                 'Infinite Stairway'),
(14, 'ELITE',  1, NULL,       3, 3, NULL,        '운명의 심판관',               'Judge of Fate'),
(15, 'BOSS',   1, 'lucifuge', 3, 4, 'Legendary', '펜트하우스 - 루시푸지',       'Penthouse - Lucifuge Rofocale');

-- =====================================================
-- 3. 보스 정보 데이터 (Bosses)
-- =====================================================
INSERT INTO bosses (id, name_en, name_ko, name_ja, name_zh, floor, total_phases, phase_config, quotes) VALUES

('mammon', 'Mammon the Greedy', '탐욕의 마몬', '強欲のマモン', '贪婪的玛门', 5, 2,
 '{
   "phase1": {
     "hp": 100,
     "aiLevel": 1,
     "skills": ["greed_dice", "double_or_nothing"],
     "pattern": "aggressive",
     "restrictions": ["Storm"]
   },
   "phase2": {
     "hp": 100,
     "aiLevel": 2,
     "skills": ["greed_dice", "double_or_nothing", "high_roller"],
     "pattern": "desperate",
     "restrictions": []
   }
 }',
 '{
   "intro": "You want to climb higher? Then bet EVERYTHING!",
   "phase2": "You''ve forced my hand... ALL IN!",
   "defeat": "The house... always wins... or so I thought...",
   "intro_ko": "더 올라가고 싶다고? 그럼 전부 걸어!",
   "phase2_ko": "네가 이렇게 만들었다... 올인!",
   "defeat_ko": "하우스가... 항상 이긴다고... 그렇게 생각했는데..."
 }'),

('eligor', 'Eligor the Ironclad', '철벽의 엘리고르', '鉄壁のエリゴール', '铁壁埃力格', 10, 2,
 '{
   "phase1": {
     "hp": 100,
     "aiLevel": 2,
     "skills": ["perfect_defense", "iron_will"],
     "pattern": "defensive",
     "damageReduction": 0.2
   },
   "phase2": {
     "hp": 100,
     "aiLevel": 2,
     "skills": ["perfect_defense", "iron_will", "counter_attack"],
     "pattern": "counter",
     "damageReduction": 0.3,
     "reflectDamage": 0.5
   }
 }',
 '{
   "intro": "Your attacks are futile against my armor.",
   "phase2": "You''ve cracked my shell... Now feel my wrath!",
   "defeat": "Impossible... my defense was absolute...",
   "intro_ko": "네 공격은 내 갑옷에 소용없다.",
   "phase2_ko": "내 껍질을 깼군... 이제 분노를 느껴라!",
   "defeat_ko": "불가능해... 내 방어는 완벽했는데..."
 }'),

('lucifuge', 'Lucifuge Rofocale', '루시푸지 로포칼레', 'ルシフージュ・ロフォカーレ', '路西弗格·罗佛卡勒', 15, 3,
 '{
   "phase1": {
     "hp": 100,
     "aiLevel": 3,
     "skills": ["dimension_dice", "fate_manipulation"],
     "pattern": "testing",
     "diceCount": 6,
     "specialDice": []
   },
   "phase2": {
     "hp": 100,
     "aiLevel": 3,
     "skills": ["dimension_dice", "fate_manipulation", "probability_distortion"],
     "pattern": "serious",
     "diceCount": 7,
     "specialDice": ["Void", "Chaos"]
   },
   "phase3": {
     "hp": 100,
     "aiLevel": 3,
     "skills": ["dimension_dice", "fate_manipulation", "probability_distortion", "cosmic_hand"],
     "pattern": "true_form",
     "diceCount": 9,
     "specialDice": ["Void", "Chaos", "Destiny", "Paradox", "Infinity"]
   }
 }',
 '{
   "intro": "Welcome, wanderer. Let us test your resolve.",
   "phase2": "Impressive. But you''ve seen nothing yet.",
   "phase3": "Behold... the true nature of fate.",
   "defeat": "You... defy probability itself. Remarkable.",
   "intro_ko": "어서 오게, 방랑자여. 네 결의를 시험하지.",
   "phase2_ko": "인상적이군. 하지만 아직 아무것도 보지 못했어.",
   "phase3_ko": "보아라... 운명의 진정한 본질을.",
   "defeat_ko": "너는... 확률 자체를 거스르는군. 놀랍군."
 }');

-- =====================================================
-- 4. 초기 PvP 시즌 데이터
-- =====================================================
INSERT INTO pvp_seasons (season_name, start_date, end_date, is_active) VALUES
('Season 1 - Grand Opening', '2026-01-01', '2026-03-31', TRUE);

-- =====================================================
-- 5. 테스트용 플레이어 (개발용, 운영시 제거)
-- =====================================================
INSERT INTO players (username, password_hash, elo, soul_stones, preferred_language) VALUES
('testplayer', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqDqX7jbkBBqJc9B.x3xmNYTMBq8Xci', 1000, 100, 'ko'),
('testplayer2', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqDqX7jbkBBqJc9B.x3xmNYTMBq8Xci', 1000, 100, 'en');

-- 테스트 플레이어에게 기본 Common 스킬 해금
INSERT INTO player_skills (player_id, skill_id)
SELECT 1, id FROM skills WHERE rarity = 'Common';

INSERT INTO player_skills (player_id, skill_id)
SELECT 2, id FROM skills WHERE rarity = 'Common';

-- 테스트 플레이어 캠페인 진행도 초기화
INSERT INTO campaign_progress (player_id) VALUES (1), (2);

-- =====================================================
-- 완료 메시지
-- =====================================================
SELECT 'Data insertion complete!' AS message;
SELECT rarity, COUNT(*) as count FROM skills GROUP BY rarity;
SELECT 'Total skills:' AS label, COUNT(*) AS total FROM skills;
SELECT 'Total floors:' AS label, COUNT(*) AS total FROM floors;
SELECT 'Total bosses:' AS label, COUNT(*) AS total FROM bosses;
