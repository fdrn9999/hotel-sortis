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
-- 1.1 Common 스킬 (기본 제공, 10개)
-- 밸런스 조정: 보너스 +1~3 범위
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
 'Deal 1.5x damage with No Hand.',
 '노 핸드 족보일 때 데미지가 1.5배가 됩니다.',
 '役なしの時、ダメージが1.5倍になります。',
 '无手牌时造成1.5倍伤害。',
 'BEFORE_DAMAGE',
 '{"type": "damage_multiplier", "condition": "NoHand", "multiplier": 1.5}',
 NULL),

('quick_learner', 'Quick Learner', '빠른 학습', 'クイックラーナー', '快速学习', 'Common',
 'Gain +1 damage for each turn passed.',
 '턴이 지날 때마다 데미지가 +1 증가합니다.',
 'ターンごとにダメージが+1増加します。',
 '每回合伤害+1。',
 'BEFORE_DAMAGE',
 '{"type": "scaling_damage", "per_turn": 1}',
 NULL),

('lucky_start', 'Lucky Start', '행운의 시작', 'ラッキースタート', '幸运开局', 'Common',
 'First roll of the battle deals +3 damage.',
 '전투의 첫 번째 굴림에 데미지 +3.',
 '戦闘の最初のロールでダメージ+3。',
 '战斗首次掷骰伤害+3。',
 'BEFORE_DAMAGE',
 '{"type": "first_turn_bonus", "bonus": 3}',
 NULL),

('defensive_stance', 'Defensive Stance', '방어 자세', 'ディフェンシブスタンス', '防御姿态', 'Common',
 'Reduce damage taken by 8%.',
 '받는 데미지 8% 감소.',
 '受けるダメージを8%減少。',
 '受到伤害减少8%。',
 'PASSIVE',
 '{"type": "damage_reduction", "reduction": 0.08}',
 NULL),

('dice_blessing', 'Dice Blessing', '주사위 축복', 'ダイスブレッシング', '骰子祝福', 'Common',
 'Each 6 in your roll deals +1 bonus damage.',
 '6이 나올 때마다 추가 데미지 +1.',
 '6が出るたびに追加ダメージ+1。',
 '每个6额外造成+1伤害。',
 'BEFORE_DAMAGE',
 '{"type": "six_bonus", "bonus": 1}',
 NULL),

('survivor', 'Survivor', '생존자', 'サバイバー', '幸存者', 'Common',
 'Heal 3 HP at the end of each turn.',
 '매 턴 종료 시 HP 3 회복.',
 '毎ターン終了時にHP3回復。',
 '每回合结束时回复3HP。',
 'TURN_END',
 '{"type": "heal", "amount": 3}',
 NULL),

('focused_mind', 'Focused Mind', '집중된 정신', '集中する精神', '专注心灵', 'Common',
 'Gain +2 damage for each same number in roll.',
 '같은 숫자가 나올 때마다 데미지 +2.',
 '同じ数字が出るたびにダメージ+2。',
 '每个相同数字伤害+2。',
 'BEFORE_DAMAGE',
 '{"type": "same_number_bonus", "bonus": 2}',
 NULL),

('calm_focus', 'Calm Focus', '침착한 집중', '冷静な集中', '冷静专注', 'Common',
 'Lowest die in your roll gets +1.',
 '가장 낮은 주사위에 +1.',
 '最も低いサイコロに+1。',
 '最低的骰子+1。',
 'DICE_ROLL',
 '{"type": "min_boost", "bonus": 1}',
 NULL),

-- -----------------------------------------------------
-- 1.2 Rare 스킬 (5층 보스 보상, 15개)
-- 밸런스 조정: 보너스 +3~7 범위
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
 'Pair hands deal +5 damage.',
 '페어 족보 완성 시 공격력 +5.',
 'ペア役完成時に攻撃力+5。',
 '对子手牌额外造成+5伤害。',
 'BEFORE_DAMAGE',
 '{"type": "damage_bonus", "condition": "Pair", "bonus": 5}',
 5),

