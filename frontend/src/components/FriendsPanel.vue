<template>
  <div class="friends-panel">
    <!-- Search Section -->
    <div class="search-section">
      <input
        type="text"
        v-model="searchQuery"
        :placeholder="t('social.friends.searchPlaceholder')"
        class="search-input"
        @keydown.enter="handleSearch"
      />
      <button
        class="search-btn"
        @click="handleSearch"
        :disabled="!searchQuery.trim() || searching"
      >
        {{ searching ? '...' : '🔍' }}
      </button>
    </div>

    <!-- Search Results -->
    <div v-if="searchResults.length > 0" class="search-results">
      <div class="section-header">{{ t('social.friends.searchResults') }}</div>
      <div
        v-for="player in searchResults"
        :key="player.id"
        class="player-item"
      >
        <span class="player-name">{{ player.username }}</span>
        <button
          v-if="!socialStore.isFriend(player.id)"
          class="action-btn add"
          @click="sendRequest(player.id)"
          :disabled="requestingSent.has(player.id)"
        >
          {{ requestingSent.has(player.id) ? '✓' : '+' }}
        </button>
        <span v-else class="friend-badge">{{ t('social.friends.alreadyFriend') }}</span>
      </div>
    </div>

    <!-- Pending Requests Section -->
    <div v-if="socialStore.pendingRequests.length > 0" class="pending-section">
      <div class="section-header">
        {{ t('social.friends.pendingRequests') }}
        <span class="count-badge">{{ socialStore.pendingRequests.length }}</span>
      </div>
      <div
        v-for="request in socialStore.pendingRequests"
        :key="request.requestId"
        class="request-item"
      >
        <span class="player-name">{{ request.senderUsername }}</span>
        <div class="request-actions">
          <button class="action-btn accept" @click="acceptRequest(request.requestId)">
            ✓
          </button>
          <button class="action-btn decline" @click="declineRequest(request.requestId)">
            ✕
          </button>
        </div>
      </div>
    </div>

    <!-- Friends List -->
    <div class="friends-section">
      <div class="section-header">
        {{ t('social.friends.title') }}
        <span class="count-badge">{{ socialStore.friendCount }}</span>
      </div>

      <div v-if="socialStore.loading" class="loading-state">
        {{ t('common.loading') }}
      </div>

      <div v-else-if="socialStore.friends.length === 0" class="empty-state">
        {{ t('social.friends.noFriends') }}
      </div>

      <div v-else class="friends-list">
        <div
          v-for="friend in socialStore.friends"
          :key="friend.playerId"
          class="friend-item"
          @contextmenu.prevent="showFriendMenu($event, friend)"
        >
          <div class="friend-info">
            <span class="status-dot" :class="friend.online ? 'online' : 'offline'"></span>
            <span class="friend-name">{{ friend.username }}</span>
          </div>
          <button class="action-btn whisper" @click="startWhisper(friend)">
            @
          </button>
        </div>
      </div>
    </div>

    <!-- Friend Context Menu -->
    <Teleport to="body">
      <div
        v-if="friendMenu.visible"
        class="friend-context-menu"
        :style="{ left: friendMenu.x + 'px', top: friendMenu.y + 'px' }"
        @click.stop
      >
        <button class="menu-item" @click="handleWhisper">
          {{ t('social.chat.whisperTo') }}
        </button>
        <button class="menu-item danger" @click="handleRemoveFriend">
          {{ t('social.friends.removeFriend') }}
        </button>
        <button class="menu-item danger" @click="handleBlockFriend">
          {{ t('social.block.blockPlayer') }}
        </button>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSocialStore } from '@/stores/social'
import { useAuthStore } from '@/stores/auth'
import { useNotification } from '@/composables/useNotification'
import { useConfirmModal } from '@/composables/useConfirmModal'
import type { FriendInfo } from '@/types/game'

const emit = defineEmits<{
  (e: 'start-whisper', partner: { id: number; username: string }): void
}>()

const { t } = useI18n()
const socialStore = useSocialStore()
const authStore = useAuthStore()
const notification = useNotification()
const { confirm } = useConfirmModal()

const playerId = authStore.playerId ?? 1

// Search state
const searchQuery = ref('')
const searching = ref(false)
const searchResults = ref<{ id: number; username: string }[]>([])
const requestingSent = ref<Set<number>>(new Set())

// Context menu state
const friendMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  friend: null as FriendInfo | null
})

// Load data on mount
onMounted(() => {
  socialStore.loadFriends(playerId)
  socialStore.loadPendingRequests(playerId)
  document.addEventListener('click', hideFriendMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', hideFriendMenu)
})

// Search handler
const handleSearch = async () => {
  const query = searchQuery.value.trim()
  if (!query) return

  searching.value = true
  try {
    // TODO: Implement player search API
    // For now, simulate with empty results
    searchResults.value = []
    // When API is ready:
    // const results = await searchPlayers(query)
    // searchResults.value = results
  } finally {
    searching.value = false
  }
}

// Friend request actions
const sendRequest = async (targetId: number) => {
  const success = await socialStore.sendFriendRequest(playerId, targetId)
  if (success) {
    requestingSent.value.add(targetId)
    notification.success(t('social.friends.requestSent'))
  }
}

const acceptRequest = async (requestId: number) => {
  const success = await socialStore.acceptFriendRequest(playerId, requestId)
  if (success) {
    notification.success(t('social.friends.requestAccepted'))
  }
}

const declineRequest = async (requestId: number) => {
  const success = await socialStore.declineFriendRequest(playerId, requestId)
  if (success) {
    notification.info(t('social.friends.requestDeclined'))
  }
}

// Whisper action
const startWhisper = (friend: FriendInfo) => {
  emit('start-whisper', { id: friend.playerId, username: friend.username })
}

// Context menu
const showFriendMenu = (event: MouseEvent, friend: FriendInfo) => {
  friendMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY,
    friend
  }
}

