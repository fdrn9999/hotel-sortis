<template>
  <div class="chat-widget" :class="{ minimized: isMinimized }">
    <!-- Header -->
    <div class="chat-header" @click="toggleMinimized">
      <div class="header-left">
        <span class="chat-icon">{{ currentTab === 'global' ? '#' : '@' }}</span>
        <span class="chat-title">{{ t('social.chat.title') }}</span>
        <span v-if="totalUnread > 0" class="unread-badge">{{ totalUnread }}</span>
      </div>
      <div class="header-actions">
        <button
          class="header-btn"
          :aria-label="isMinimized ? t('social.chat.expand') : t('social.chat.minimize')"
          @click.stop="toggleMinimized"
        >
          {{ isMinimized ? '+' : '-' }}
        </button>
      </div>
    </div>

    <!-- Chat Body (hidden when minimized) -->
    <template v-if="!isMinimized">
      <!-- Tab Bar -->
      <div class="chat-tabs">
        <button
          class="tab-btn"
          :class="{ active: currentTab === 'global' }"
          @click="currentTab = 'global'"
        >
          <span class="tab-icon">#</span>
          {{ t('social.chat.global') }}
        </button>
        <button
          class="tab-btn"
          :class="{ active: currentTab === 'whisper' }"
          @click="currentTab = 'whisper'"
        >
          <span class="tab-icon">@</span>
          {{ t('social.chat.whisper') }}
          <span v-if="socialStore.unreadCount > 0" class="tab-badge">{{ socialStore.unreadCount }}</span>
        </button>
        <button
          class="tab-btn"
          :class="{ active: currentTab === 'friends' }"
          @click="currentTab = 'friends'"
        >
          <span class="tab-icon">♥</span>
          {{ t('social.friends.title') }}
          <span v-if="socialStore.pendingRequestCount > 0" class="tab-badge">{{ socialStore.pendingRequestCount }}</span>
        </button>
      </div>

      <!-- Friends Tab -->
      <FriendsPanel
        v-if="currentTab === 'friends'"
        @start-whisper="handleStartWhisper"
      />

      <!-- Messages Area -->
      <div v-else class="chat-messages" ref="messagesContainer">
        <template v-if="currentTab === 'global'">
          <div v-if="displayMessages.length === 0" class="empty-messages">
            {{ t('social.chat.noMessages') }}
          </div>
          <div
            v-for="msg in displayMessages"
            :key="msg.id"
            class="message"
            :class="{ own: msg.senderId === playerId }"
            @contextmenu.prevent="showContextMenu($event, msg)"
          >
            <span class="sender" @click="startWhisper(msg.senderUsername)">
              {{ msg.senderUsername }}
            </span>
            <span class="content">{{ msg.content }}</span>
            <span class="time">{{ formatTime(msg.createdAt) }}</span>
          </div>
        </template>

        <template v-else>
          <!-- Whisper Conversations List -->
          <div v-if="!activeWhisperPartner" class="whisper-list">
            <div v-if="whisperPartners.length === 0" class="empty-messages">
              {{ t('social.chat.noWhispers') }}
            </div>
            <div
              v-for="partner in whisperPartners"
              :key="partner.id"
              class="whisper-partner"
              @click="openWhisperConversation(partner)"
            >
              <span class="partner-name">{{ partner.username }}</span>
              <span v-if="partner.unread > 0" class="partner-unread">{{ partner.unread }}</span>
            </div>
          </div>

          <!-- Active Whisper Conversation -->
          <template v-else>
            <div class="whisper-header">
              <button class="back-btn" @click="activeWhisperPartner = null">
                &#8249;
              </button>
              <span class="partner-title">{{ activeWhisperPartner.username }}</span>
            </div>
            <div class="whisper-messages">
              <div
                v-for="msg in activeWhisperMessages"
                :key="msg.id"
                class="message"
                :class="{ own: msg.senderId === playerId }"
              >
                <span class="content">{{ msg.content }}</span>
                <span class="time">{{ formatTime(msg.createdAt) }}</span>
              </div>
            </div>
          </template>
        </template>
      </div>

      <!-- Input Area -->
      <div class="chat-input-area">
        <input
          type="text"
          v-model="inputMessage"
          :placeholder="inputPlaceholder"
          @keydown.enter="sendMessage"
          class="chat-input"
          :aria-label="t('social.chat.inputLabel')"
        />
        <button class="send-btn" @click="sendMessage" :aria-label="t('social.chat.send')">
          &#10148;
        </button>
      </div>
    </template>

    <!-- Context Menu -->
    <Teleport to="body">
      <div
        v-if="contextMenu.visible"
        class="context-menu"
        :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
        @click.stop
      >
        <button class="context-item" @click="handleContextWhisper">
          {{ t('social.chat.whisperTo') }}
        </button>
        <button
          v-if="!socialStore.isFriend(contextMenu.playerId)"
          class="context-item"
          @click="handleContextAddFriend"
        >
          {{ t('social.friends.addFriend') }}
        </button>
        <button
          v-if="!socialStore.isBlocked(contextMenu.playerId)"
          class="context-item danger"
          @click="handleContextBlock"
        >
          {{ t('social.block.blockPlayer') }}
        </button>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSocialStore } from '@/stores/social'