('double_or_nothing', 'Double or Nothing', '더블 오어 낫씽', 'ダブル・オア・ナッシング', '全赢或全输', 'Rare',
 '50% chance to deal 1.5x damage or 0.',
 '50% 확률로 데미지 1.5배 또는 0.',
 '50%の確率でダメージ1.5倍または0。',
 '50%几率造成1.5倍伤害或0伤害。',
 'BEFORE_DAMAGE',
 '{"type": "gamble", "chance": 0.5, "multiplier": 1.5}',
 5),

('iron_will', 'Iron Will', '강철 의지', '鉄の意志', '钢铁意志', 'Rare',
 'Take 15% less damage when HP is below 50.',
 'HP가 50 이하일 때 받는 데미지 15% 감소.',
 'HPが50以下の時、受けるダメージ15%減少。',
 'HP低于50时受到伤害减少15%。',
 'PASSIVE',
 '{"type": "damage_reduction", "hp_threshold": 50, "reduction": 0.15}',
 5),

('greed_dice', 'Greed Dice', '탐욕의 주사위', '強欲のダイス', '贪婪骰子', 'Rare',
 'Triple hands deal +6 damage.',
 '트리플 족보 완성 시 공격력 +6.',
 'トリプル役完成時に攻撃力+6。',
 '三同号额外造成+6伤害。',
 'BEFORE_DAMAGE',
 '{"type": "damage_bonus", "condition": "Triple", "bonus": 6}',
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
 'Deal +5 damage to enemies below 30 HP.',
 '적 HP가 30 이하일 때 데미지 +5.',
 '敵のHPが30以下の時ダメージ+5。',
 '敌人HP低于30时伤害+5。',
 'BEFORE_DAMAGE',
 '{"type": "execute_bonus", "threshold": 30, "bonus": 5}',
 5),

('dice_collector', 'Dice Collector', '주사위 수집가', 'ダイスコレクター', '骰子收藏家', 'Rare',
 'Each unique number in your roll adds +2 damage.',
 '굴림에서 서로 다른 숫자 하나당 데미지 +2.',
 'ロールで異なる数字ごとにダメージ+2。',
 '掷骰中每个不同数字伤害+2。',
 'BEFORE_DAMAGE',
 '{"type": "unique_bonus", "per_unique": 2}',
 5),

('momentum', 'Momentum', '기세', 'モメンタム', '气势', 'Rare',
 'Consecutive wins increase damage by +3 (max +9).',
 '연속 승리 시 데미지 +3 (최대 +9).',
 '連続勝利時にダメージ+3（最大+9）。',
 '连续胜利时伤害+3（最多+9）。',
 'BEFORE_DAMAGE',
 '{"type": "streak_bonus", "per_win": 3, "max": 9}',
 5),

('mirror_shield', 'Mirror Shield', '거울 방패', 'ミラーシールド', '镜盾', 'Rare',
 'Reflect 10% of damage taken.',
 '받는 데미지의 10%를 반사.',
 '受けたダメージの10%を反射。',
 '反射10%受到的伤害。',
 'AFTER_DAMAGE',
 '{"type": "reflect", "percentage": 0.10}',
 5),

('storm_chaser', 'Storm Chaser', '스톰 체이서', 'ストームチェイサー', '风暴追逐者', 'Rare',
 'Storm [1-2-3] deals +6 damage.',
 '스톰 [1-2-3] 완성 시 공격력 +6.',
 'ストーム[1-2-3]完成時に攻撃力+6。',
 '风暴[1-2-3]额外造成+6伤害。',
 'BEFORE_DAMAGE',
 '{"type": "damage_bonus", "condition": "Storm", "bonus": 6}',
 5),

('tactical_retreat', 'Tactical Retreat', '전술적 후퇴', '戦術的撤退', '战术撤退', 'Rare',
 'When taking 25+ damage, reduce it by 5.',
 '25 이상의 데미지를 받을 때 5 감소.',
 '25以上のダメージを受ける時5減少。',
 '受到25以上伤害时减少5。',
 'PASSIVE',
 '{"type": "heavy_hit_reduction", "threshold": 25, "reduction": 5}',
 5),