const hideFriendMenu = () => {
  friendMenu.value.visible = false
}

const handleWhisper = () => {
  if (friendMenu.value.friend) {
    startWhisper(friendMenu.value.friend)
  }
  hideFriendMenu()
}

const handleRemoveFriend = async () => {
  if (!friendMenu.value.friend) return

  const friend = friendMenu.value.friend
  hideFriendMenu()

  const confirmed = await confirm({
    title: t('social.friends.removeFriend'),
    message: t('social.friends.removeConfirm', { name: friend.username }),
    confirmText: t('common.confirm'),
    cancelText: t('common.cancel'),
    variant: 'danger'
  })

  if (confirmed) {
    const success = await socialStore.removeFriend(playerId, friend.playerId)
    if (success) {
      notification.success(t('social.friends.friendRemoved'))
    }
  }
}

const handleBlockFriend = async () => {
  if (!friendMenu.value.friend) return

  const friend = friendMenu.value.friend
  hideFriendMenu()

  const confirmed = await confirm({
    title: t('social.block.blockPlayer'),
    message: t('social.block.blockConfirm', { name: friend.username }),
    confirmText: t('common.confirm'),
    cancelText: t('common.cancel'),
    variant: 'danger'
  })

  if (confirmed) {
    const success = await socialStore.blockPlayer(playerId, friend.playerId)
    if (success) {
      notification.success(t('social.block.blocked'))
    }
  }
}
</script>

<style scoped>
.friends-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
  height: 100%;
  overflow-y: auto;
}

/* Search Section */
.search-section {
  display: flex;
  gap: 8px;
}

.search-input {
  flex: 1;
  padding: 8px 12px;
  background: rgba(var(--color-cream-rgb), 0.1);
  border: 1px solid rgba(var(--color-gold-rgb), 0.3);
  border-radius: 6px;
  color: var(--color-cream);
  font-size: 13px;
  outline: none;
}

.search-input::placeholder {
  color: var(--color-text-secondary);
}

.search-input:focus {
  border-color: var(--color-gold);
}

.search-btn {
  padding: 8px 12px;
  background: var(--color-gold);
  border: none;
  border-radius: 6px;
  color: var(--color-dark-navy);
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s ease;
}

.search-btn:hover:not(:disabled) {
  background: var(--color-cream);
}

.search-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Section Headers */
.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-gold);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(var(--color-gold-rgb), 0.2);
}

.count-badge {
  background: rgba(var(--color-gold-rgb), 0.2);
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 11px;
}

/* Search Results */
.search-results {
  background: rgba(var(--color-cream-rgb), 0.05);
  border-radius: 6px;
  padding: 12px;
}

.player-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid rgba(var(--color-cream-rgb), 0.1);
}

.player-item:last-child {
  border-bottom: none;
}

.player-name {
  color: var(--color-cream);
  font-size: 13px;
}

.friend-badge {
  font-size: 11px;
  color: var(--color-text-secondary);
}

/* Action Buttons */
.action-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
}

.action-btn.add {
  background: var(--color-gold);
  color: var(--color-dark-navy);
}

.action-btn.add:hover:not(:disabled) {
  background: var(--color-cream);
}

.action-btn.accept {
  background: var(--color-success);
  color: white;
}

.action-btn.decline {
  background: var(--color-error);
  color: white;
}

.action-btn.whisper {
  background: rgba(var(--color-gold-rgb), 0.2);
  color: var(--color-gold);
}

.action-btn.whisper:hover {
  background: var(--color-gold);
  color: var(--color-dark-navy);
}

/* Pending Section */
.pending-section {
  background: rgba(var(--color-gold-rgb), 0.1);
  border-radius: 6px;
  padding: 12px;
}

.request-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.request-actions {
  display: flex;
  gap: 6px;
}

/* Friends List */
.friends-section {
  flex: 1;
  min-height: 0;
}

.friends-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.friend-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: rgba(var(--color-cream-rgb), 0.05);
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.friend-item:hover {
  background: rgba(var(--color-gold-rgb), 0.1);
}

.friend-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.online {
  background: var(--color-success);
  box-shadow: 0 0 6px var(--color-success);
}

.status-dot.offline {
  background: var(--color-text-secondary);
}

.friend-name {
  color: var(--color-cream);
  font-size: 13px;
}

/* States */
.loading-state,
.empty-state {
  color: var(--color-text-secondary);
  text-align: center;
  padding: 24px;
  font-size: 13px;
}

/* Context Menu */
.friend-context-menu {
  position: fixed;
  background: rgba(var(--color-dark-navy-rgb), 0.98);
  border: 1px solid var(--color-gold);
  border-radius: 6px;
  padding: 4px 0;
  min-width: 150px;
  z-index: 9999;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.4);
}

.menu-item {
  display: block;
  width: 100%;
  padding: 10px 16px;
  background: none;
  border: none;
  color: var(--color-cream);
  font-size: 13px;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s ease;
}

.menu-item:hover {
  background: rgba(var(--color-gold-rgb), 0.2);
}

.menu-item.danger {
  color: var(--color-error);
}

.menu-item.danger:hover {
  background: rgba(var(--color-error-rgb), 0.2);
}
</style>