import { useAuthStore } from '@/stores/auth'
import { useChatWebSocket } from '@/composables/useChatWebSocket'
import { useNotification } from '@/composables/useNotification'
import FriendsPanel from '@/components/FriendsPanel.vue'
import type { ChatMessage, FriendRequest } from '@/types/game'

const { t } = useI18n()
const socialStore = useSocialStore()
const authStore = useAuthStore()
const notification = useNotification()

// Player ID from auth store
const playerId = computed(() => authStore.playerId ?? 1)

// Chat WebSocket
const chatWs = useChatWebSocket(playerId.value)

// UI State
const isMinimized = ref(true)
const currentTab = ref<'global' | 'whisper' | 'friends'>('global')
const inputMessage = ref('')
const messagesContainer = ref<HTMLElement | null>(null)

// Whisper state
const activeWhisperPartner = ref<{ id: number; username: string } | null>(null)

// Context menu state
const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  playerId: 0,
  username: ''
})

// Computed
const displayMessages = computed(() => {
  if (currentTab.value === 'global') {
    return socialStore.globalMessages.slice().reverse()
  }
  return []
})

const activeWhisperMessages = computed(() => {
  if (!activeWhisperPartner.value) return []
  return socialStore.getWhisperHistory(activeWhisperPartner.value.id).slice().reverse()
})

const whisperPartners = computed(() => {
  // Extract unique partners from whisper conversations
  const partners: { id: number; username: string; unread: number }[] = []
  socialStore.whisperConversations.forEach((messages, partnerId) => {
    if (messages.length > 0) {
      const lastMsg = messages[0]
      const unread = messages.filter(m => !m.readAt && m.senderId !== playerId.value).length
      partners.push({
        id: partnerId,
        username: lastMsg.senderId === playerId.value
          ? (lastMsg.receiverUsername || 'Unknown')
          : lastMsg.senderUsername,
        unread
      })
    }
  })
  return partners
})

const totalUnread = computed(() => socialStore.unreadCount)

const inputPlaceholder = computed(() => {
  if (currentTab.value === 'whisper' && activeWhisperPartner.value) {
    return t('social.chat.whisperPlaceholder', { name: activeWhisperPartner.value.username })
  }
  return t('social.chat.globalPlaceholder')
})

// Methods
const toggleMinimized = () => {
  isMinimized.value = !isMinimized.value
}

const formatTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const sendMessage = () => {
  const content = inputMessage.value.trim()
  if (!content) return

  // Check for whisper command: /w username message
  const whisperMatch = content.match(/^\/w\s+(\S+)\s+(.+)$/i)
  if (whisperMatch) {
    const targetUsername = whisperMatch[1]
    // TODO: Implement username-to-ID lookup to send whisper via WebSocket
    // For now, just show notification
    notification.info(t('social.chat.whisperSent', { name: targetUsername }))
    inputMessage.value = ''
    return
  }

  // Normal message
  if (currentTab.value === 'global') {
    if (chatWs.connected.value) {
      chatWs.sendGlobalMessage(content)
    } else {
      // Fallback to API
      socialStore.sendGlobalMessage(playerId.value, content)
    }
  } else if (activeWhisperPartner.value) {
    if (chatWs.connected.value) {
      chatWs.sendWhisper(activeWhisperPartner.value.id, content)
    } else {
      socialStore.sendWhisper(playerId.value, activeWhisperPartner.value.id, content)
    }
  }

  inputMessage.value = ''
  scrollToBottom()
}

