<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const showMenu = ref(false)

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="user-menu">
    <button class="user-trigger" @click="showMenu = !showMenu" aria-label="User menu">
      <div class="user-avatar">{{ authStore.email?.[0]?.toUpperCase() }}</div>
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <polyline points="6 9 12 15 18 9" />
      </svg>
    </button>

    <div v-if="showMenu" class="menu-overlay" @click="showMenu = false"></div>
    <div v-if="showMenu" class="menu-dropdown card">
      <div class="menu-header">
        <div class="user-avatar user-avatar-lg">{{ authStore.email?.[0]?.toUpperCase() }}</div>
        <div class="menu-user-info">
          <span class="menu-email">{{ authStore.email }}</span>
          <span class="menu-role mono">{{ authStore.isSuperAdmin ? 'super_admin' : 'user' }}</span>
        </div>
      </div>
      <div class="menu-divider"></div>
      <button class="menu-item menu-item-danger" @click="handleLogout">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
          <polyline points="16 17 21 12 16 7" />
          <line x1="21" y1="12" x2="9" y2="12" />
        </svg>
        Log out
      </button>
    </div>
  </div>
</template>

<style scoped>
.user-menu {
  position: relative;
}
.user-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: var(--radius);
  color: var(--color-text-muted);
  transition: background var(--transition-fast);
}
.user-trigger:hover {
  background: var(--color-bg);
}
.user-trigger:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}
.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}
.user-avatar-lg {
  width: 36px;
  height: 36px;
  font-size: 14px;
}
.menu-overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
}
.menu-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 220px;
  z-index: 201;
  padding: var(--space-sm);
}
.menu-header {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm);
}
.menu-user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.menu-email {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.menu-role {
  font-size: 11px;
  color: var(--color-text-muted);
}
.menu-divider {
  height: 1px;
  background: var(--color-border);
  margin: var(--space-sm) 0;
}
.menu-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 8px var(--space-sm);
  border-radius: var(--radius);
  font-size: 13px;
  color: var(--color-text);
  text-align: left;
  transition: background var(--transition-fast);
}
.menu-item:hover {
  background: var(--color-bg);
}
.menu-item:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: -2px;
}
.menu-item-danger {
  color: var(--color-danger);
}
</style>