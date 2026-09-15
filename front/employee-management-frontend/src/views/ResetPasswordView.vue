<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api/axios'

const route = useRoute()
const router = useRouter()

const newPassword = ref('')
const confirmPassword = ref('')
const error = ref('')
const success = ref('')
const loading = ref(false)

const token = computed(() => {
  const queryToken = route.query.token

  if (Array.isArray(queryToken)) {
    return queryToken[0] || ''
  }

  return typeof queryToken === 'string' ? queryToken : ''
})

async function handleResetPassword() {
  error.value = ''
  success.value = ''

  if (!token.value) {
    error.value = 'Invalid or missing password reset link.'
    return
  }

  if (newPassword.value.length < 8) {
    error.value = 'Password must be at least 8 characters long.'
    return
  }

  if (newPassword.value !== confirmPassword.value) {
    error.value = 'Passwords do not match.'
    return
  }

  loading.value = true

  try {
    const response = await api.post('/auth/reset-password', {
      token: token.value,
      newPassword: newPassword.value,
    })

    success.value = response.data || 'Password has been reset successfully.'
    newPassword.value = ''
    confirmPassword.value = ''

    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (err) {
    error.value =
      err.response?.data?.message || err.message || 'Unable to reset password. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="reset-container">
    <div class="reset-form card">
      <div class="terminal-dots">
        <span></span>
        <span></span>
        <span></span>
      </div>

      <div class="brand">
        <div class="brand-mark">EM</div>
        <h1>Employee Management</h1>
      </div>

      <p class="subtitle mono">
        &gt; reset your password
      </p>

      <div v-if="!token" class="error-box">
        Invalid or missing password reset link.
      </div>

      <form v-else @submit.prevent="handleResetPassword">
        <div class="field">
          <label for="new-password">New Password</label>
          <input
            id="new-password"
            v-model="newPassword"
            type="password"
            placeholder="Enter new password"
            class="input"
            minlength="8"
            autocomplete="new-password"
            required
          />
          <small class="hint">
            Password must be at least 8 characters.
          </small>
        </div>

        <div class="field">
          <label for="confirm-password">Confirm Password</label>
          <input
            id="confirm-password"
            v-model="confirmPassword"
            type="password"
            placeholder="Confirm new password"
            class="input"
            minlength="8"
            autocomplete="new-password"
            required
          />
        </div>

        <p v-if="error" class="error-message">
          {{ error }}
        </p>

        <p v-if="success" class="success-message">
          {{ success }}
        </p>

        <button
          type="submit"
          class="btn btn-primary btn-full"
          :disabled="loading"
        >
          {{ loading ? 'Resetting password...' : 'Reset Password' }}
        </button>
      </form>

      <div class="back-to-login">
        <router-link to="/login">
          ← Back to login
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.reset-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: var(--space-md);
  background: var(--color-chrome);
  background-image:
    radial-gradient(
      circle at 1px 1px,
      rgba(255, 255, 255, 0.06) 1px,
      transparent 0
    );
  background-size: 24px 24px;
}

.reset-form {
  width: 380px;
  max-width: 100%;
  padding: var(--space-xl);
  position: relative;
}

.terminal-dots {
  display: flex;
  gap: 6px;
  margin-bottom: var(--space-lg);
}

.terminal-dots span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-border);
}

.terminal-dots span:nth-child(1) { background: #EF4444; opacity: 0.6; }
.terminal-dots span:nth-child(2) { background: #F59E0B; opacity: 0.6; }
.terminal-dots span:nth-child(3) { background: #22C55E; opacity: 0.6; }

.brand {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-bottom: 4px;
}

.brand-mark {
  width: 32px;
  height: 32px;
  border-radius: var(--radius);
  background: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  font-family: var(--font-mono);
  flex-shrink: 0;
}

.reset-form h1 {
  font-size: 18px;
}

.subtitle {
  color: var(--color-text-muted);
  font-size: 13px;
  margin: var(--space-xs) 0 var(--space-xl) 0;
}

.field {
  margin-bottom: var(--space-md);
}

.field label {
  display: block;
  font-size: 13px;
  color: var(--color-text-muted);
  margin-bottom: 4px;
}

.hint {
  display: block;
  margin-top: 5px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.error-message,
.error-box {
  color: var(--color-danger);
  font-size: 13px;
  margin-bottom: var(--space-md);
}

.success-message {
  color: var(--color-success, #22C55E);
  font-size: 13px;
  margin-bottom: var(--space-md);
}

.error-box {
  padding: var(--space-md);
  border: 1px solid var(--color-danger);
  border-radius: var(--radius);
  background: rgba(239, 68, 68, 0.08);
}

.btn-full {
  width: 100%;
  justify-content: center;
  text-align: center;
  margin-top: var(--space-sm);
}

.back-to-login {
  text-align: center;
  margin-top: var(--space-lg);
}

.back-to-login a {
  color: var(--color-primary);
  font-size: 13px;
  text-decoration: none;
}

.back-to-login a:hover {
  text-decoration: underline;
}
</style>