const startWhisper = (username: string) => {
  inputMessage.value = `/w ${username} `
}

const openWhisperConversation = (partner: { id: number; username: string }) => {
  activeWhisperPartner.value = partner
  // Mark as read
  if (chatWs.connected.value) {
    chatWs.markAsRead(partner.id)
  } else {
    socialStore.markConversationAsRead(playerId.value, partner.id)
  }
}

// Handler for starting whisper from FriendsPanel
const handleStartWhisper = (partner: { id: number; username: string }) => {
  currentTab.value = 'whisper'
  activeWhisperPartner.value = partner
  // Load whisper history if not already loaded
  if (!socialStore.whisperConversations.has(partner.id)) {
    socialStore.loadWhisperConversation(playerId.value, partner.id)
  }
}

// Context Menu
const showContextMenu = (event: MouseEvent, msg: ChatMessage) => {
  if (msg.senderId === playerId.value) return // Don't show for own messages

  contextMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY,
    playerId: msg.senderId,
    username: msg.senderUsername
  }
}

const hideContextMenu = () => {
  contextMenu.value.visible = false
}

const handleContextWhisper = () => {
  startWhisper(contextMenu.value.username)
  hideContextMenu()
}

const handleContextAddFriend = async () => {
  const success = await socialStore.sendFriendRequest(playerId.value, contextMenu.value.playerId)
  if (success) {
    notification.success(t('social.friends.requestSent'))
  }
  hideContextMenu()
}

const handleContextBlock = async () => {
  const success = await socialStore.blockPlayer(playerId.value, contextMenu.value.playerId)
  if (success) {
    notification.success(t('social.block.blocked'))
  }
  hideContextMenu()
}

// Watch for new messages to auto-scroll
watch(() => socialStore.globalMessages.length, scrollToBottom)

// Lifecycle
onMounted(() => {
  // Connect to WebSocket and subscribe
  chatWs.subscribeChat()

  // Wire up friend request notifications to dispatch custom event
  chatWs.onFriendRequest.value = (request: FriendRequest) => {
    window.dispatchEvent(new CustomEvent('friend-request-received', { detail: request }))
  }

  // Load initial messages
  socialStore.loadGlobalMessages()
  socialStore.loadUnreadCount(playerId.value)

  // Close context menu on click outside
  document.addEventListener('click', hideContextMenu)
})

onUnmounted(() => {
  chatWs.unsubscribeAll()
  document.removeEventListener('click', hideContextMenu)
  hideContextMenu() // Force close any open menu
})
</script>

<style scoped>
.chat-widget {
  position: fixed;
  bottom: 20px;
  left: 20px;
  width: 350px;
  max-height: 450px;
  background: rgba(var(--color-dark-navy-rgb), 0.95);
  border: 2px solid var(--color-gold);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  z-index: 1000;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.chat-widget.minimized {
  max-height: 48px;
}

/* Header */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(var(--color-gold-rgb), 0.2) 0%, transparent 100%);
  border-bottom: 1px solid rgba(var(--color-gold-rgb), 0.3);
  cursor: pointer;
  user-select: none;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-icon {
  font-size: 18px;
  color: var(--color-gold);
  font-weight: bold;
}

