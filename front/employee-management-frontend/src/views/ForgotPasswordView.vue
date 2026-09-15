<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/axios'

const email = ref('')
const error = ref('')
const success = ref('')
const loading = ref(false)

const router = useRouter()

async function handleForgotPassword() {
  error.value = ''
  success.value = ''

  if (!email.value.trim()) {
    error.value = 'Please enter your email address.'
    return
  }

  loading.value = true

  try {
    const response = await api.post('/auth/forgot-password', {
      email: email.value.trim(),
    })

    success.value =
      response.data ||
      'If that email is registered, a reset link has been sent.'
  } catch (err) {
    error.value =
      err.response?.data?.message || err.message || 'Unable to process your request.'
  } finally {
    loading.value = false
  }
}

function goBackToLogin() {
  router.push('/login')
}
</script>

<template>
  <div class="forgot-password-container">
    <div class="forgot-password-form card">
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

      <div class="header">
        <h2>Forgot Password?</h2>
        <p>
          Enter your email address and we'll send you a password reset link.
        </p>
      </div>

      <form @submit.prevent="handleForgotPassword">
        <div class="field">
          <label for="email">Email Address</label>
          <input
            id="email"
            v-model="email"
            type="email"
            placeholder="you@company.com"
            class="input"
            autocomplete="email"
            required
          />
        </div>

        <p v-if="error" class="general-error" role="alert">
          {{ error }}
        </p>

        <div v-if="success" class="success-box" role="status">
          <p class="success-message">
            {{ success }}
          </p>
          <p class="success-help">
            Check your inbox and follow the link in the email to create a new password.
          </p>
        </div>

        <button
          type="submit"
          class="btn btn-primary btn-full"
          :disabled="loading"
        >
          {{ loading ? 'Sending...' : 'Send Reset Link' }}
        </button>
      </form>

      <button
        type="button"
        class="back-button"
        @click="goBackToLogin"
      >
        ← Back to Sign In
      </button>
    </div>
  </div>
</template>

<style scoped>
.forgot-password-container {
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

.forgot-password-form {
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

.terminal-dots span:nth-child(1) { background: #ef4444; opacity: 0.6; }
.terminal-dots span:nth-child(2) { background: #f59e0b; opacity: 0.6; }
.terminal-dots span:nth-child(3) { background: #22c55e; opacity: 0.6; }

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

.forgot-password-form h1 {
  font-size: 18px;
}

.subtitle {
  color: var(--color-text-muted);
  font-size: 13px;
  margin: var(--space-xs) 0 var(--space-xl) 0;
}

.header {
  margin-bottom: var(--space-lg);
}

.header h2 {
  margin: 0 0 var(--space-xs) 0;
  font-size: 20px;
}

.header p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 13px;
  line-height: 1.6;
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

.general-error {
  color: var(--color-danger);
  font-size: 13px;
  margin-bottom: var(--space-md);
  line-height: 1.5;
}

.success-box {
  padding: var(--space-md);
  margin-bottom: var(--space-md);
  border: 1px solid rgba(34, 197, 94, 0.25);
  border-radius: var(--radius);
  background: rgba(34, 197, 94, 0.05);
}

.success-message {
  color: var(--color-success, #22c55e);
  font-size: 13px;
  margin: 0 0 6px;
  line-height: 1.5;
}

.success-help {
  color: var(--color-text-muted);
  font-size: 12px;
  margin: 0;
  line-height: 1.5;
}

.btn-full {
  width: 100%;
  justify-content: center;
  text-align: center;
  margin-top: var(--space-sm);
}

.back-button {
  display: block;
  width: 100%;
  margin-top: var(--space-lg);
  padding: 0;
  border: none;
  background: transparent;
  color: var(--color-primary);
  font-size: 13px;
  cursor: pointer;
  text-align: center;
}

.back-button:hover {
  text-decoration: underline;
}
</style>
