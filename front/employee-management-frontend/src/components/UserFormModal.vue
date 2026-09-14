<script setup>
import { ref, watch } from 'vue'
import { createUser, updateUser } from '../api/users'

const props = defineProps({
  user: { type: Object, default: null },
})
const emit = defineEmits(['close', 'saved'])

const form = ref({
  name: '',
  email: '',
  password: '',
  userType: 'NORMAL_USER',
})

const errors = ref({})
const saving = ref(false)
const generalError = ref('')

watch(
  () => props.user,
  (u) => {
    if (u) {
      form.value = { name: u.name, email: u.email, password: '', userType: u.userType }
    }
  },
  { immediate: true }
)

async function handleSubmit() {
  errors.value = {}
  generalError.value = ''
  saving.value = true

  try {
    if (props.user) {
      await updateUser(props.user.id, form.value)
    } else {
      await createUser(form.value)
    }
    emit('saved')
  } catch (err) {
    if (err.response?.status === 400 && typeof err.response.data === 'object') {
      errors.value = err.response.data
    } else {
      generalError.value = err.response?.data?.message || 'Something went wrong. Please try again.'
    }
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="overlay" @click.self="$emit('close')">
    <div class="modal card">
      <h2>{{ user ? 'Edit User' : 'Add User' }}</h2>

      <form @submit.prevent="handleSubmit">
        <div class="field">
          <label>Name</label>
          <input v-model="form.name" class="input" />
          <span v-if="errors.name" class="field-error">{{ errors.name }}</span>
        </div>

        <div class="field">
          <label>Email</label>
          <input v-model="form.email" type="email" class="input" />
          <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
        </div>

        <div class="field">
          <label>{{ user ? 'New Password (leave blank to keep current)' : 'Password' }}</label>
          <input v-model="form.password" type="password" class="input" />
          <span v-if="errors.password" class="field-error">{{ errors.password }}</span>
        </div>

        <div class="field">
          <label>User Type</label>
          <select v-model="form.userType" class="input">
            <option value="NORMAL_USER">Normal User</option>
            <option value="SUPER_ADMIN">Super Administrator</option>
          </select>
        </div>

        <p v-if="generalError" class="general-error">{{ generalError }}</p>

        <div class="modal-actions">
          <button type="button" class="btn btn-secondary" @click="$emit('close')">Cancel</button>
          <button type="submit" class="btn btn-primary" :disabled="saving">
            {{ saving ? 'Saving...' : 'Save' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.overlay { position: fixed; inset: 0; background: rgba(28, 36, 48, 0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { width: 420px; max-height: 90vh; overflow-y: auto; padding: var(--space-xl); }
.modal h2 { font-size: 18px; margin-bottom: var(--space-lg); }
.field { margin-bottom: var(--space-md); }
.field label { display: block; font-size: 13px; color: var(--color-text-muted); margin-bottom: 4px; }
.field-error { display: block; color: var(--color-danger); font-size: 12px; margin-top: 4px; }
.general-error { color: var(--color-danger); font-size: 13px; margin-bottom: var(--space-md); }
.modal-actions { display: flex; justify-content: flex-end; gap: var(--space-sm); margin-top: var(--space-lg); }
</style>