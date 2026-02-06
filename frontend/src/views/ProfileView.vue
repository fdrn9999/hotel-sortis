<template>
  <div class="profile-view">
    <!-- Navigation Bar -->
    <AppNavigation
      :title="$t('profile.myProfile')"
      :show-home="true"
      :show-back="true"
      :show-settings="false"
    />

    <div class="profile-container">
      <h1 class="page-title">{{ $t('profile.myProfile') }}</h1>

      <!-- 프로필 정보 -->
      <div v-if="authStore.user" class="profile-section">
        <div class="info-grid">
          <div class="info-item">
            <span class="label">{{ $t('auth.email') }}</span>
            <span class="value">{{ authStore.user.email }}</span>
          </div>

          <div class="info-item">
            <span class="label">{{ $t('auth.username') }}</span>
            <span class="value">{{ authStore.user.username }}</span>
          </div>

          <div class="info-item">
            <span class="label">{{ $t('profile.elo') }}</span>
            <span class="value">{{ authStore.user.elo }}</span>
          </div>

          <div class="info-item">
            <span class="label">{{ $t('profile.soulStones') }}</span>
            <span class="value">{{ authStore.user.soulStones }}</span>
          </div>

          <div class="info-item">
            <span class="label">{{ $t('profile.currentFloor') }}</span>
            <span class="value">{{ authStore.user.currentFloor }}</span>
          </div>

          <div class="info-item">
            <span class="label">{{ $t('profile.highestFloor') }}</span>
            <span class="value">{{ authStore.user.highestFloorCleared }}</span>
          </div>
        </div>
      </div>

      <!-- 프로필 수정 폼 -->
      <div class="edit-section">
        <h2 class="section-title">{{ $t('profile.editProfile') }}</h2>

        <form @submit.prevent="handleUpdateProfile" class="edit-form">
          <div class="form-group">
            <label for="username">{{ $t('auth.username') }}</label>
            <input
              id="username"
              v-model="newUsername"
              type="text"
              :placeholder="authStore.user?.username"
              minlength="3"
              maxlength="20"
            />
          </div>

          <div class="form-group">
            <label for="language">{{ $t('auth.preferredLanguage') }}</label>
            <select id="language" v-model="newLanguage">
              <option value="ko">{{ $t('languages.ko') }}</option>
              <option value="en">{{ $t('languages.en') }}</option>
              <option value="ja">{{ $t('languages.ja') }}</option>
              <option value="zh">{{ $t('languages.zh') }}</option>
            </select>
          </div>

          <button type="submit" class="btn-primary" :disabled="isLoading">
            {{ isLoading ? $t('common.loading') : $t('profile.saveChanges') }}
          </button>
        </form>
      </div>

      <!-- 비밀번호 변경 -->
      <div class="password-section">
        <h2 class="section-title">{{ $t('profile.changePassword') }}</h2>

        <form @submit.prevent="handleChangePassword" class="password-form">
          <div class="form-group">
            <label for="currentPassword">{{ $t('profile.currentPassword') }}</label>
            <input
              id="currentPassword"
              v-model="currentPassword"
              type="password"
              required
              autocomplete="current-password"
            />
          </div>

          <div class="form-group">
            <label for="newPassword">{{ $t('profile.newPassword') }}</label>
            <input
              id="newPassword"
              v-model="newPassword"
              type="password"
              required
              minlength="8"
              autocomplete="new-password"
            />
          </div>

          <button type="submit" class="btn-secondary" :disabled="isLoading">
            {{ isLoading ? $t('common.loading') : $t('profile.changePassword') }}
          </button>
        </form>
      </div>

      <!-- 로그아웃 버튼 -->
      <div class="logout-section">
        <button @click="handleLogout" class="btn-danger">
          {{ $t('auth.logout') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from 'vue-i18n'
import { useNotification } from '@/composables/useNotification'
import { useConfirmModal } from '@/composables/useConfirmModal'
import AppNavigation from '@/components/AppNavigation.vue'

const router = useRouter()
const authStore = useAuthStore()
const { t } = useI18n()
const { info, success, error } = useNotification()
const { confirm } = useConfirmModal()

const newUsername = ref('')
const newLanguage = ref(authStore.user?.preferredLanguage || 'en')
const currentPassword = ref('')
const newPassword = ref('')
const isLoading = ref(false)

onMounted(() => {
  if (authStore.user) {
    newLanguage.value = authStore.user.preferredLanguage
  }
})

async function handleUpdateProfile() {
  const updates: any = {}

  if (newUsername.value && newUsername.value !== authStore.user?.username) {
    updates.username = newUsername.value
  }

  if (newLanguage.value !== authStore.user?.preferredLanguage) {
    updates.preferredLanguage = newLanguage.value
  }

  if (Object.keys(updates).length === 0) {
    info(t('profile.noChanges'))
    return
  }

  isLoading.value = true

  try {
    await authStore.updateProfile(updates)
    success(t('profile.profileUpdated'))
    newUsername.value = ''
  } catch {
    error(t('profile.updateFailed'))
  } finally {
    isLoading.value = false
  }
}

async function handleChangePassword() {
  if (!currentPassword.value || !newPassword.value) {
    error(t('auth.errors.fillAllFields'))
    return
  }

  if (newPassword.value.length < 8) {
    error(t('auth.errors.weakPassword'))
    return
  }

  isLoading.value = true

  try {
    await authStore.changePassword({
      currentPassword: currentPassword.value,
      newPassword: newPassword.value
    })

    success(t('profile.passwordChanged'))
    currentPassword.value = ''
    newPassword.value = ''
  } catch {
    error(t('profile.passwordChangeFailed'))
  } finally {
    isLoading.value = false
  }
}

async function handleLogout() {
  // CLAUDE.md 3.3.1 rule compliance: replaces confirm()
  const confirmed = await confirm({
    title: t('auth.logout'),
    message: t('auth.confirmLogout'),
    confirmText: t('auth.logout'),
    cancelText: t('common.cancel')
  })

  if (confirmed) {
    authStore.logout()
    success(t('auth.logoutSuccess'))
    router.push('/login')
  }
}
</script>

<style scoped>
.profile-view {
  min-height: 100vh;
  padding: 80px 20px 20px;
  background: linear-gradient(135deg, var(--color-dark-navy) 0%, #2d2d3f 100%);
}

.profile-container {
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-family: 'Cinzel Decorative', serif;
  font-size: 36px;
  color: var(--color-gold);
  text-align: center;
  margin-bottom: 40px;
  text-shadow: 0 0 20px rgba(var(--color-gold-rgb), 0.5);
}

.profile-section,
.edit-section,
.password-section {
  background: rgba(var(--color-dark-navy-rgb), 0.95);
  border: 2px solid rgba(var(--color-gold-rgb), 0.3);
  border-radius: 8px;
  padding: 32px;
  margin-bottom: 24px;
}

.section-title {
  font-size: 24px;
  color: var(--color-gold);
  margin-bottom: 24px;
  border-bottom: 1px solid rgba(var(--color-gold-rgb), 0.2);
  padding-bottom: 12px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item .label {
  font-size: 14px;
  color: rgba(var(--color-gold-rgb), 0.7);
  font-weight: 600;
}

.info-item .value {
  font-size: 18px;
  color: var(--color-cream);
  font-weight: 600;
}

.edit-form,
.password-form {
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

.form-group select option {
  background: var(--color-dark-navy);
  color: var(--color-cream);
}

.btn-primary,
.btn-secondary,
.btn-danger {
  padding: 12px 24px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.btn-primary {
  background: linear-gradient(135deg, var(--color-gold) 0%, #f4d03f 100%);
  color: var(--color-dark-navy);
}

.btn-secondary {
  background: linear-gradient(135deg, var(--color-purple) 0%, #9b30ff 100%);
  color: var(--color-cream);
}

.btn-danger {
  background: linear-gradient(135deg, #c41e3a 0%, #ff1744 100%);
  color: var(--color-cream);
  width: 100%;
}

.btn-primary:hover:not(:disabled),
.btn-secondary:hover:not(:disabled),
.btn-danger:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(var(--color-gold-rgb), 0.4);
}

.btn-primary:disabled,
.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.logout-section {
  margin-top: 32px;
}

/* Mobile optimization */
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .profile-section,
  .edit-section,
  .password-section {
    padding: 24px 20px;
  }

  .page-title {
    font-size: 28px;
  }
}
</style>
