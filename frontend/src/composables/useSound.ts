import { watch } from 'vue'
import { Howl, Howler } from 'howler'
import { useSettingsStore } from '@/stores/settings'

/**
 * SoundManager - Howler.js-based sound system
 * Follows PROJECTPLAN.md Chapter 11 Sound Design specifications
 *
 * BGM: Crossfade + looping
 * SFX: Sound effects (dice, hands, combat, UI)
 * Web Audio API: Works with synthesized sounds even without asset files
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
  /** Dice roll start - rolling sound on wooden table */
  diceRoll() {
    playNoise(0.8, 0.2)
    // Multiple short taps to simulate bouncing
    for (let i = 0; i < 5; i++) {
      setTimeout(() => {
        playTone(200 + Math.random() * 300, 0.05, 'square', 0.1)
      }, i * 80 + Math.random() * 40)
    }
  },

  /** Dice landing - stopping sound */
  diceLand() {
    playTone(350, 0.15, 'triangle', 0.2)
    playNoise(0.1, 0.15)
  },

  /** Ace hand complete - sparkling magic sound */
  aceComplete() {
    const notes = [1047, 1319, 1568, 2093] // C6, E6, G6, C7
    notes.forEach((freq, i) => {
      setTimeout(() => playTone(freq, 0.6 - i * 0.1, 'sine', 0.25), i * 150)
    })
  },

  /** Triple hand complete - explosive sound */
  tripleComplete() {
    playTone(880, 0.5, 'sawtooth', 0.2)
    setTimeout(() => playTone(1100, 0.4, 'sine', 0.25), 100)
    setTimeout(() => playTone(1320, 0.3, 'sine', 0.2), 200)
  },

  /** Straight/Strike/Slash hand complete - ascending scale */
  straightComplete() {
    const notes = [523, 659, 784] // C5, E5, G5
    notes.forEach((freq, i) => {
      setTimeout(() => playTone(freq, 0.3, 'sine', 0.2), i * 120)
    })
  },

  /** Storm hand complete - wind + thunder */
  stormComplete() {
    playNoise(0.8, 0.2)
    setTimeout(() => playTone(80, 0.5, 'sawtooth', 0.3), 200)
    setTimeout(() => playNoise(0.3, 0.25), 400)
  },

  /** Pair hand complete - soft bell sound */
  pairComplete() {
    playTone(659, 0.4, 'sine', 0.2) // E5
    setTimeout(() => playTone(784, 0.3, 'sine', 0.15), 100) // G5
  },

  /** No hand - disappointment sound */
  noHandComplete() {
    playTone(262, 0.3, 'sine', 0.15) // C4
    setTimeout(() => playTone(220, 0.4, 'sine', 0.12), 150) // A3 (descending)
  },

  /** Damage dealt - hit sound */
  damageDealt() {
    playTone(150, 0.15, 'square', 0.2)
    playNoise(0.1, 0.2)
  },

  /** Damage taken sound */
  damageTaken() {
    playTone(100, 0.2, 'sawtooth', 0.15)
    setTimeout(() => playNoise(0.15, 0.1), 50)
  },

  /** Victory fanfare */
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

  /** Defeat sound */
  defeat() {
    const notes = [440, 392, 349, 262] // A4, G4, F4, C4 (descending)
    notes.forEach((freq, i) => {
      setTimeout(() => playTone(freq, 0.5, 'sine', 0.2), i * 250)
    })
  },

  /** Skill activation (by rarity) */
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

  /** UI button click */
  buttonClick() {
    playTone(800, 0.06, 'sine', 0.1)
  },

  /** UI button hover */
  buttonHover() {
    playTone(600, 0.04, 'sine', 0.05)
  },

  /** Turn change sound */
  turnChange() {
    playTone(440, 0.15, 'triangle', 0.12)
    setTimeout(() => playTone(550, 0.12, 'triangle', 0.1), 100)
  },

  /** Timer warning (10 seconds or less) */
  timerWarning() {
    playTone(880, 0.1, 'square', 0.15)
  },

  /** Phase transition */
  phaseTransition() {
    playNoise(0.5, 0.2)
    setTimeout(() => {
      playTone(200, 0.8, 'sawtooth', 0.2)
      playTone(400, 0.6, 'sine', 0.15)
    }, 300)
    setTimeout(() => playTone(800, 0.5, 'sine', 0.2), 800)
  },

  /** Auto-play sound matching the hand rank */
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

/** BGM key to file path mapping (used when asset files exist) */
const BGM_MAP: Record<string, string> = {
  menu: '/sounds/bgm/menu.mp3',
  'floor-1-5': '/sounds/bgm/floor_1_5.mp3',
  'floor-6-10': '/sounds/bgm/floor_6_10.mp3',
  'floor-11-15': '/sounds/bgm/floor_11_15.mp3',
  boss: '/sounds/bgm/boss.mp3'
}

export const BGM = {
  /**
   * Play BGM (with crossfade)
   * Silently ignored if asset file doesn't exist (no error)
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

  /** Stop BGM */
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

  /** Update BGM volume */
  setVolume(volume: number) {
    if (currentBgm) {
      currentBgm.volume(volume / 100)
    }
  },

  /** Get current BGM key */
  getCurrentKey(): string | null {
    return currentBgmKey
  }
}

// --- Global Volume Sync ---

/**
 * useSound - Sound system initialization composable
 * Call once in App.vue to watch for settings changes
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
