<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const authStore = useAuthStore()
const router = useRouter()

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    await authStore.login(email.value, password.value)
    router.push('/employees')
  } catch (err) {
    error.value = 'Invalid email or password'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-form card">
      <div class="terminal-dots">
        <span></span><span></span><span></span>
      </div>

      <div class="brand">
        <div class="brand-mark">EM</div>
        <h1>Employee Management</h1>
      </div>
      <p class="subtitle mono">&gt; authenticate to continue</p>

      <form @submit.prevent="handleLogin">
        <div class="field">
          <label>Email or Username</label>
          <input v-model="email" type="text" placeholder="you@company.com" class="input" required />
        </div>

        <div class="field">
          <label>Password</label>
          <input v-model="password" type="password" placeholder="••••••••" class="input" required />
        </div>

        <p v-if="error" class="general-error">{{ error }}</p>

        <button type="submit" class="btn btn-primary btn-full" :disabled="loading">
          {{ loading ? 'Signing in...' : 'Sign In' }}
        </button>
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
    radial-gradient(circle at 1px 1px, rgba(255,255,255,0.06) 1px, transparent 0);
  background-size: 24px 24px;
}
.login-form {
  padding: var(--space-xl);
  width: 380px;
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
.login-form h1 {
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
.general-error {
  color: var(--color-danger);
  font-size: 13px;
  margin-bottom: var(--space-md);
}
.btn-full {
  width: 100%;
  margin-top: var(--space-sm);
}
</style>