import apiClient from './client'

export interface SignupRequest {
  email: string
  password: string
  username: string
  preferredLanguage?: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  email: string
  username: string
  playerId: number
  role: string
}

export interface MessageResponse {
  message: string
}

export interface UserProfile {
  userId: number
  email: string
  username: string
  playerId: number
  elo: number
  soulStones: number
  currentFloor: number
  highestFloorCleared: number
  preferredLanguage: string
  role: string
  emailVerified: boolean
}

export interface UpdateProfileRequest {
  username?: string
  preferredLanguage?: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

/**
 * 회원가입
 */
export async function signup(request: SignupRequest): Promise<AuthResponse> {
  const response = await apiClient.post<AuthResponse>('/auth/signup', request)
  return response.data
}

/**
 * 로그인
 */
export async function login(request: LoginRequest): Promise<AuthResponse> {
  const response = await apiClient.post<AuthResponse>('/auth/login', request)
  return response.data
}

/**
 * 내 프로필 조회
 */
export async function getMyProfile(): Promise<UserProfile> {
  const response = await apiClient.get<UserProfile>('/users/me')
  return response.data
}

/**
 * 프로필 업데이트
 */
export async function updateProfile(request: UpdateProfileRequest): Promise<UserProfile> {
  const response = await apiClient.put<UserProfile>('/users/me', request)
  return response.data
}

/**
 * 비밀번호 변경
 */
export async function changePassword(request: ChangePasswordRequest): Promise<MessageResponse> {
  const response = await apiClient.post<MessageResponse>('/users/me/change-password', request)
  return response.data
}
