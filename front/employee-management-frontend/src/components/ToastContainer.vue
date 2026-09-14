<script setup>
import { useToastStore } from '../stores/toast'

const toastStore = useToastStore()
</script>

<template>
  <div class="toast-stack">
    <transition-group name="toast">
      <div v-for="toast in toastStore.toasts" :key="toast.id" class="toast" :class="`toast-${toast.type}`">
        <svg v-if="toast.type === 'success'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <polyline points="20 6 9 17 4 12" />
        </svg>
        <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="8" x2="12" y2="12" />
          <line x1="12" y1="16" x2="12.01" y2="16" />
        </svg>
        <span>{{ toast.message }}</span>
        <button class="toast-close" @click="toastStore.remove(toast.id)">×</button>
      </div>
    </transition-group>
  </div>
</template>

<style scoped>
.toast-stack {
  position: fixed;
  bottom: var(--space-lg);
  right: var(--space-lg);
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
  z-index: 1000;
}
.toast {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: 12px var(--space-md);
  border-radius: var(--radius);
  background: var(--color-chrome);
  color: white;
  font-size: 13px;
  box-shadow: 0 4px 16px rgba(19, 23, 34, 0.25);
  min-width: 260px;
}
.toast-success svg {
  color: var(--color-success);
  flex-shrink: 0;
}
.toast-error svg {
  color: var(--color-danger);
  flex-shrink: 0;
}
.toast span {
  flex: 1;
}
.toast-close {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  padding: 0 0 0 var(--space-sm);
}
.toast-close:hover {
  color: white;
}
.toast-enter-active, .toast-leave-active {
  transition: all 0.2s ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(20px);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>