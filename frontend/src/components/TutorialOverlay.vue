<template>
  <Teleport to="body">
    <Transition name="overlay">
      <div v-if="visible" class="tutorial-overlay">
        <!-- Dimmed background with cutout for highlighted element -->
        <div class="overlay-backdrop" @click="handleBackdropClick"></div>

        <!-- Highlight ring around target element -->
        <div
          v-if="highlightRect"
          class="highlight-ring"
          :style="highlightStyle"
        ></div>

        <!-- Speech bubble (Lucifuge dialogue) -->
        <div
          v-if="dialogueText"
          class="speech-bubble"
          :class="[`bubble-${bubblePosition}`, { 'is-lucifuge': isLucifuge }]"
          :style="bubbleStyle"
        >
          <!-- Character name -->
          <div v-if="speakerName" class="speaker-name">{{ speakerName }}</div>

          <!-- Dialogue text with typewriter effect -->
          <p class="dialogue-text">{{ displayedText }}</p>

          <!-- Continue prompt -->
          <div v-if="showContinue" class="continue-prompt" @click="$emit('next')">
            {{ $t('tutorial.tapToContinue') }}
            <span class="blink-arrow">▶</span>
          </div>
        </div>

        <!-- Skip button -->
        <button
          v-if="showSkipBtn"
          class="skip-btn"
          @click="$emit('skip')"
        >
          {{ $t('tutorial.skip') }}
        </button>

        <!-- Progress bar -->
        <div v-if="progress != null && progress >= 0" class="tutorial-progress">
          <div class="progress-fill" :style="{ width: `${progress}%` }"></div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'

useI18n()

const props = defineProps<{
  visible: boolean
  dialogueText?: string
  speakerName?: string
  isLucifuge?: boolean
  highlightSelector?: string
  bubblePosition?: 'top' | 'bottom' | 'left' | 'right'
  showContinue?: boolean
  showSkipBtn?: boolean
  progress?: number
}>()

const emit = defineEmits<{
  next: []
  skip: []
}>()

// Typewriter effect
const displayedText = ref('')
const typewriterTimer = ref<ReturnType<typeof setInterval> | null>(null)

watch(
  () => props.dialogueText,
  (newText) => {
    if (!newText) {
      displayedText.value = ''
      return
    }
    // Start typewriter
    displayedText.value = ''
    let charIndex = 0
    if (typewriterTimer.value) clearInterval(typewriterTimer.value)

    typewriterTimer.value = setInterval(() => {
      if (charIndex < newText.length) {
        displayedText.value += newText[charIndex]
        charIndex++
      } else {
        if (typewriterTimer.value) clearInterval(typewriterTimer.value)
      }
    }, 30) // 30ms per character
  },
  { immediate: true }
)

// Highlight element position tracking
const highlightRect = ref<DOMRect | null>(null)
const resizeObserver = ref<ResizeObserver | null>(null)

function updateHighlightRect() {
  if (!props.highlightSelector) {
    highlightRect.value = null
    return
  }

  nextTick(() => {
    const el = document.querySelector(props.highlightSelector!)
    if (el) {
      highlightRect.value = el.getBoundingClientRect()
    } else {
      highlightRect.value = null
    }
  })
}

watch(() => props.highlightSelector, updateHighlightRect, { immediate: true })

const highlightStyle = computed(() => {
  if (!highlightRect.value) return {}
  const padding = 8
  return {
    top: `${highlightRect.value.top - padding}px`,
    left: `${highlightRect.value.left - padding}px`,
    width: `${highlightRect.value.width + padding * 2}px`,
    height: `${highlightRect.value.height + padding * 2}px`
  }
})

const bubbleStyle = computed(() => {
  if (!highlightRect.value) {
    // Center the bubble if no highlight target
    return {
      left: '50%',
      top: '50%',
      transform: 'translate(-50%, -50%)'
    }
  }

  const rect = highlightRect.value
  const pos = props.bubblePosition || 'top'

  switch (pos) {
    case 'top':
      return {
        left: `${rect.left + rect.width / 2}px`,
        top: `${rect.top - 16}px`,
        transform: 'translate(-50%, -100%)'
      }
    case 'bottom':
      return {
        left: `${rect.left + rect.width / 2}px`,
        top: `${rect.bottom + 16}px`,
        transform: 'translate(-50%, 0)'
      }
    case 'left':
      return {
        left: `${rect.left - 16}px`,
        top: `${rect.top + rect.height / 2}px`,
        transform: 'translate(-100%, -50%)'
      }
    case 'right':
      return {
        left: `${rect.right + 16}px`,
        top: `${rect.top + rect.height / 2}px`,
        transform: 'translate(0, -50%)'
      }
    default:
      return {}
  }
})

