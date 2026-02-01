import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth'
import type {
  SignupRequest,
  LoginRequest,
  AuthResponse,
  UserProfile,
  UpdateProfileRequest,
  ChangePasswordRequest
} from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref<string | null>(localStorage.getItem('auth_token'))
  const user = ref<UserProfile | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const isAuthenticated = computed(() => !!token.value)
  const userEmail = computed(() => user.value?.email || '')
  const username = computed(() => user.value?.username || '')
  const playerId = computed(() => user.value?.playerId || null)

  // Actions
  /**
   * 회원가입
   */
  async function signup(request: SignupRequest): Promise<void> {
    isLoading.value = true
    error.value = null

    try {
      const response: AuthResponse = await authApi.signup(request)

      // 토큰 저장
      token.value = response.token
      localStorage.setItem('auth_token', response.token)

      // 프로필 조회
      await fetchProfile()
    } catch (err: any) {
      error.value = err.message || 'Signup failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 로그인
   */
  async function login(request: LoginRequest): Promise<void> {
    isLoading.value = true
    error.value = null

    try {
      const response: AuthResponse = await authApi.login(request)

      // 토큰 저장
      token.value = response.token
      localStorage.setItem('auth_token', response.token)

      // 프로필 조회
      await fetchProfile()
    } catch (err: any) {
      error.value = err.message || 'Login failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 로그아웃
   */
  function logout(): void {
    token.value = null
    user.value = null
    localStorage.removeItem('auth_token')
  }

  /**
   * 프로필 조회
   */
  async function fetchProfile(): Promise<void> {
    if (!token.value) {
      return
    }

    isLoading.value = true
    error.value = null

    try {
      user.value = await authApi.getMyProfile()
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch profile'
      // 401 에러면 로그아웃
      if (err.message?.includes('401') || err.message?.includes('Unauthorized')) {
        logout()
      }
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 프로필 업데이트
   */
  async function updateProfile(request: UpdateProfileRequest): Promise<void> {
    isLoading.value = true
    error.value = null

    try {
      user.value = await authApi.updateProfile(request)
    } catch (err: any) {
      error.value = err.message || 'Failed to update profile'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 비밀번호 변경
   */
  async function changePassword(request: ChangePasswordRequest): Promise<void> {
    isLoading.value = true
    error.value = null

    try {
      await authApi.changePassword(request)
    } catch (err: any) {
      error.value = err.message || 'Failed to change password'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 인증 상태 확인 (앱 초기화 시 호출)
   */
  async function checkAuth(): Promise<void> {
    if (token.value) {
      try {
        await fetchProfile()
      } catch (err) {
        console.error('Auth check failed:', err)
        logout()
      }
    }
  }

  return {
    // State
    token,
    user,
    isLoading,
    error,

    // Getters
    isAuthenticated,
    userEmail,
    username,
    playerId,

    // Actions
    signup,
    login,
    logout,
    fetchProfile,
    updateProfile,
    changePassword,
    checkAuth
  }
})
