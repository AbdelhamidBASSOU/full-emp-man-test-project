<script setup>
import NotificationBell from './NotificationBell.vue'
import UserMenu from './UserMenu.vue'

const emit = defineEmits(['open-mobile-sidebar'])
</script>

<template>
  <header class="navbar">
    <div class="navbar-left">
      <!-- Mobile hamburger button -->
      <button
        class="hamburger hide-desktop"
        aria-label="Open sidebar"
        @click="emit('open-mobile-sidebar')"
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="3" y1="6" x2="21" y2="6" />
          <line x1="3" y1="12" x2="21" y2="12" />
          <line x1="3" y1="18" x2="21" y2="18" />
        </svg>
      </button>

      <!-- Workspace / System identity badge (replaces redundant duplicate title) -->
      <div class="workspace-pill">
        <div class="workspace-icon">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
        </div>
        <span class="workspace-name">Organization Portal</span>
        <span class="status-indicator" title="System online"></span>
      </div>
    </div>

    <div class="navbar-actions">
      <NotificationBell />
      <div class="navbar-divider"></div>
      <UserMenu />
    </div>
  </header>
</template>

<style scoped>
.navbar {
  height: 60px;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-xl);
  flex-shrink: 0;
  gap: var(--space-md);
}
@media (max-width: 768px) {
  .navbar {
    padding: 0 var(--space-md);
  }
}
.navbar-left {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  min-width: 0;
}

.workspace-pill {
  display: inline-flex;
  align-items: center;
  gap: var(--space-xs);
  padding: 5px 10px;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text);
  user-select: none;
}
.workspace-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
}
.workspace-name {
  white-space: nowrap;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text);
}
.status-indicator {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-success);
  margin-left: 2px;
  box-shadow: 0 0 0 2px var(--color-success-soft);
}

.hamburger {
  background: transparent;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background var(--transition-fast), color var(--transition-fast);
}
.hamburger:hover {
  background: var(--color-bg);
  color: var(--color-text);
}
.hamburger:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}
.navbar-actions {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  flex-shrink: 0;
}
.navbar-divider {
  width: 1px;
  height: 24px;
  background: var(--color-border);
}
</style>