function handleBackdropClick() {
  if (props.showContinue) {
    emit('next')
  }
}

// Track window resize to update highlight position
onMounted(() => {
  window.addEventListener('resize', updateHighlightRect)
  window.addEventListener('scroll', updateHighlightRect)

  resizeObserver.value = new ResizeObserver(updateHighlightRect)
  resizeObserver.value.observe(document.body)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateHighlightRect)
  window.removeEventListener('scroll', updateHighlightRect)

  if (typewriterTimer.value) clearInterval(typewriterTimer.value)
  if (resizeObserver.value) resizeObserver.value.disconnect()
})
</script>

<style scoped>
.tutorial-overlay {
  position: fixed;
  inset: 0;
  z-index: 9000;
  pointer-events: none;
}

.overlay-backdrop {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  pointer-events: auto;
}

/* Highlight ring around target element */
.highlight-ring {
  position: fixed;
  border: 3px solid #d4af37;
  border-radius: 12px;
  box-shadow:
    0 0 0 4000px rgba(0, 0, 0, 0.6),
    0 0 20px rgba(212, 175, 55, 0.5),
    inset 0 0 20px rgba(212, 175, 55, 0.2);
  z-index: 9001;
  pointer-events: none;
  animation: pulse-ring 2s ease-in-out infinite;
}

@keyframes pulse-ring {
  0%, 100% {
    box-shadow:
      0 0 0 4000px rgba(0, 0, 0, 0.6),
      0 0 20px rgba(212, 175, 55, 0.5),
      inset 0 0 20px rgba(212, 175, 55, 0.2);
  }
  50% {
    box-shadow:
      0 0 0 4000px rgba(0, 0, 0, 0.6),
      0 0 40px rgba(212, 175, 55, 0.8),
      inset 0 0 30px rgba(212, 175, 55, 0.3);
  }
}

/* Speech bubble */
.speech-bubble {
  position: fixed;
  max-width: 420px;
  min-width: 280px;
  padding: 20px 24px;
  background: linear-gradient(135deg, rgba(27, 27, 39, 0.98) 0%, rgba(40, 40, 60, 0.98) 100%);
  border: 2px solid rgba(212, 175, 55, 0.5);
  border-radius: 16px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.5),
    0 0 20px rgba(212, 175, 55, 0.2);
  z-index: 9002;
  pointer-events: auto;
}

.speech-bubble.is-lucifuge {
  border-color: #d4af37;
  background: linear-gradient(135deg, rgba(20, 10, 30, 0.98) 0%, rgba(40, 20, 50, 0.98) 100%);
}

.speaker-name {
  font-size: 14px;
  font-weight: 700;
  color: #d4af37;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 8px;
}

.dialogue-text {
  margin: 0;
  font-size: 16px;
  line-height: 1.6;
  color: #fffdd0;
  white-space: pre-line;
}

.continue-prompt {
  margin-top: 12px;
  font-size: 13px;
  color: rgba(212, 175, 55, 0.7);
  text-align: right;
  cursor: pointer;
  transition: color 0.2s;
}

.continue-prompt:hover {
  color: #d4af37;
}

.blink-arrow {
  display: inline-block;
  animation: blink 1s ease-in-out infinite;
  margin-left: 4px;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

/* Skip button */
.skip-btn {
  position: fixed;
  top: 16px;
  right: 16px;
  padding: 8px 20px;
  background: rgba(100, 100, 100, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  cursor: pointer;
  z-index: 9003;
  pointer-events: auto;
  transition: all 0.2s;
}

.skip-btn:hover {
  background: rgba(100, 100, 100, 0.6);
  color: #ffffff;
}

/* Progress bar */
.tutorial-progress {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  z-index: 9003;
  pointer-events: none;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #d4af37, #e4bf47);
  transition: width 0.5s ease;
}

/* Transition animations */
.overlay-enter-active,
.overlay-leave-active {
  transition: opacity 0.4s ease;
}

.overlay-enter-from,
.overlay-leave-to {
  opacity: 0;
}

/* Responsive */
@media (max-width: 480px) {
  .speech-bubble {
    max-width: calc(100vw - 32px);
    min-width: 240px;
    padding: 16px 20px;
  }

  .dialogue-text {
    font-size: 14px;
  }
}
</style>