('adrenaline_rush', 'Adrenaline Rush', '아드레날린 러시', 'アドレナリンラッシュ', '肾上腺素', 'Rare',
 'Deal +6 damage when HP is below 30.',
 'HP가 30 이하일 때 데미지 +6.',
 'HPが30以下の時ダメージ+6。',
 'HP低于30时伤害+6。',
 'BEFORE_DAMAGE',
 '{"type": "low_hp_bonus", "threshold": 30, "bonus": 6}',
 5),

('fortune_favor', 'Fortune''s Favor', '행운의 가호', '幸運の加護', '幸运眷顾', 'Rare',
 '25% chance to deal +6 bonus damage.',
 '25% 확률로 추가 데미지 +6.',
 '25%の確率で追加ダメージ+6。',
 '25%几率额外伤害+6。',
 'BEFORE_DAMAGE',
 '{"type": "luck_bonus", "chance": 0.25, "bonus": 6}',
 5),

('lucky_streak', 'Lucky Streak', '행운의 연속', 'ラッキーストリーク', '幸运连击', 'Rare',
 'Deal +4 damage if you get the same hand rank twice in a row.',
 '연속으로 같은 족보를 내면 데미지 +4.',
 '連続で同じ役を出すとダメージ+4。',
 '连续出相同手牌时伤害+4。',
 'BEFORE_DAMAGE',
 '{"type": "same_hand_bonus", "bonus": 4}',
 5),

-- -----------------------------------------------------
-- 1.3 Epic 스킬 (10층 보스 보상, 20개)
-- 밸런스 조정: 보너스 +6~12 범위
-- -----------------------------------------------------
('storm_seeker', 'Storm Seeker', '스톰 시커', 'ストームシーカー', '风暴追寻者', 'Epic',
 'Storm [1-2-3] deals +10 damage (total 26).',
 '스톰 [1-2-3] 완성 시 공격력 +10 (총 26).',
 'ストーム[1-2-3]完成時に攻撃力+10（合計26）。',
 '风暴[1-2-3]额外造成+10伤害（总计26）。',
 'BEFORE_DAMAGE',
 '{"type": "damage_bonus", "condition": "Storm", "bonus": 10}',
 10),

('perfect_defense', 'Perfect Defense', '완벽한 방어', '完全防御', '完美防御', 'Epic',
 'Take 35% less damage from No Hand attacks.',
 '노 핸드 공격에서 받는 데미지 35% 감소.',
 '役なし攻撃から受けるダメージ35%減少。',
 '受到无手牌攻击时伤害减少35%。',
 'PASSIVE',
 '{"type": "conditional_reduction", "enemy_condition": "NoHand", "reduction": 0.35}',
 10),

('counter_attack', 'Counter Attack', '반격', 'カウンター攻撃', '反击', 'Epic',
 'Reflect 20% of damage taken back to attacker.',
 '받은 데미지의 20%를 반격.',
 '受けたダメージの20%を反撃。',
 '反击20%受到的伤害。',
 'AFTER_DAMAGE',
 '{"type": "reflect", "percentage": 0.2}',
 10),

('straight_shooter', 'Straight Shooter', '스트레이트 슈터', 'ストレートシューター', '顺子射手', 'Epic',
 'Straight [4-5-6] deals +8 damage (total 46).',
 '스트레이트 [4-5-6] 완성 시 공격력 +8 (총 46).',
 'ストレート[4-5-6]完成時に攻撃力+8（合計46）。',
 '顺子[4-5-6]额外造成+8伤害（总计46）。',
 'BEFORE_DAMAGE',
 '{"type": "damage_bonus", "condition": "Straight", "bonus": 8}',
 10),

