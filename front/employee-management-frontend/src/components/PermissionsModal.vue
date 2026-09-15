<script setup>
import { ref, watch } from 'vue'
import { updatePermissions } from '../api/users'

const props = defineProps({
  user: { type: Object, default: null },
})
const emit = defineEmits(['close', 'saved'])

const canCreate = ref(false)
const canRead = ref(false)
const canUpdate = ref(false)
const canDelete = ref(false)
const saving = ref(false)
const error = ref('')

watch(
  () => props.user,
  (u) => {
    if (u) {
      canCreate.value = Boolean(u.canCreate)
      canRead.value = Boolean(u.canRead)
      canUpdate.value = Boolean(u.canUpdate)
      canDelete.value = Boolean(u.canDelete)
    }
  },
  { immediate: true }
)

async function handleSave() {
  saving.value = true
  error.value = ''
  try {
    await updatePermissions(props.user.id, {
      canCreate: canCreate.value,
      canRead: canRead.value,
      canUpdate: canUpdate.value,
      canDelete: canDelete.value,
    })
    emit('saved')
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to update permissions.'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="overlay" @click.self="$emit('close')">
    <div class="modal card">
      <div class="modal-header">
        <div>
          <h2>User Permissions</h2>
          <p class="modal-subtitle">{{ user?.name }} ({{ user?.email }})</p>
        </div>
        <button class="modal-close btn-ghost btn" @click="$emit('close')" aria-label="Close">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>

      <div class="modal-body">
        <p class="section-desc">Select which actions this user is allowed to perform on employee records.</p>
        
        <p v-if="error" class="general-error">{{ error }}</p>

        <div class="perm-grid">
          <label class="perm-card" :class="{ active: canCreate }">
            <input type="checkbox" v-model="canCreate" class="perm-checkbox" />
            <div class="perm-content">
              <span class="perm-title">Create Employees</span>
              <span class="perm-desc">Add new employee records to the system</span>
            </div>
          </label>

          <label class="perm-card" :class="{ active: canRead }">
            <input type="checkbox" v-model="canRead" class="perm-checkbox" />
            <div class="perm-content">
              <span class="perm-title">Read Employees</span>
              <span class="perm-desc">View employee details, CVs, and documents</span>
            </div>
          </label>

          <label class="perm-card" :class="{ active: canUpdate }">
            <input type="checkbox" v-model="canUpdate" class="perm-checkbox" />
            <div class="perm-content">
              <span class="perm-title">Update Employees</span>
              <span class="perm-desc">Edit details and upload photo or CV</span>
            </div>
          </label>

          <label class="perm-card" :class="{ active: canDelete }">
            <input type="checkbox" v-model="canDelete" class="perm-checkbox" />
            <div class="perm-content">
              <span class="perm-title">Delete Employees</span>
              <span class="perm-desc">Remove employee records permanently</span>
            </div>
          </label>
        </div>
      </div>

      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" @click="$emit('close')">Cancel</button>
        <button type="button" class="btn btn-primary" :disabled="saving" @click="handleSave">
          <svg v-if="saving" class="spinner" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
          </svg>
          {{ saving ? 'Saving…' : 'Save Permissions' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(19, 23, 34, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 400;
  padding: var(--space-md);
  backdrop-filter: blur(2px);
}
.modal {
  width: 100%;
  max-width: 460px;
  max-height: 92vh;
  overflow-y: auto;
  box-shadow: var(--shadow-lg);
  border-radius: var(--radius-lg);
}
@media (max-width: 600px) {
  .overlay { padding: 0; align-items: flex-end; }
  .modal {
    max-width: 100%;
    max-height: 96vh;
    border-bottom-left-radius: 0;
    border-bottom-right-radius: 0;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: var(--space-lg) var(--space-xl);
  border-bottom: 1px solid var(--color-border);
}
.modal-header h2 { font-size: 17px; }
.modal-subtitle { font-size: 13px; color: var(--color-text-muted); margin: 2px 0 0; }
.modal-close { color: var(--color-text-muted); padding: 4px; }

.modal-body { padding: var(--space-lg) var(--space-xl); }
.section-desc { font-size: 13px; color: var(--color-text-muted); margin-bottom: var(--space-md); }

.general-error {
  background: var(--color-danger-soft);
  color: var(--color-danger);
  font-size: 13px;
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius);
  margin-bottom: var(--space-md);
}

.perm-grid {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}
.perm-card {
  display: flex;
  align-items: flex-start;
  gap: var(--space-md);
  padding: var(--space-md);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  cursor: pointer;
  transition: background var(--transition-fast), border-color var(--transition-fast);
}
.perm-card:hover {
  border-color: #C9CDD6;
  background: var(--color-bg);
}
.perm-card.active {
  border-color: var(--color-primary);
  background: var(--color-primary-soft);
}
.perm-checkbox {
  margin-top: 2px;
  accent-color: var(--color-primary);
  width: 16px;
  height: 16px;
}
.perm-content {
  display: flex;
  flex-direction: column;
}
.perm-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
}
.perm-desc {
  font-size: 12px;
  color: var(--color-text-muted);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
  padding: var(--space-md) var(--space-xl);
  border-top: 1px solid var(--color-border);
  background: var(--color-surface);
}

.spinner { animation: spin 0.8s linear infinite; }
@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}
</style>