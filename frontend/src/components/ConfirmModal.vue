<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="isOpen" class="modal-overlay" @click="handleOverlayClick">
        <div class="modal-container" @click.stop>
          <div class="modal-header">
            <h3 class="modal-title">{{ currentModal?.title }}</h3>
          </div>

          <div class="modal-body">
            <p class="modal-message">{{ currentModal?.message }}</p>
          </div>

          <div class="modal-footer">
            <button
              class="modal-btn cancel-btn"
              @click="handleCancel"
              :aria-label="currentModal?.cancelText || 'Cancel'"
            >
              {{ currentModal?.cancelText || $t('common.cancel') }}
            </button>
            <button
              class="modal-btn confirm-btn"
              @click="handleConfirm"
              :aria-label="currentModal?.confirmText || 'Confirm'"
            >
              {{ currentModal?.confirmText || $t('common.confirm') }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'

useI18n()

export interface ConfirmModalOptions {
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  resolver?: (value: boolean) => void
}

const isOpen = ref(false)
const currentModal = ref<ConfirmModalOptions | null>(null)

// 모달 열기
const openModal = (options: ConfirmModalOptions) => {
  currentModal.value = options
  isOpen.value = true
}

// 확인 버튼 클릭
const handleConfirm = () => {
  if (currentModal.value?.resolver) {
    currentModal.value.resolver(true)
  }
  closeModal()
}

// 취소 버튼 클릭
const handleCancel = () => {
  if (currentModal.value?.resolver) {
    currentModal.value.resolver(false)
  }
  closeModal()
}

// 오버레이 클릭 (모달 외부 클릭 시 취소)
const handleOverlayClick = () => {
  handleCancel()
}

// 모달 닫기
const closeModal = () => {
  isOpen.value = false
  currentModal.value = null
}

// 전역 이벤트 리스너
const handleConfirmEvent = (event: CustomEvent) => {
  openModal(event.detail)
}

// ESC 키로 닫기
const handleKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'Escape' && isOpen.value) {
    handleCancel()
  }
}

onMounted(() => {
  window.addEventListener('show-confirm', handleConfirmEvent as EventListener)
  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('show-confirm', handleConfirmEvent as EventListener)
  window.removeEventListener('keydown', handleKeyDown)
})

defineExpose({ openModal, closeModal })
</script>

<style scoped>
/* 오버레이 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
}

/* 모달 컨테이너 */
.modal-container {
  width: 100%;
  max-width: 480px;
  background: linear-gradient(135deg, rgba(var(--color-dark-navy-rgb), 0.98) 0%, rgba(40, 40, 60, 0.98) 100%);
  border: 3px solid var(--color-gold);
  border-radius: 16px;
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.6),
    0 0 40px rgba(var(--color-gold-rgb), 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

/* 헤더 */
.modal-header {
  padding: 24px 24px 16px;
  border-bottom: 2px solid rgba(var(--color-gold-rgb), 0.3);
  background: linear-gradient(to bottom, rgba(var(--color-gold-rgb), 0.1), transparent);
}

.modal-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--color-gold);
  text-align: center;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  letter-spacing: 0.5px;
}

/* 본문 */
.modal-body {
  padding: 24px;
}

.modal-message {
  margin: 0;
  font-size: 16px;
  line-height: 1.6;
  color: var(--color-cream);
  text-align: center;
  white-space: pre-line;
}

/* 푸터 */
.modal-footer {
  padding: 16px 24px 24px;
  display: flex;
  gap: 12px;
  justify-content: center;
}

/* 버튼 공통 */
.modal-btn {
  flex: 1;
  max-width: 160px;
  height: 48px;
  border: 2px solid;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  text-transform: uppercase;
  letter-spacing: 1px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.modal-btn:active {
  transform: translateY(2px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

/* 취소 버튼 */
.cancel-btn {
  background: rgba(100, 100, 100, 0.3);
  border-color: rgba(255, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.8);
}

.cancel-btn:hover {
  background: rgba(100, 100, 100, 0.5);
  border-color: rgba(255, 255, 255, 0.5);
  color: #ffffff;
}

/* 확인 버튼 */
.confirm-btn {
  background: linear-gradient(135deg, rgba(var(--color-gold-rgb), 0.3), rgba(var(--color-gold-rgb), 0.2));
  border-color: var(--color-gold);
  color: var(--color-gold);
}

.confirm-btn:hover {
  background: linear-gradient(135deg, rgba(var(--color-gold-rgb), 0.5), rgba(var(--color-gold-rgb), 0.3));
  border-color: #e4bf47;
  color: #e4bf47;
  box-shadow: 0 4px 16px rgba(var(--color-gold-rgb), 0.4);
}

/* 애니메이션 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-active .modal-container,
.modal-leave-active .modal-container {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-container {
  transform: scale(0.9) translateY(-20px);
  opacity: 0;
}

.modal-leave-to .modal-container {
  transform: scale(0.95) translateY(10px);
  opacity: 0;
}

/* 모바일 최적화 */
@media (max-width: 480px) {
  .modal-container {
    max-width: 100%;
    border-radius: 12px;
  }

  .modal-header {
    padding: 20px 20px 12px;
  }

  .modal-title {
    font-size: 20px;
  }

  .modal-body {
    padding: 20px;
  }

  .modal-message {
    font-size: 14px;
  }

  .modal-footer {
    padding: 12px 20px 20px;
    flex-direction: column;
  }

  .modal-btn {
    max-width: 100%;
    width: 100%;
  }
}
</style>