('vampiric_dice', 'Vampiric Dice', '흡혈 주사위', '吸血のダイス', '吸血骰子', 'Epic',
 'Heal 15% of damage dealt.',
 '가한 데미지의 15%만큼 회복.',
 '与えたダメージの15%回復。',
 '回复造成伤害的15%。',
 'AFTER_DAMAGE',
 '{"type": "lifesteal", "percentage": 0.15}',
 10),

('berserker', 'Berserker', '광전사', 'バーサーカー', '狂战士', 'Epic',
 'Deal +30% damage but take +20% damage.',
 '데미지 +30%, 받는 데미지 +20%.',
 'ダメージ+30%、受けるダメージ+20%。',
 '伤害+30%，受到伤害+20%。',
 'PASSIVE',
 '{"type": "trade_off", "damage_bonus": 0.3, "damage_taken": 0.2}',
 10),

('armor_piercing', 'Armor Piercing', '관통', '貫通', '穿甲', 'Epic',
 'Ignore 20% of enemy damage reduction.',
 '적의 데미지 감소 효과 20% 무시.',
 '敵のダメージ減少効果を20%無視。',
 '无视敌人20%伤害减免。',
 'BEFORE_DAMAGE',
 '{"type": "penetration", "percentage": 0.2}',
 10),

('last_stand', 'Last Stand', '최후의 저항', 'ラストスタンド', '背水一战', 'Epic',
 'When HP drops below 20, deal 1.5x damage for 2 turns.',
 'HP가 20 이하가 되면 2턴 동안 데미지 1.5배.',
 'HPが20以下になると2ターンダメージ1.5倍。',
 'HP低于20时，2回合内伤害1.5倍。',
 'PASSIVE',
 '{"type": "low_hp_buff", "threshold": 20, "multiplier": 1.5, "duration": 2}',
 10),

('chain_reaction', 'Chain Reaction', '연쇄 반응', '連鎖反応', '连锁反应', 'Epic',
 'If you deal 30+ damage, deal additional 6 damage.',
 '30 이상의 데미지를 가하면 추가로 6 데미지.',
 '30以上のダメージを与えると追加で6ダメージ。',
 '造成30以上伤害时额外造成6伤害。',
 'AFTER_DAMAGE',
 '{"type": "threshold_bonus", "threshold": 30, "bonus": 6}',
 10),

('triple_threat', 'Triple Threat', '트리플 쓰렛', 'トリプルスレット', '三重威胁', 'Epic',
 'Triple hands have 15% chance to skip enemy turn.',
 '트리플 완성 시 15% 확률로 적 턴 스킵.',
 'トリプル完成時に15%の確率で敵のターンをスキップ。',
 '三同号完成时15%几率跳过敌人回合。',
 'AFTER_DAMAGE',
 '{"type": "stun_chance", "condition": "Triple", "chance": 0.15}',
 10),

('ace_seeker', 'Ace Seeker', '에이스 시커', 'エースシーカー', '王牌寻找者', 'Epic',
 'Ace [1-1-1] deals +10 damage.',
 '에이스 [1-1-1] 완성 시 공격력 +10.',
 'エース[1-1-1]完成時に攻撃力+10。',
 'Ace[1-1-1]额外造成+10伤害。',
 'BEFORE_DAMAGE',
 '{"type": "damage_bonus", "condition": "Ace", "bonus": 10}',
 10),

('battle_hardened', 'Battle Hardened', '전투 경험', 'バトルハードゥン', '身经百战', 'Epic',
 'Reduce all damage taken by 18%.',
 '받는 모든 데미지 18% 감소.',
 '受けるすべてのダメージを18%減少。',
 '所有受到伤害减少18%。',
 'PASSIVE',
 '{"type": "damage_reduction", "reduction": 0.18}',
 10),

('critical_eye', 'Critical Eye', '치명적 시선', 'クリティカルアイ', '致命之眼', 'Epic',
 '20% chance to deal 1.5x damage.',
 '20% 확률로 1.5배 데미지.',
 '20%の確率で1.5倍ダメージ。',
 '20%几率造成1.5倍伤害。',
 'BEFORE_DAMAGE',
 '{"type": "critical", "chance": 0.2, "multiplier": 1.5}',
 10),

