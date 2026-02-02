import { ref } from 'vue'
import type { DiceSkin } from '@/types/game'

export interface DiceResult {
  die1: number
  die2: number
  die3: number
}

/**
 * Vue-based dice roller composable
 * Replaces the Three.js + Cannon-es based useDiceScene
 *
 * Provides the same API as useDiceScene for easy migration:
 * - roll(): Promise<DiceResult> - random dice roll (practice mode)
 * - rollTo(targetValues): Promise<DiceResult> - server-synchronized roll (battle mode)
 * - isRolling: Ref<boolean> - rolling state
 * - lastResult: Ref<DiceResult | null> - last roll result
 * - applySkin(skin): void - apply dice skin
 */
export function useDiceRoller() {
  const isRolling = ref(false)
  const lastResult = ref<DiceResult | null>(null)
  const currentResult = ref<DiceResult | null>(null)
  const currentSkin = ref<DiceSkin | null>(null)

  // Animation timing constants
  const ANIMATION_DURATION = 1200 // Total animation time in ms

  /**
   * Roll dice with random result (practice mode)
   * Not used in real battles - server generates dice
   */
  const roll = (): Promise<DiceResult> => {
    return new Promise((resolve) => {
      if (isRolling.value) {
        return
      }

      isRolling.value = true

      const result: DiceResult = {
        die1: Math.floor(Math.random() * 6) + 1,
        die2: Math.floor(Math.random() * 6) + 1,
        die3: Math.floor(Math.random() * 6) + 1
      }

      currentResult.value = result

      // Wait for animation to complete
      setTimeout(() => {
        isRolling.value = false
        lastResult.value = result
        resolve(result)
      }, ANIMATION_DURATION)
    })
  }

  /**
   * Roll dice to specific target values (battle mode)
   * Server provides the target values, animation shows the result
   * This maintains server authority over dice results
   */
  const rollTo = (targetValues: DiceResult): Promise<DiceResult> => {
    return new Promise((resolve) => {
      if (isRolling.value) {
        return
      }

      isRolling.value = true
      currentResult.value = targetValues

      // Wait for animation to complete
      setTimeout(() => {
        isRolling.value = false
        lastResult.value = targetValues
        resolve(targetValues)
      }, ANIMATION_DURATION)
    })
  }

  /**
   * Apply a dice skin
   */
  const applySkin = (skin: DiceSkin) => {
    currentSkin.value = skin
    console.log(`Applied dice skin: ${skin.name} (${skin.skinCode})`)
  }

  /**
   * Get current skin
   */
  const getCurrentSkin = (): DiceSkin | null => {
    return currentSkin.value
  }

  return {
    // State
    isRolling,
    lastResult,
    currentResult,
    currentSkin,

    // Methods
    roll,
    rollTo,
    applySkin,
    getCurrentSkin
  }
}