.chat-title {
  font-family: var(--font-primary);
  font-size: 14px;
  color: var(--color-cream);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.unread-badge {
  background: var(--color-velvet-red);
  color: var(--color-cream);
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: bold;
}

.header-btn {
  background: none;
  border: 1px solid var(--color-gold);
  color: var(--color-gold);
  width: 24px;
  height: 24px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  transition: all 0.2s ease;
}

.header-btn:hover {
  background: var(--color-gold);
  color: var(--color-dark-navy);
}

/* Tabs */
.chat-tabs {
  display: flex;
  border-bottom: 1px solid rgba(var(--color-gold-rgb), 0.3);
}

.tab-btn {
  flex: 1;
  padding: 10px;
  background: none;
  border: none;
  color: var(--color-text-secondary);
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.tab-btn:hover {
  color: var(--color-cream);
  background: rgba(var(--color-gold-rgb), 0.1);
}

.tab-btn.active {
  color: var(--color-gold);
  border-bottom: 2px solid var(--color-gold);
}

.tab-icon {
  font-weight: bold;
}

.tab-badge {
  background: var(--color-velvet-red);
  color: var(--color-cream);
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 8px;
}

/* Messages */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 200px;
  max-height: 280px;
}

.empty-messages {
  color: var(--color-text-secondary);
  text-align: center;
  padding: 40px 20px;
  font-size: 13px;
}

.message {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px 10px;
  background: rgba(var(--color-cream-rgb), 0.05);
  border-radius: 6px;
  font-size: 13px;
  transition: background 0.2s ease;
}

.message:hover {
  background: rgba(var(--color-cream-rgb), 0.1);
}

.message.own {
  background: rgba(var(--color-gold-rgb), 0.15);
}

.sender {
  color: var(--color-gold);
  font-weight: 600;
  cursor: pointer;
}

.sender:hover {
  text-decoration: underline;
}

.content {
  color: var(--color-cream);
  flex: 1;
  word-break: break-word;
}

.time {
  color: var(--color-text-secondary);
  font-size: 10px;
  align-self: flex-end;
}

/* Whisper List */
.whisper-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.whisper-partner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: rgba(var(--color-cream-rgb), 0.05);
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.whisper-partner:hover {
  background: rgba(var(--color-gold-rgb), 0.15);
}

.partner-name {
  color: var(--color-cream);
  font-weight: 500;
}

.partner-unread {
  background: var(--color-velvet-red);
  color: var(--color-cream);
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 10px;
}

/* Whisper Header */
.whisper-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid rgba(var(--color-gold-rgb), 0.2);
  margin-bottom: 8px;
}

.back-btn {
  background: none;
  border: none;
  color: var(--color-gold);
  font-size: 20px;
  cursor: pointer;
  padding: 4px 8px;
}

.back-btn:hover {
  color: var(--color-cream);
}

.partner-title {
  color: var(--color-gold);
  font-weight: 600;
}

.whisper-messages {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* Input Area */
.chat-input-area {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid rgba(var(--color-gold-rgb), 0.3);
}

.chat-input {
  flex: 1;
  padding: 10px 12px;
  background: rgba(var(--color-cream-rgb), 0.1);
  border: 1px solid rgba(var(--color-gold-rgb), 0.3);
  border-radius: 6px;
  color: var(--color-cream);
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s ease;
}

.chat-input::placeholder {
  color: var(--color-text-secondary);
}

.chat-input:focus {
  border-color: var(--color-gold);
}

.send-btn {
  background: var(--color-gold);
  border: none;
  color: var(--color-dark-navy);
  width: 40px;
  height: 40px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s ease;
}

.send-btn:hover {
  background: var(--color-cream);
}

/* Context Menu */
.context-menu {
  position: fixed;
  background: rgba(var(--color-dark-navy-rgb), 0.98);
  border: 1px solid var(--color-gold);
  border-radius: 6px;
  padding: 4px 0;
  min-width: 150px;
  z-index: 9999;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.4);
}

.context-item {
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

.context-item:hover {
  background: rgba(var(--color-gold-rgb), 0.2);
}

.context-item.danger {
  color: var(--color-error);
}

.context-item.danger:hover {
  background: rgba(var(--color-error-rgb), 0.2);
}

/* Mobile Responsive */
@media (max-width: 480px) {
  .chat-widget {
    left: 10px;
    right: 10px;
    bottom: 10px;
    width: auto;
    max-height: 60vh;
  }

  .chat-messages {
    max-height: 40vh;
  }
}
</style>