('soul_drain', 'Soul Drain', '영혼 흡수', '魂の吸収', '灵魂汲取', 'Epic',
 'Heal 12% of damage dealt.',
 '가한 데미지의 12%만큼 회복.',
 '与えたダメージの12%回復。',
 '回复造成伤害的12%。',
 'AFTER_DAMAGE',
 '{"type": "lifesteal", "percentage": 0.12}',
 10),

('dice_mastery', 'Dice Mastery', '주사위 숙련', 'ダイスマスタリー', '骰子精通', 'Epic',
 'Reroll one die after seeing results.',
 '결과 확인 후 주사위 1개 재굴림 가능.',
 '結果確認後にサイコロ1個を振り直し可能。',
 '查看结果后可重掷1个骰子。',
 'DICE_ROLL',
 '{"type": "reroll_choice", "count": 1}',
 10),

('vengeance', 'Vengeance', '복수', 'ヴェンジェンス', '复仇', 'Epic',
 'Deal +8 damage after taking 25+ damage in previous turn.',
 '이전 턴에 25 이상의 데미지를 받으면 다음 턴 데미지 +8.',
 '前のターンで25以上のダメージを受けると次のターンダメージ+8。',
 '上回合受到25以上伤害后伤害+8。',
 'BEFORE_DAMAGE',
 '{"type": "revenge_bonus", "threshold": 25, "bonus": 8}',
 10),

('lucky_sevens', 'Lucky Sevens', '럭키 세븐', 'ラッキーセブン', '幸运七', 'Epic',
 'If dice sum is 7 or 14, deal +8 damage.',
 '주사위 합이 7 또는 14이면 데미지 +8.',
 'サイコロの合計が7または14ならダメージ+8。',
 '骰子总和为7或14时伤害+8。',
 'BEFORE_DAMAGE',
 '{"type": "sum_bonus", "sums": [7, 14], "bonus": 8}',
 10),

('elemental_fury', 'Elemental Fury', '원소의 분노', 'エレメンタルフューリー', '元素狂怒', 'Epic',
 'No Hand deals +7 damage.',
 '노 핸드 시 데미지 +7.',
 '役なしの時ダメージ+7。',
 '无手牌时伤害+7。',
 'BEFORE_DAMAGE',
 '{"type": "damage_bonus", "condition": "NoHand", "bonus": 7}',
 10),

('unyielding', 'Unyielding', '불굴', 'アンイールディング', '坚韧不拔', 'Epic',
 'Cannot be reduced below 1 HP by a single attack.',
 '단일 공격으로 HP가 1 이하로 떨어지지 않음.',
 '単一攻撃でHPが1以下にならない。',
 '单次攻击不会使HP降至1以下。',
 'PASSIVE',
 '{"type": "survive", "min_hp": 1}',
 10),

('strike_master', 'Strike Master', '스트라이크 마스터', 'ストライクマスター', '打击大师', 'Epic',
 'Strike [3-4-5] or Slash [2-3-4] deals +8 damage.',
 '스트라이크 또는 슬래시 완성 시 공격력 +8.',
 'ストライクまたはスラッシュ完成時に攻撃力+8。',
 '打击或斩击额外造成+8伤害。',
 'BEFORE_DAMAGE',
 '{"type": "damage_bonus", "condition": ["Strike", "Slash"], "bonus": 8}',
 10),

-- -----------------------------------------------------
-- 1.4 Legendary 스킬 (15층 보스 보상, 15개)
-- 밸런스 조정: 보너스 +10~18 범위
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
 '1.5x probability for Ace and Triple hands.',
 '에이스와 트리플 확률이 1.5배.',
 'エースとトリプルの確率が1.5倍。',
 'Ace和三同号的概率1.5倍。',
 'PASSIVE',
 '{"type": "probability_boost", "hands": ["Ace", "Triple"], "multiplier": 1.5}',
 15),

