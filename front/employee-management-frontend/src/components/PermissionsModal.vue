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
      canCreate.value = u.canCreate
      canRead.value = u.canRead
      canUpdate.value = u.canUpdate
      canDelete.value = u.canDelete
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
      <h2>Permissions — {{ user?.name }}</h2>
      <p class="subtitle">Control what this user can do with employee records.</p>

      <div class="checkbox-row">
        <label><input type="checkbox" v-model="canCreate" /> Create</label>
        <label><input type="checkbox" v-model="canRead" /> Read</label>
        <label><input type="checkbox" v-model="canUpdate" /> Update</label>
        <label><input type="checkbox" v-model="canDelete" /> Delete</label>
      </div>

      <p v-if="error" class="general-error">{{ error }}</p>

      <div class="modal-actions">
        <button type="button" class="btn btn-secondary" @click="$emit('close')">Cancel</button>
        <button type="button" class="btn btn-primary" :disabled="saving" @click="handleSave">
          {{ saving ? 'Saving...' : 'Save' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.overlay { position: fixed; inset: 0; background: rgba(28, 36, 48, 0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { width: 380px; padding: var(--space-xl); }
.modal h2 { font-size: 18px; margin-bottom: 4px; }
.subtitle { font-size: 13px; color: var(--color-text-muted); margin-bottom: var(--space-lg); }
.checkbox-row { display: flex; flex-direction: column; gap: var(--space-sm); margin-bottom: var(--space-md); }
.checkbox-row label { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.general-error { color: var(--color-danger); font-size: 13px; margin-bottom: var(--space-md); }
.modal-actions { display: flex; justify-content: flex-end; gap: var(--space-sm); margin-top: var(--space-lg); }
</style>