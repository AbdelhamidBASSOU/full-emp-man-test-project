<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const router = useRouter()

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <div class="brand-mark">EM</div>
        <span>Employee<br />Management</span>
      </div>

      <nav class="sidebar-nav">
        <router-link to="/employees" class="sidebar-link">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="2" y="7" width="20" height="14" rx="2" />
            <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" />
          </svg>
          Employees
        </router-link>
        <router-link v-if="authStore.isSuperAdmin" to="/users" class="sidebar-link">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
          </svg>
          Users
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="user-chip">
          <div class="user-avatar">{{ authStore.email?.[0]?.toUpperCase() }}</div>
          <div class="user-info">
            <span class="user-email">{{ authStore.email }}</span>
            <span class="user-role mono">{{ authStore.isSuperAdmin ? 'super_admin' : 'user' }}</span>
          </div>
        </div>
        <button class="logout-btn" @click="handleLogout" title="Logout">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line x1="21" y1="12" x2="9" y2="12" />
          </svg>
        </button>
      </div>
    </aside>

    <main class="content">
      <slot />
    </main>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}
.sidebar {
  width: 220px;
  background: var(--color-chrome);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  padding: var(--space-lg) 0;
}
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: 0 var(--space-lg) var(--space-lg) var(--space-lg);
  border-bottom: 1px solid var(--color-chrome-border);
  margin-bottom: var(--space-md);
  color: var(--color-chrome-text-active);
  font-size: 13px;
  font-weight: 600;
  line-height: 1.3;
}
.brand-mark {
  width: 30px;
  height: 30px;
  border-radius: var(--radius);
  background: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  font-family: var(--font-mono);
  flex-shrink: 0;
}
.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 0 var(--space-sm);
  flex: 1;
}
.sidebar-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px var(--space-md);
  color: var(--color-chrome-text);
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  border-radius: var(--radius);
  transition: background 0.15s ease, color 0.15s ease;
}
.sidebar-link svg {
  flex-shrink: 0;
  opacity: 0.85;
}
.sidebar-link:hover {
  background: var(--color-chrome-elevated);
  color: var(--color-chrome-text-active);
}
.sidebar-link.router-link-active {
  background: var(--color-primary);
  color: white;
}
.sidebar-link.router-link-active svg {
  opacity: 1;
}
.sidebar-footer {
  padding: var(--space-md) var(--space-md) 0 var(--space-md);
  border-top: 1px solid var(--color-chrome-border);
  margin-top: var(--space-md);
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding-top: var(--space-md);
}
.user-chip {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex: 1;
  min-width: 0;
}
.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-chrome-elevated);
  color: var(--color-chrome-text-active);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}
.user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.user-email {
  font-size: 12px;
  color: var(--color-chrome-text-active);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.user-role {
  font-size: 10px;
  color: var(--color-primary);
}
.logout-btn {
  background: transparent;
  border: none;
  color: var(--color-chrome-text);
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.logout-btn:hover {
  background: var(--color-chrome-elevated);
  color: var(--color-danger);
}
.content {
  flex: 1;
  overflow-y: auto;
  background: var(--color-bg);
}
</style>