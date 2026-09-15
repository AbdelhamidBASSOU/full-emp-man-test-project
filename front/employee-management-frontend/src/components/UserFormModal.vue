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
      <div class="modal-header">
        <div>
          <h2>{{ user ? 'Edit User' : 'Add User' }}</h2>
          <p v-if="user" class="modal-subtitle">{{ user.email }}</p>
        </div>
        <button class="modal-close btn-ghost btn" @click="$emit('close')" aria-label="Close">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="modal-body">
        <p v-if="generalError" class="general-error">{{ generalError }}</p>

        <div class="field">
          <label>Full Name <span class="required">*</span></label>
          <input v-model="form.name" class="input" :class="{ 'input-error': errors.name }" />
          <span v-if="errors.name" class="field-error">{{ errors.name }}</span>
        </div>

        <div class="field">
          <label>Email Address <span class="required">*</span></label>
          <input v-model="form.email" type="email" class="input" :class="{ 'input-error': errors.email }" />
          <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
        </div>

        <div class="field">
          <label>
            {{ user ? 'New Password' : 'Password' }}
            <span v-if="!user" class="required">*</span>
          </label>
          <input v-model="form.password" type="password" class="input" :class="{ 'input-error': errors.password }"
            :placeholder="user ? 'Leave blank to keep current' : ''" />
          <span v-if="errors.password" class="field-error">{{ errors.password }}</span>
        </div>

        <div class="field">
          <label>User Type</label>
          <select v-model="form.userType" class="input">
            <option value="NORMAL_USER">Normal User</option>
            <option value="SUPER_ADMIN">Super Administrator</option>
          </select>
          <p class="field-hint" v-if="form.userType === 'SUPER_ADMIN'">
            Super Admins have full access to all features and data.
          </p>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="$emit('close')">Cancel</button>
          <button type="submit" class="btn btn-primary" :disabled="saving">
            <svg v-if="saving" class="spinner" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
            </svg>
            {{ saving ? 'Saving…' : (user ? 'Save Changes' : 'Add User') }}
          </button>
        </div>
      </form>
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
  max-width: 440px;
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

.field { margin-bottom: var(--space-md); }
.field label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 5px;
}
.required { color: var(--color-danger); }
.field-error { display: block; color: var(--color-danger); font-size: 12px; margin-top: 4px; }
.field-hint { font-size: 12px; color: var(--color-text-muted); margin: 4px 0 0; }
.general-error {
  background: var(--color-danger-soft);
  color: var(--color-danger);
  font-size: 13px;
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius);
  margin-bottom: var(--space-md);
}
.input-error { border-color: var(--color-danger); }
.input-error:focus { box-shadow: 0 0 0 3px var(--color-danger-soft); }

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
  margin-top: var(--space-xl);
  padding-top: var(--space-md);
  border-top: 1px solid var(--color-border);
}

.spinner { animation: spin 0.8s linear infinite; }
@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}
</style>