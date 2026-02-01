/**
 * useConfirmModal - Confirm 모달 시스템
 *
 * CLAUDE.md 규칙 준수: confirm() 대체
 */

export interface ConfirmModalOptions {
  title: string
  message: string
  confirmText?: string
  cancelText?: string
}

/**
 * Confirm 모달을 표시하는 composable
 *
 * @example
 * const { confirm } = useConfirmModal()
 *
 * const result = await confirm({
 *   title: '로그아웃',
 *   message: '정말 로그아웃하시겠습니까?',
 *   confirmText: '로그아웃',
 *   cancelText: '취소'
 * })
 *
 * if (result) {
 *   // 확인 버튼 클릭
 *   logout()
 * } else {
 *   // 취소 버튼 클릭
 * }
 */
export function useConfirmModal() {
  /**
   * Confirm 모달 표시 (Promise 기반)
   */
  const confirm = (options: ConfirmModalOptions): Promise<boolean> => {
    return new Promise((resolve) => {
      // CustomEvent를 사용해 ConfirmModal 컴포넌트로 전달
      window.dispatchEvent(
        new CustomEvent('show-confirm', {
          detail: {
            ...options,
            resolver: resolve
          }
        })
      )
    })
  }

  return {
    confirm
  }
}
