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
   * Sign up
   */
  async function signup(request: SignupRequest): Promise<void> {
    isLoading.value = true
    error.value = null

    try {
      const response: AuthResponse = await authApi.signup(request)

      // Save token
      token.value = response.token
      localStorage.setItem('auth_token', response.token)

      // Fetch profile
      await fetchProfile()
    } catch (err: any) {
      error.value = err.message || 'Signup failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Login
   */
  async function login(request: LoginRequest): Promise<void> {
    isLoading.value = true
    error.value = null

    try {
      const response: AuthResponse = await authApi.login(request)

      // Save token
      token.value = response.token
      localStorage.setItem('auth_token', response.token)

      // Fetch profile
      await fetchProfile()
    } catch (err: any) {
      error.value = err.message || 'Login failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Logout
   */
  function logout(): void {
    token.value = null
    user.value = null
    localStorage.removeItem('auth_token')
  }

  /**
   * Fetch profile
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
      // Logout on 401 error
      if (err.message?.includes('401') || err.message?.includes('Unauthorized')) {
        logout()
      }
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Update profile
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
   * Change password
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
   * Check auth status (called on app initialization)
   */
  async function checkAuth(): Promise<void> {
    if (token.value) {
      try {
        await fetchProfile()
      } catch {
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
