<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

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

function fillCredentials(demoEmail, demoPassword) {
  email.value = demoEmail
  password.value = demoPassword
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
          <label class="form-label" for="password">Password</label>
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

        <div class="demo-accounts">
          <span class="demo-title mono">&gt; quick fill demo account:</span>
          <div class="demo-buttons">
            <button
              type="button"
              class="btn btn-sm btn-ghost demo-btn"
              @click="fillCredentials('admin1@company.com', 'Admin123!')"
            >
              Super Admin
            </button>
            <button
              type="button"
              class="btn btn-sm btn-ghost demo-btn"
              @click="fillCredentials('user2@company.com', '151515')"
            >
              Normal User
            </button>
          </div>
        </div>
      </form>
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

.form-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
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

.btn-full {
  width: 100%;
  justify-content: center;
  text-align: center;
  padding: 11px var(--space-lg);
  font-size: 14px;
  font-weight: 600;
  margin-top: var(--space-xs);
}

.demo-accounts {
  margin-top: var(--space-sm);
  padding-top: var(--space-md);
  border-top: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.demo-title {
  font-size: 11px;
  color: var(--color-text-muted);
}

.demo-buttons {
  display: flex;
  gap: var(--space-xs);
}

.demo-btn {
  flex: 1;
  font-size: 12px;
  border: 1px dashed var(--color-border);
  color: var(--color-text-muted);
}

.demo-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-text);
  background: rgba(255, 255, 255, 0.04);
}
</style>
