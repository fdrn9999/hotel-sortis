import { ref, computed, readonly } from 'vue'

/**
 * useTutorial - Tutorial Manager (Floor 0: The Lobby)
 *
 * PROJECTPLAN.md 16.1.1 spec compliance:
 * - Step-based tutorial with dialogue
 * - Forced skill usage
 * - Guaranteed player victory
 * - Hand education popups
 */

export type TutorialStepType =
  | 'dialogue'          // Lucifuge speech bubble
  | 'highlight'         // Highlight a UI element
  | 'wait_roll'         // Wait for player to roll dice
  | 'show_hand_result'  // Show hand result explanation
  | 'force_skill'       // Force a skill usage demonstration
  | 'wait_continue'     // Wait for player to click "continue"
  | 'enemy_turn'        // Show enemy rolling
  | 'battle_end'        // Battle result

export interface TutorialStep {
  id: string
  type: TutorialStepType
  /** i18n key for dialogue text */
  dialogueKey?: string
  /** CSS selector or ref name to highlight */
  highlightTarget?: string
  /** Position of speech bubble relative to highlight */
  bubblePosition?: 'top' | 'bottom' | 'left' | 'right'
  /** Auto-advance after delay (ms). 0 = wait for user */
  autoAdvanceMs?: number
  /** Callback action when step activates */
  onEnter?: () => void
  /** Callback when step completes */
  onExit?: () => void
}

// Tutorial step definitions for Floor 0
export function createTutorialSteps(): TutorialStep[] {
  return [
    // --- Phase 1: Introduction ---
    {
      id: 'welcome',
      type: 'dialogue',
      dialogueKey: 'tutorial.lucifuge.welcome',
      autoAdvanceMs: 0
    },
    {
      id: 'explain_hotel',
      type: 'dialogue',
      dialogueKey: 'tutorial.lucifuge.explainHotel',
      autoAdvanceMs: 0
    },
    {
      id: 'explain_dice',
      type: 'dialogue',
      dialogueKey: 'tutorial.lucifuge.explainDice',
      autoAdvanceMs: 0
    },

    // --- Phase 2: First Roll ---
    {
      id: 'highlight_roll_btn',
      type: 'highlight',
      dialogueKey: 'tutorial.guide.rollInstruction',
      highlightTarget: '.roll-btn',
      bubblePosition: 'top'
    },
    {
      id: 'first_roll',
      type: 'wait_roll',
      dialogueKey: 'tutorial.guide.rollNow'
    },
    {
      id: 'first_hand_result',
      type: 'show_hand_result',
      dialogueKey: 'tutorial.guide.handExplanation',
      autoAdvanceMs: 0
    },
    {
      id: 'lucifuge_react_1',
      type: 'dialogue',
      dialogueKey: 'tutorial.lucifuge.firstReaction',
      autoAdvanceMs: 0
    },

    // --- Phase 3: Enemy Turn Demo ---
    {
      id: 'enemy_turn_explain',
      type: 'dialogue',
      dialogueKey: 'tutorial.guide.enemyTurnExplain',
      autoAdvanceMs: 0
    },
    {
      id: 'enemy_turn_1',
      type: 'enemy_turn',
      autoAdvanceMs: 2000
    },

    // --- Phase 4: Hand Guide ---
    {
      id: 'highlight_hand_guide',
      type: 'highlight',
      dialogueKey: 'tutorial.guide.handGuideIntro',
      highlightTarget: '.hand-guide-btn',
      bubblePosition: 'bottom'
    },
    {
      id: 'explain_rankings',
      type: 'dialogue',
      dialogueKey: 'tutorial.guide.handRankings',
      autoAdvanceMs: 0
    },

    // --- Phase 5: Second Roll ---
    {
      id: 'second_roll_prompt',
      type: 'highlight',
      dialogueKey: 'tutorial.guide.rollAgain',
      highlightTarget: '.roll-btn',
      bubblePosition: 'top'
    },
    {
      id: 'second_roll',
      type: 'wait_roll',
      dialogueKey: 'tutorial.guide.rollNow'
    },
    {
      id: 'second_hand_result',
      type: 'show_hand_result',
      dialogueKey: 'tutorial.guide.handExplanation',
      autoAdvanceMs: 0
    },

    // --- Phase 6: Skill Introduction ---
    {
      id: 'skill_intro',
      type: 'dialogue',
      dialogueKey: 'tutorial.lucifuge.skillIntro',
      autoAdvanceMs: 0
    },
    {
      id: 'force_skill',
      type: 'force_skill',
      dialogueKey: 'tutorial.guide.useSkill',
      autoAdvanceMs: 0
    },

    // --- Phase 7: Enemy Turn 2 ---
    {
      id: 'enemy_turn_2',
      type: 'enemy_turn',
      autoAdvanceMs: 2000
    },

    // --- Phase 8: Third Roll (guaranteed strong hand) ---
    {
      id: 'third_roll_prompt',
      type: 'highlight',
      dialogueKey: 'tutorial.guide.finalRoll',
      highlightTarget: '.roll-btn',
      bubblePosition: 'top'
    },
    {
      id: 'third_roll',
      type: 'wait_roll',
      dialogueKey: 'tutorial.guide.rollNow'
    },
    {
      id: 'third_hand_result',
      type: 'show_hand_result',
      dialogueKey: 'tutorial.guide.handExplanation',
      autoAdvanceMs: 0
    },

    // --- Phase 9: Victory ---
    {
      id: 'battle_end',
      type: 'battle_end',
      dialogueKey: 'tutorial.lucifuge.defeat',
      autoAdvanceMs: 0
    },
    {
      id: 'farewell',
      type: 'dialogue',
      dialogueKey: 'tutorial.lucifuge.farewell',
      autoAdvanceMs: 0
    }
  ]
}