('cosmic_hand', 'Cosmic Hand', '우주의 손', '宇宙の手', '宇宙之手', 'Legendary',
 'Straight or Storm has 50% chance to skip enemy next turn.',
 '스트레이트 또는 스톰 완성 시 50% 확률로 적 다음 턴 스킵.',
 'ストレートまたはストーム完成時に50%の確率で敵の次のターンをスキップ。',
 '顺子或风暴完成时50%几率跳过敌人下回合。',
 'AFTER_DAMAGE',
 '{"type": "skip_turn", "condition": ["Straight", "Storm"], "chance": 0.5}',
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
 'Start battle with a shield that absorbs 18 damage.',
 '전투 시작 시 18 데미지를 흡수하는 보호막.',
 '戦闘開始時に18ダメージを吸収するシールド。',
 '战斗开始时获得吸收18伤害的护盾。',
 'BATTLE_START',
 '{"type": "shield", "amount": 18}',
 15),

('ace_hunter', 'Ace Hunter', '에이스 헌터', 'エースハンター', '王牌猎手', 'Legendary',
 'Ace [1-1-1] deals +18 damage (total 63).',
 '에이스 [1-1-1] 완성 시 공격력 +18 (총 63).',
 'エース[1-1-1]完成時に攻撃力+18（合計63）。',
 'Ace[1-1-1]额外造成+18伤害（总计63）。',
 'BEFORE_DAMAGE',
 '{"type": "damage_bonus", "condition": "Ace", "bonus": 18}',
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
 'All 6s in your roll deal double damage.',
 '굴림에서 모든 6은 2배 데미지.',
 'ロールで全ての6は2倍ダメージ。',
 '掷骰中所有6造成2倍伤害。',
 'BEFORE_DAMAGE',
 '{"type": "six_bonus", "multiplier": 2}',
 15),

('chaos_lord', 'Chaos Lord', '혼돈의 군주', '混沌の君主', '混沌之主', 'Legendary',
 'Randomly apply one of: 1.5x damage, heal 10, skip enemy turn.',
 '랜덤하게 1.5배 데미지, 10 회복, 적 턴 스킵 중 하나 발동.',
 'ランダムで1.5倍ダメージ、10回復、敵ターンスキップのいずれかを発動。',
 '随机触发：1.5倍伤害、回复10、跳过敌人回合其一。',
 'AFTER_DAMAGE',
 '{"type": "random_effect", "effects": ["1.5x_damage", "heal_10", "skip_turn"]}',
 15),

('quantum_dice', 'Quantum Dice', '양자 주사위', '量子ダイス', '量子骰子', 'Legendary',
 'Roll results exist in superposition - choose outcome after seeing enemy roll.',
 '굴림 결과가 중첩 상태 - 적 굴림 확인 후 결과 선택.',
 'ロール結果が重ね合わせ状態 - 敵のロールを確認後に結果を選択。',
 '掷骰结果处于叠加态 - 看到敌人结果后选择。',
 'DICE_ROLL',
 '{"type": "delayed_choice", "options": 2}',
 15),

('absolute_zero', 'Absolute Zero', '절대 영도', '絶対零度', '绝对零度', 'Legendary',
 'Once per battle, nullify all damage from one attack.',
 '전투당 1회, 공격 1회의 데미지를 완전 무효화.',
 '戦闘ごとに1回、攻撃1回のダメージを完全無効化。',
 '每场战斗1次，完全无效化1次攻击伤害。',
 'PASSIVE',
 '{"type": "nullify", "uses": 1}',
 15),

('omega_strike', 'Omega Strike', '오메가 스트라이크', 'オメガストライク', '欧米伽打击', 'Legendary',
 'If you win the turn by 30+ damage difference, enemy loses next turn.',
 '30 이상의 데미지 차이로 턴 승리 시 적 다음 턴 상실.',
 '30以上のダメージ差でターン勝利時、敵は次のターンを失う。',
 '伤害差超过30获胜时敌人失去下回合。',
 'AFTER_DAMAGE',
 '{"type": "overwhelming_victory", "threshold": 30}',
 15),

