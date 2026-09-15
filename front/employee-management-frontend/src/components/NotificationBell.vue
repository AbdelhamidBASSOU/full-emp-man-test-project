<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useNotificationSocket } from '../composables/useNotificationSocket'
import { getNotifications, getUnreadCount, markAllAsRead } from '../api/notifications'

const authStore = useAuthStore()
const notifications = ref([])
const unreadCount = ref(0)
const showDropdown = ref(false)
const loadingNotifications = ref(false)

/* ── WebSocket (Super Admins only) ─────────────────────────── */
const { lastNotification, connect, disconnect } = useNotificationSocket()

/**
 * Whenever the socket pushes a new notification, prepend it to
 * the visible list immediately and bump the badge.
 */
watch(lastNotification, (notification) => {
  if (!notification) return
  unreadCount.value++
  // Prepend so the newest notification appears at the top instantly
  notifications.value = [notification, ...notifications.value]
})

/* ── Pre-fetch notifications & unread count ───────────────── */
async function loadNotificationData() {
  if (!authStore.isSuperAdmin) return
  loadingNotifications.value = true
  try {
    const [countRes, notifRes] = await Promise.all([
      getUnreadCount(),
      getNotifications(),
    ])
    unreadCount.value = countRes.data.count
    notifications.value = notifRes.data
  } catch {
    // silently ignore errors
  } finally {
    loadingNotifications.value = false
  }
}

/* ── Dropdown (Opens INSTANTLY from memory) ───────────────── */
function toggleDropdown() {
  showDropdown.value = !showDropdown.value
  if (showDropdown.value) {
    // Refresh quietly in background without delaying dropdown open
    getNotifications().then((res) => {
      notifications.value = res.data
    }).catch(() => {})
  }
}

async function handleMarkAllRead() {
  try {
    await markAllAsRead()
    notifications.value = notifications.value.map((n) => ({
      ...n,
      isRead: true,
    }))
    unreadCount.value = 0
  } catch (err) {
    console.error('Failed to mark notifications as read', err)
  }
}

function timeAgo(dateStr) {
  if (!dateStr) return ''
  const diffMs = Date.now() - new Date(dateStr).getTime()
  const mins = Math.floor(diffMs / 60000)
  if (mins < 1) return 'just now'
  if (mins < 60) return `${mins}m ago`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}h ago`
  return `${Math.floor(hours / 24)}d ago`
}

/* ── Lifecycle ─────────────────────────────────────────────── */
onMounted(async () => {
  if (!authStore.isSuperAdmin) return
  await loadNotificationData()
  connect()
})

onUnmounted(() => {
  disconnect()
})
</script>

<template>
  <div v-if="authStore.isSuperAdmin" class="notification-wrapper">
    <button class="bell-btn" @click="toggleDropdown" aria-label="Notifications">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
        <path d="M13.73 21a2 2 0 0 1-3.46 0" />
      </svg>
      <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</span>
    </button>

    <div v-if="showDropdown" class="dropdown-overlay" @click="showDropdown = false"></div>
    <div v-if="showDropdown" class="dropdown card">
      <div class="dropdown-header">
        <span>Notifications</span>
        <button v-if="unreadCount > 0" class="mark-read-btn" @click="handleMarkAllRead">
          Mark all read
        </button>
      </div>
      <div class="dropdown-list">
        <div v-if="notifications.length === 0" class="empty-state">
          No notifications yet.
        </div>
        <div
          v-for="n in notifications"
          :key="n.id"
          class="notification-item"
          :class="{ unread: !n.isRead }"
        >
          <div class="notification-message">{{ n.message }}</div>
          <div class="notification-time mono">{{ timeAgo(n.createdAt) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.notification-wrapper {
  position: relative;
}
.bell-btn {
  position: relative;
  background: transparent;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 8px;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s ease, color 0.15s ease;
}
.bell-btn:hover {
  background: var(--color-bg);
  color: var(--color-text);
}
.bell-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}
.badge {
  position: absolute;
  top: 2px;
  right: 2px;
  background: var(--color-danger);
  color: white;
  font-size: 10px;
  font-weight: 600;
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 3px;
  border: 2px solid var(--color-surface);
}
.dropdown-overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
}
.dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 340px;
  max-height: 420px;
  overflow-y: auto;
  z-index: 201;
  padding: 0;
}
.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-md);
  border-bottom: 1px solid var(--color-border);
  font-size: 13px;
  font-weight: 600;
  position: sticky;
  top: 0;
  background: var(--color-surface);
}
.mark-read-btn {
  background: transparent;
  border: none;
  color: var(--color-primary);
  font-size: 12px;
  cursor: pointer;
  font-weight: 500;
  padding: 2px 4px;
  border-radius: var(--radius);
  transition: background 0.15s ease;
}
.mark-read-btn:hover {
  background: var(--color-primary-soft);
}
.dropdown-list {
  padding: var(--space-sm);
}
.empty-state {
  padding: var(--space-xl);
  text-align: center;
  color: var(--color-text-muted);
  font-size: 13px;
}
.notification-item {
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius);
  margin-bottom: 2px;
  transition: background 0.15s ease;
}
.notification-item.unread {
  background: var(--color-primary-soft);
}
.notification-message {
  font-size: 13px;
  color: var(--color-text);
  margin-bottom: 2px;
}
.notification-time {
  font-size: 11px;
  color: var(--color-text-muted);
}
</style>