// --- Pre-scripted dice results for tutorial ---

export interface TutorialDiceScript {
  playerDice: [number, number, number]
  enemyDice: [number, number, number]
}

/**
 * Tutorial is scripted: player always wins.
 * Roll 1: Player gets Pair, Enemy gets NoHand (player learns basics)
 * Roll 2: Player gets Storm after skill demo, Enemy gets Pair (introducing skills)
 * Roll 3: Player gets Straight (finishing blow), Enemy already low HP
 */
export const TUTORIAL_DICE_SCRIPTS: TutorialDiceScript[] = [
  {
    // Roll 1: Player gets Pair-4 (13 dmg), Enemy gets NoHand [1,3,5] (9 dmg)
    playerDice: [4, 4, 2],
    enemyDice: [1, 3, 5]
  },
  {
    // Roll 2: Player gets Storm [1,2,3] (16 dmg), Enemy gets Pair-2 (9 dmg)
    playerDice: [1, 2, 3],
    enemyDice: [2, 2, 5]
  },
  {
    // Roll 3: Player gets Straight [4,5,6] (38 dmg) - guaranteed victory
    playerDice: [4, 5, 6],
    enemyDice: [1, 4, 6] // NoHand (11 dmg) - won't matter
  }
]

// --- Composable ---

export function useTutorial() {
  const steps = ref<TutorialStep[]>(createTutorialSteps())
  const currentStepIndex = ref(0)
  const isActive = ref(false)
  const isPaused = ref(false)
  const rollCount = ref(0)
  const tutorialComplete = ref(false)

  // Player/Enemy HP for tutorial battle
  const playerHP = ref(100)
  const enemyHP = ref(100)

  const currentStep = computed(() => {
    if (!isActive.value || currentStepIndex.value >= steps.value.length) return null
    return steps.value[currentStepIndex.value]
  })

  const progress = computed(() => {
    if (steps.value.length === 0) return 0
    return Math.round((currentStepIndex.value / steps.value.length) * 100)
  })

  const isLastStep = computed(() => {
    return currentStepIndex.value >= steps.value.length - 1
  })

  function start() {
    currentStepIndex.value = 0
    isActive.value = true
    isPaused.value = false
    rollCount.value = 0
    tutorialComplete.value = false
    playerHP.value = 100
    enemyHP.value = 100
  }

  function nextStep() {
    if (currentStepIndex.value < steps.value.length - 1) {
      const oldStep = steps.value[currentStepIndex.value]
      if (oldStep.onExit) oldStep.onExit()

      currentStepIndex.value++

      const newStep = steps.value[currentStepIndex.value]
      if (newStep.onEnter) newStep.onEnter()

      // Auto-advance if configured
      if (newStep.autoAdvanceMs && newStep.autoAdvanceMs > 0) {
        setTimeout(() => {
          if (currentStep.value?.id === newStep.id) {
            nextStep()
          }
        }, newStep.autoAdvanceMs)
      }
    } else {
      complete()
    }
  }

  function skipToStep(stepId: string) {
    const idx = steps.value.findIndex(s => s.id === stepId)
    if (idx !== -1) {
      currentStepIndex.value = idx
    }
  }

  function complete() {
    isActive.value = false
    tutorialComplete.value = true
    // Save completion to localStorage
    localStorage.setItem('tutorial_completed', 'true')
  }

  function skip() {
    complete()
  }

  /** Get scripted dice for current roll */
  function getScriptedDice(): TutorialDiceScript | null {
    if (rollCount.value >= TUTORIAL_DICE_SCRIPTS.length) {
      return null
    }
    return TUTORIAL_DICE_SCRIPTS[rollCount.value]
  }

  /** Record a roll happened */
  function recordRoll() {
    rollCount.value++
  }

  /** Apply damage from scripted roll */
  function applyDamage(playerDamage: number, enemyDamage: number) {
    playerHP.value = Math.max(0, playerHP.value - playerDamage)
    enemyHP.value = Math.max(0, enemyHP.value - enemyDamage)
  }

  /** Check if tutorial was already completed */
  function isCompleted(): boolean {
    return localStorage.getItem('tutorial_completed') === 'true'
  }

  return {
    // State
    steps: readonly(steps),
    currentStepIndex: readonly(currentStepIndex),
    isActive: readonly(isActive),
    isPaused: readonly(isPaused),
    rollCount: readonly(rollCount),
    tutorialComplete: readonly(tutorialComplete),
    playerHP: readonly(playerHP),
    enemyHP: readonly(enemyHP),

    // Computed
    currentStep,
    progress,
    isLastStep,

    // Methods
    start,
    nextStep,
    skipToStep,
    complete,
    skip,
    getScriptedDice,
    recordRoll,
    applyDamage,
    isCompleted
  }
}
