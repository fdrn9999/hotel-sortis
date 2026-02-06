<template>
  <div class="signup-view">
    <div class="signup-container">
      <!-- 로고 -->
      <div class="logo-section">
        <h1 class="game-title">HOTEL SORTIS</h1>
        <p class="subtitle">{{ $t('auth.createAccount') }}</p>
      </div>

      <!-- 회원가입 폼 -->
      <form @submit.prevent="handleSignup" class="signup-form">
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
          <label for="username">{{ $t('auth.username') }}</label>
          <input
            id="username"
            v-model="username"
            type="text"
            :placeholder="$t('auth.usernamePlaceholder')"
            required
            minlength="3"
            maxlength="20"
            autocomplete="username"
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
            minlength="8"
            autocomplete="new-password"
          />
        </div>

        <div class="form-group">
          <label for="confirmPassword">{{ $t('auth.confirmPassword') }}</label>
          <input
            id="confirmPassword"
            v-model="confirmPassword"
            type="password"
            :placeholder="$t('auth.confirmPasswordPlaceholder')"
            required
            autocomplete="new-password"
          />
        </div>

        <div class="form-group">
          <label for="language">{{ $t('auth.preferredLanguage') }}</label>
          <select id="language" v-model="preferredLanguage">
            <option value="ko">{{ $t('languages.ko') }}</option>
            <option value="en">{{ $t('languages.en') }}</option>
            <option value="ja">{{ $t('languages.ja') }}</option>
            <option value="zh">{{ $t('languages.zh') }}</option>
          </select>
        </div>

        <button type="submit" class="btn-primary" :disabled="isLoading">
          {{ isLoading ? $t('common.loading') : $t('auth.signup') }}
        </button>
      </form>

      <!-- 로그인 링크 -->
      <div class="login-link">
        <p>{{ $t('auth.alreadyHaveAccount') }}</p>
        <router-link to="/login" class="link">
          {{ $t('auth.loginNow') }}
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
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const preferredLanguage = ref('en')
const isLoading = ref(false)

async function handleSignup() {
  // Validation
  if (!email.value || !username.value || !password.value || !confirmPassword.value) {
    error(t('auth.errors.fillAllFields'))
    return
  }

  if (password.value !== confirmPassword.value) {
    error(t('auth.errors.passwordMismatch'))
    return
  }

  if (password.value.length < 8) {
    error(t('auth.errors.weakPassword'))
    return
  }

  if (username.value.length < 3 || username.value.length > 20) {
    error(t('auth.errors.invalidUsername'))
    return
  }

  isLoading.value = true

  try {
    await authStore.signup({
      email: email.value,
      username: username.value,
      password: password.value,
      preferredLanguage: preferredLanguage.value
    })

    // Navigate to home on signup success
    router.push('/')
  } catch (err: any) {
    if (err.message?.includes('Email already exists')) {
      error(t('auth.errors.emailExists'))
    } else {
      error(t('auth.errors.signupFailed'))
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.signup-view {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, var(--color-dark-navy) 0%, #2d2d3f 100%);
  padding: 20px;
}

.signup-container {
  width: 100%;
  max-width: 450px;
  background: rgba(var(--color-dark-navy-rgb), 0.95);
  border: 2px solid var(--color-gold);
  border-radius: 8px;
  padding: 40px 32px;
  box-shadow: 0 8px 32px rgba(var(--color-gold-rgb), 0.2);
}

.logo-section {
  text-align: center;
  margin-bottom: 32px;
}

.game-title {
  font-family: 'Cinzel Decorative', serif;
  font-size: 32px;
  color: var(--color-gold);
  margin-bottom: 8px;
  text-shadow: 0 0 20px rgba(var(--color-gold-rgb), 0.5);
}

.subtitle {
  font-size: 14px;
  color: var(--color-cream);
  opacity: 0.8;
}

.signup-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  color: var(--color-gold);
  font-weight: 600;
}

.form-group input,
.form-group select {
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(var(--color-gold-rgb), 0.3);
  border-radius: 4px;
  color: var(--color-cream);
  font-size: 16px;
  transition: all 0.3s ease;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: var(--color-gold);
  box-shadow: 0 0 12px rgba(var(--color-gold-rgb), 0.3);
}

.form-group input::placeholder {
  color: rgba(var(--color-cream-rgb), 0.4);
}

.form-group select option {
  background: var(--color-dark-navy);
  color: var(--color-cream);
}

.btn-primary {
  padding: 14px 24px;
  background: linear-gradient(135deg, var(--color-gold) 0%, #f4d03f 100%);
  border: none;
  border-radius: 4px;
  color: var(--color-dark-navy);
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-top: 8px;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(var(--color-gold-rgb), 0.4);
}

.btn-primary:active:not(:disabled) {
  transform: translateY(0);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-link {
  margin-top: 24px;
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid rgba(var(--color-gold-rgb), 0.2);
}

.login-link p {
  font-size: 14px;
  color: var(--color-cream);
  opacity: 0.8;
  margin-bottom: 8px;
}

.login-link .link {
  color: var(--color-gold);
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.login-link .link:hover {
  text-decoration: underline;
  text-shadow: 0 0 8px rgba(var(--color-gold-rgb), 0.5);
}

/* Tablet optimization */
@media (max-width: 768px) {
  .signup-container {
    padding: 36px 28px;
    max-width: 420px;
  }

  .game-title {
    font-size: 30px;
  }

  .signup-form {
    gap: 18px;
  }
}

/* Mobile optimization */
@media (max-width: 480px) {
  .signup-container {
    padding: 32px 24px;
    max-width: 100%;
  }

  .game-title {
    font-size: 28px;
  }

  .signup-form {
    gap: 16px;
  }
}
</style>