('phoenix_rebirth', 'Phoenix Rebirth', '불사조의 부활', 'フェニックスリバース', '凤凰重生', 'Legendary',
 'Once per battle, revive with 25 HP when reduced to 0.',
 '전투당 1회, HP가 0이 되면 25 HP로 부활.',
 '戦闘ごとに1回、HPが0になると25HPで復活。',
 '每场战斗1次，HP降至0时以25HP复活。',
 'PASSIVE',
 '{"type": "revive", "hp": 25, "uses": 1}',
 15),

('gambler_supreme', 'Gambler Supreme', '도박의 제왕', 'ギャンブラースプリーム', '赌神', 'Legendary',
 'All dice roll twice - you choose the better result for each.',
 '모든 주사위를 두 번 굴리고 각각 더 좋은 결과 선택.',
 '全てのサイコロを2回振り、それぞれ良い方を選択。',
 '所有骰子掷两次，各选较好结果。',
 'DICE_ROLL',
 '{"type": "advantage", "rolls": 2}',
 15);

-- =====================================================
-- 2. 층 정보 데이터 (Floors)
-- =====================================================
INSERT INTO floors (id, floor_type, battle_count, boss_id, ai_level, enemy_skill_count, skill_reward_rarity, description_ko, description_en, description_ja, description_zh) VALUES
(1,  'NORMAL', 3, NULL,       0, 0, NULL,        '로비 - 첫 번째 시험',        'Lobby - First Trial',                   'ロビー - 最初の試練',             '大堂 - 第一场试炼'),
(2,  'NORMAL', 3, NULL,       0, 0, NULL,        '카지노 홀',                   'Casino Hall',                           'カジノホール',                    '赌场大厅'),
(3,  'NORMAL', 4, NULL,       0, 0, NULL,        'VIP 라운지',                  'VIP Lounge',                            'VIPラウンジ',                     'VIP休息室'),
(4,  'ELITE',  1, NULL,       1, 1, NULL,        '경비실 - 엘리트 경비원',      'Security Room - Elite Guard',           '警備室 - エリート警備員',         '警卫室 - 精英警卫'),
(5,  'BOSS',   1, 'mammon',   1, 2, 'Rare',      '금고 - 탐욕의 마몬',          'Vault - Mammon the Greedy',             '金庫 - 強欲のマモン',             '金库 - 贪婪的玛门'),
(6,  'NORMAL', 3, NULL,       1, 1, NULL,        '균열의 복도',                 'Cracked Corridor',                      '亀裂の廊下',                      '裂缝走廊'),
(7,  'NORMAL', 4, NULL,       1, 1, NULL,        '거울의 방',                   'Hall of Mirrors',                       '鏡の間',                          '镜之厅'),
(8,  'NORMAL', 3, NULL,       1, 1, NULL,        '왜곡된 객실',                 'Distorted Rooms',                       '歪んだ客室',                      '扭曲的客房'),
(9,  'ELITE',  1, NULL,       2, 2, NULL,        '경비대장실 - 엘리트 대장',    'Captain''s Quarters - Elite Captain',    '警備隊長室 - エリート隊長',       '队长室 - 精英队长'),
(10, 'BOSS',   1, 'eligor',   2, 3, 'Epic',      '방어의 탑 - 철벽의 엘리고르', 'Tower of Defense - Eligor the Ironclad', '防御の塔 - 鉄壁のエリゴール',     '防御之塔 - 铁壁埃力格'),
(11, 'NORMAL', 4, NULL,       2, 2, NULL,        '차원의 틈',                   'Dimensional Rift',                      '次元の裂け目',                    '维度裂缝'),
(12, 'ELITE',  1, NULL,       2, 3, NULL,        '공허의 감시자',               'Void Watcher',                          '虚空の監視者',                    '虚空监视者'),
(13, 'NORMAL', 4, NULL,       2, 2, NULL,        '무한의 계단',                 'Infinite Stairway',                     '無限の階段',                      '无限阶梯'),
(14, 'ELITE',  1, NULL,       3, 3, NULL,        '운명의 심판관',               'Judge of Fate',                         '運命の審判者',                    '命运审判官'),
(15, 'BOSS',   1, 'lucifuge', 3, 4, 'Legendary', '펜트하우스 - 루시푸지',       'Penthouse - Lucifuge Rofocale',         'ペントハウス - ルシフージュ',     '顶层 - 路西弗格·罗佛卡勒');

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
   "intro_en": "You want to climb higher? Then bet EVERYTHING!",
   "phase2_en": "You''ve forced my hand... ALL IN!",
   "defeat_en": "The house... always wins... or so I thought...",
   "intro_ko": "더 올라가고 싶다고? 그럼 전부 걸어!",
   "phase2_ko": "네가 이렇게 만들었다... 올인!",
   "defeat_ko": "하우스가... 항상 이긴다고... 그렇게 생각했는데...",
   "intro_ja": "もっと上に行きたい？ なら全てを賭けろ！",
   "phase2_ja": "お前がこうさせた…オールイン！",
   "defeat_ja": "ハウスが…常に勝つと…そう思っていたのに…",
   "intro_zh": "想往上爬？那就赌上一切！",
   "phase2_zh": "是你逼我的……全押！",
   "defeat_zh": "庄家……永远赢……我是这么以为的……"
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
   "intro_en": "Your attacks are futile against my armor.",
   "phase2_en": "You''ve cracked my shell... Now feel my wrath!",
   "defeat_en": "Impossible... my defense was absolute...",
   "intro_ko": "네 공격은 내 갑옷에 소용없다.",
   "phase2_ko": "내 껍질을 깼군... 이제 분노를 느껴라!",
   "defeat_ko": "불가능해... 내 방어는 완벽했는데...",
   "intro_ja": "お前の攻撃は我が鎧には無力だ。",
   "phase2_ja": "我が殻を破ったか…さあ、怒りを味わえ！",
   "defeat_ja": "馬鹿な…我が防御は完璧だったはず…",
   "intro_zh": "你的攻击对我的铠甲毫无用处。",
   "phase2_zh": "你破了我的壳……现在感受我的愤怒吧！",
   "defeat_zh": "不可能……我的防御是完美的……"
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
   "intro_en": "Welcome, wanderer. Let us test your resolve.",
   "phase2_en": "Impressive. But you''ve seen nothing yet.",
   "phase3_en": "Behold... the true nature of fate.",
   "defeat_en": "You... defy probability itself. Remarkable.",
   "intro_ko": "어서 오게, 방랑자여. 네 결의를 시험하지.",
   "phase2_ko": "인상적이군. 하지만 아직 아무것도 보지 못했어.",
   "phase3_ko": "보아라... 운명의 진정한 본질을.",
   "defeat_ko": "너는... 확률 자체를 거스르는군. 놀랍군.",
   "intro_ja": "ようこそ、放浪者よ。お前の覚悟を試そう。",
   "phase2_ja": "見事だ。だが、まだ何も見ていない。",
   "phase3_ja": "見よ…運命の真の姿を。",
   "defeat_ja": "お前は…確率そのものに逆らうのか。驚嘆に値する。",
   "intro_zh": "欢迎，流浪者。让我们来考验你的决心。",
   "phase2_zh": "令人印象深刻。但你还什么都没看到。",
   "phase3_zh": "看吧……命运的真正本质。",
   "defeat_zh": "你……违抗了概率本身。了不起。"
 }');

-- =====================================================
-- 4. 초기 PvP 시즌 데이터
-- =====================================================
INSERT INTO pvp_seasons (season_name_en, season_name_ko, season_name_ja, season_name_zh, start_date, end_date, is_active) VALUES
('Season 1 - Grand Opening', '시즌 1 - 그랜드 오프닝', 'シーズン1 - グランドオープニング', '赛季1 - 盛大开幕', '2026-01-01', '2026-03-31', TRUE);

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
