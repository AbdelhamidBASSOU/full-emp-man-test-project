<script setup>
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import NavBar from './NavBar.vue'

const authStore = useAuthStore()

const collapsed = ref(localStorage.getItem('sidebarCollapsed') === 'true')
/** Mobile drawer open/closed — independent of desktop collapsed state */
const mobileOpen = ref(false)

function toggleSidebar() {
  collapsed.value = !collapsed.value
  localStorage.setItem('sidebarCollapsed', collapsed.value)
}

function openMobile() {
  mobileOpen.value = true
}

function closeMobile() {
  mobileOpen.value = false
}
</script>

<template>
  <div class="layout">
    <!-- Mobile drawer backdrop -->
    <div
      v-if="mobileOpen"
      class="mobile-backdrop"
      @click="closeMobile"
    ></div>

    <aside
      class="sidebar"
      :class="{
        collapsed,
        'mobile-open': mobileOpen,
      }"
    >
      <div class="sidebar-header">
        <div class="header-top">
          <div class="brand">
            <div class="brand-mark">EM</div>
            <span v-if="!collapsed" class="brand-text">Employee<br />Management</span>
          </div>
          <!-- Desktop collapse toggle -->
          <button
            class="fold-btn hide-mobile"
            @click="toggleSidebar"
            :title="collapsed ? 'Expand' : 'Collapse'"
          >
            <svg
              width="14"
              height="14"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2.5"
              :style="{ transform: collapsed ? 'rotate(180deg)' : 'none' }"
            >
              <polyline points="15 18 9 12 15 6" />
            </svg>
          </button>
          <!-- Mobile close button -->
          <button
            class="fold-btn hide-desktop"
            @click="closeMobile"
            title="Close menu"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>
      </div>

      <nav class="sidebar-nav">
        <router-link
          to="/employees"
          class="sidebar-link"
          :title="collapsed ? 'Employees' : ''"
          @click="closeMobile"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="2" y="7" width="20" height="14" rx="2" />
            <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" />
          </svg>
          <span v-if="!collapsed">Employees</span>
        </router-link>
        <router-link
          v-if="authStore.isSuperAdmin"
          to="/users"
          class="sidebar-link"
          :title="collapsed ? 'Users' : ''"
          @click="closeMobile"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
          </svg>
          <span v-if="!collapsed">Users</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="user-chip">
          <div class="user-avatar">{{ authStore.email?.[0]?.toUpperCase() }}</div>
          <div v-if="!collapsed" class="user-info">
            <span class="user-email">{{ authStore.email }}</span>
            <span class="user-role mono">{{ authStore.isSuperAdmin ? 'super_admin' : 'user' }}</span>
          </div>
        </div>
      </div>
    </aside>

    <div class="main-area">
      <NavBar @open-mobile-sidebar="openMobile" />
      <main class="content">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

/* ── Sidebar ─────────────────────────────────────────────── */
.sidebar {
  width: 220px;
  background: var(--color-chrome);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width 0.2s ease;
  z-index: 300;
}
.sidebar.collapsed {
  width: 64px;
}

/* ── Mobile sidebar ───────────────────────────────────────── */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    height: 100%;
    width: 260px;
    transform: translateX(-100%);
    transition: transform 0.25s ease;
    box-shadow: var(--shadow-lg);
  }
  .sidebar.mobile-open {
    transform: translateX(0);
  }
  .sidebar.collapsed {
    /* On mobile the collapsed state doesn't apply — drawer is full width */
    width: 260px;
  }
}

.mobile-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 299;
  backdrop-filter: blur(1px);
}

/* ── Sidebar header ───────────────────────────────────────── */
.sidebar-header {
  padding: var(--space-md);
  border-bottom: 1px solid var(--color-chrome-border);
  margin-bottom: var(--space-md);
}
.collapsed .sidebar-header {
  padding: var(--space-md) 12px;
}
.header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.collapsed .header-top {
  justify-content: center;
  flex-direction: column;
  gap: 8px;
}
.brand {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  min-width: 0;
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
.brand-text {
  color: var(--color-chrome-text-active);
  font-size: 13px;
  font-weight: 600;
  line-height: 1.25;
  white-space: nowrap;
}
.fold-btn {
  background: var(--color-chrome-elevated);
  border: none;
  color: var(--color-chrome-text);
  cursor: pointer;
  width: 22px;
  height: 22px;
  border-radius: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: color var(--transition-fast), background var(--transition-fast);
}
.collapsed .fold-btn {
  width: 100%;
}
.fold-btn:hover {
  color: var(--color-chrome-text-active);
  background: var(--color-chrome-border);
}
.fold-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}
.fold-btn svg {
  transition: transform 0.2s ease;
}

/* ── Nav links ───────────────────────────────────────────── */
.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 0 var(--space-sm);
  flex: 1;
}
.collapsed .sidebar-nav {
  padding: 0 12px;
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
  transition: background var(--transition-fast), color var(--transition-fast);
  white-space: nowrap;
}
.collapsed .sidebar-link {
  padding: 9px;
  justify-content: center;
}
.sidebar-link svg {
  flex-shrink: 0;
  opacity: 0.85;
}
.sidebar-link:hover {
  background: var(--color-chrome-elevated);
  color: var(--color-chrome-text-active);
}
.sidebar-link:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}
.sidebar-link.router-link-active {
  background: var(--color-primary);
  color: white;
}
.sidebar-link.router-link-active svg {
  opacity: 1;
}

/* ── Footer ──────────────────────────────────────────────── */
.sidebar-footer {
  padding: var(--space-md);
  border-top: 1px solid var(--color-chrome-border);
  margin-top: var(--space-md);
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}
.collapsed .sidebar-footer {
  padding: var(--space-md) 12px;
  justify-content: center;
}
.user-chip {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex: 1;
  min-width: 0;
}
.collapsed .user-chip {
  flex: 0;
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

/* ── Main area ───────────────────────────────────────────── */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.content {
  flex: 1;
  overflow-y: auto;
  background: var(--color-bg);
}
</style>