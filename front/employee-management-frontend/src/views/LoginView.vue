<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../api/axios'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

// Forgot Password Modal state
const showForgotModal = ref(false)
const forgotEmail = ref('')
const forgotLoading = ref(false)
const forgotMessage = ref('')
const forgotError = ref('')

async function handleLogin() {
  if (!email.value || !password.value) {
    error.value = 'Please enter both email and password.'
    return
  }

  error.value = ''
  loading.value = true
  try {
    await authStore.loginWithCredentials(email.value, password.value)
    router.push('/employees')
  } catch (err) {
    console.error('Login error:', err)
    error.value = err.message || 'Invalid email or password.'
  } finally {
    loading.value = false
  }
}

function openForgotModal() {
  forgotEmail.value = email.value || ''
  forgotMessage.value = ''
  forgotError.value = ''
  showForgotModal.value = true
}

function closeForgotModal() {
  showForgotModal.value = false
}

async function handleForgotPassword() {
  if (!forgotEmail.value) {
    forgotError.value = 'Please enter your email address.'
    return
  }

  forgotError.value = ''
  forgotMessage.value = ''
  forgotLoading.value = true

  try {
    const res = await api.post('/auth/forgot-password', { email: forgotEmail.value })
    forgotMessage.value = res.data.message || 'Password reset instructions have been sent via Mailtrap.'
  } catch (err) {
    console.error('Forgot password error:', err)
    forgotError.value = err.response?.data?.message || 'Failed to send reset email. Please try again.'
  } finally {
    forgotLoading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-form card">
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
        &gt; enter credentials to sign in
      </p>

      <form @submit.prevent="handleLogin" class="auth-box">
        <p v-if="error" class="general-error">
          {{ error }}
        </p>

        <div class="form-group">
          <label class="form-label" for="email">Email address</label>
          <input
            id="email"
            v-model="email"
            type="email"
            required
            placeholder="admin1@company.com"
            class="input"
            :disabled="loading"
          />
        </div>

        <div class="form-group">
          <div class="label-row">
            <label class="form-label" for="password">Password</label>
            <button
              type="button"
              class="forgot-link mono"
              @click="openForgotModal"
            >
              Forgot password?
            </button>
          </div>
          <input
            id="password"
            v-model="password"
            type="password"
            required
            placeholder="••••••••"
            class="input"
            :disabled="loading"
          />
        </div>

        <button
          type="submit"
          class="btn btn-primary btn-full"
          :disabled="loading"
        >
          <svg v-if="!loading" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right: 8px;">
            <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
            <polyline points="10 17 15 12 10 7"/>
            <line x1="15" y1="12" x2="3" y2="12"/>
          </svg>
          {{ loading ? 'Authenticating...' : 'Sign In' }}
        </button>
      </form>
    </div>

    <!-- Forgot Password Modal -->
    <div v-if="showForgotModal" class="modal-backdrop" @click.self="closeForgotModal">
      <div class="modal card">
        <div class="modal-header">
          <h2>Reset Password</h2>
          <button type="button" class="btn-close" @click="closeForgotModal">&times;</button>
        </div>

        <p class="subtitle mono">
          &gt; enter your account email to receive a reset link via Mailtrap SMTP
        </p>

        <form @submit.prevent="handleForgotPassword" class="modal-body">
          <p v-if="forgotMessage" class="success-msg">
            ✓ {{ forgotMessage }}
          </p>
          <p v-if="forgotError" class="general-error">
            {{ forgotError }}
          </p>

          <div class="form-group">
            <label class="form-label" for="forgot-email">Account Email</label>
            <input
              id="forgot-email"
              v-model="forgotEmail"
              type="email"
              required
              placeholder="user2@company.com"
              class="input"
              :disabled="forgotLoading"
            />
          </div>

          <div class="modal-actions">
            <button
              type="button"
              class="btn btn-ghost"
              @click="closeForgotModal"
            >
              Cancel
            </button>
            <button
              type="submit"
              class="btn btn-primary"
              :disabled="forgotLoading"
            >
              {{ forgotLoading ? 'Sending...' : 'Send Reset Email' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--color-chrome);
  background-image:
    radial-gradient(
      circle at 1px 1px,
      rgba(255, 255, 255, 0.06) 1px,
      transparent 0
    );
  background-size: 24px 24px;
}

.login-form {
  padding: var(--space-xl);
  width: 400px;
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

.terminal-dots span:nth-child(1) {
  background: #EF4444;
  opacity: 0.6;
}

.terminal-dots span:nth-child(2) {
  background: #F59E0B;
  opacity: 0.6;
}

.terminal-dots span:nth-child(3) {
  background: #22C55E;
  opacity: 0.6;
}

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

.login-form h1 {
  font-size: 18px;
}

.subtitle {
  color: var(--color-text-muted);
  font-size: 13px;
  margin: var(--space-xs) 0 var(--space-lg) 0;
}

.auth-box {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.forgot-link {
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: 11px;
  cursor: pointer;
  padding: 0;
  text-decoration: underline;
}

.forgot-link:hover {
  opacity: 0.8;
}

.input {
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
}

.general-error {
  color: var(--color-danger);
  font-size: 13px;
  padding: 8px 12px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: var(--radius);
}

.success-msg {
  color: var(--color-success, #22C55E);
  font-size: 13px;
  padding: 8px 12px;
  background: rgba(34, 197, 94, 0.1);
  border: 1px solid rgba(34, 197, 94, 0.2);
  border-radius: var(--radius);
}

.btn-full {
  width: 100%;
  justify-content: center;
  text-align: center;
  padding: 11px var(--space-lg);
  font-size: 14px;
  font-weight: 600;
  margin-top: var(--space-xs);
}

.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  width: 420px;
  padding: var(--space-lg);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-close {
  background: none;
  border: none;
  color: var(--color-text-muted);
  font-size: 20px;
  cursor: pointer;
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  margin-top: var(--space-sm);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
  margin-top: var(--space-xs);
}
</style>
