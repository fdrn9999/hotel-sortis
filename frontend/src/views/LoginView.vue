<template>
  <div class="login-view">
    <div class="login-container">
      <!-- 로고 -->
      <div class="logo-section">
        <h1 class="game-title">HOTEL SORTIS</h1>
        <p class="subtitle">{{ $t('auth.welcome') }}</p>
      </div>

      <!-- 로그인 폼 -->
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="email">{{ $t('auth.email') }}</label>
          <input
            id="email"
            v-model="email"
            type="email"
            :placeholder="$t('auth.emailPlaceholder')"
            required
            autocomplete="email"
          />
        </div>

        <div class="form-group">
          <label for="password">{{ $t('auth.password') }}</label>
          <input
            id="password"
            v-model="password"
            type="password"
            :placeholder="$t('auth.passwordPlaceholder')"
            required
            autocomplete="current-password"
          />
        </div>

        <button type="submit" class="btn-primary" :disabled="isLoading">
          {{ isLoading ? $t('common.loading') : $t('auth.login') }}
        </button>
      </form>

      <!-- 회원가입 링크 -->
      <div class="signup-link">
        <p>{{ $t('auth.noAccount') }}</p>
        <router-link to="/signup" class="link">
          {{ $t('auth.signupNow') }}
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from 'vue-i18n'
import { useNotification } from '@/composables/useNotification'

const router = useRouter()
const authStore = useAuthStore()
const { t } = useI18n()
const { error } = useNotification()

const email = ref('')
const password = ref('')
const isLoading = ref(false)

async function handleLogin() {
  if (!email.value || !password.value) {
    error(t('auth.errors.fillAllFields'))
    return
  }

  isLoading.value = true

  try {
    await authStore.login({
      email: email.value,
      password: password.value
    })

    // 로그인 성공 시 홈으로 이동
    router.push('/')
  } catch (err: any) {
    console.error('Login failed:', err)
    error(t('auth.errors.invalidCredentials'))
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-view {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1a1a27 0%, #2d2d3f 100%);
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 400px;
  background: rgba(27, 27, 39, 0.95);
  border: 2px solid #d4af37;
  border-radius: 8px;
  padding: 40px 32px;
  box-shadow: 0 8px 32px rgba(212, 175, 55, 0.2);
}

.logo-section {
  text-align: center;
  margin-bottom: 32px;
}

.game-title {
  font-family: 'Cinzel Decorative', serif;
  font-size: 32px;
  color: #d4af37;
  margin-bottom: 8px;
  text-shadow: 0 0 20px rgba(212, 175, 55, 0.5);
}

.subtitle {
  font-size: 14px;
  color: #fffdd0;
  opacity: 0.8;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  color: #d4af37;
  font-weight: 600;
}

.form-group input {
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 4px;
  color: #fffdd0;
  font-size: 16px;
  transition: all 0.3s ease;
}

.form-group input:focus {
  outline: none;
  border-color: #d4af37;
  box-shadow: 0 0 12px rgba(212, 175, 55, 0.3);
}

.form-group input::placeholder {
  color: rgba(255, 253, 208, 0.4);
}

.btn-primary {
  padding: 14px 24px;
  background: linear-gradient(135deg, #d4af37 0%, #f4d03f 100%);
  border: none;
  border-radius: 4px;
  color: #1a1a27;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(212, 175, 55, 0.4);
}

.btn-primary:active:not(:disabled) {
  transform: translateY(0);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.signup-link {
  margin-top: 24px;
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid rgba(212, 175, 55, 0.2);
}

.signup-link p {
  font-size: 14px;
  color: #fffdd0;
  opacity: 0.8;
  margin-bottom: 8px;
}

.signup-link .link {
  color: #d4af37;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.signup-link .link:hover {
  text-decoration: underline;
  text-shadow: 0 0 8px rgba(212, 175, 55, 0.5);
}

/* 모바일 최적화 */
@media (max-width: 480px) {
  .login-container {
    padding: 32px 24px;
  }

  .game-title {
    font-size: 28px;
  }
}
</style>
