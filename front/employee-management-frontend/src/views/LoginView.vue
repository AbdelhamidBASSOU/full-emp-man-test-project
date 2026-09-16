<script setup>
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    await authStore.login()
  } catch (err) {
    console.error('Redirect to login failed:', err)
    error.value = 'Failed to connect to identity provider. Please check if Keycloak is running.'
    loading.value = false
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
        &gt; authenticate with Keycloak to continue
      </p>

      <div class="auth-box">
        <p v-if="error" class="general-error">
          {{ error }}
        </p>

        <button
          type="button"
          class="btn btn-primary btn-full"
          :disabled="loading"
          @click="handleLogin"
        >
          <svg v-if="!loading" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right: 8px;">
            <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
            <polyline points="10 17 15 12 10 7"/>
            <line x1="15" y1="12" x2="3" y2="12"/>
          </svg>
          {{ loading ? 'Redirecting to login...' : 'Sign In with Keycloak' }}
        </button>

        <p class="hint-text">
          Authentication and single sign-on are managed securely via Keycloak.
        </p>
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
  margin: var(--space-xs) 0 var(--space-xl) 0;
}

.auth-box {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.general-error {
  color: var(--color-danger);
  font-size: 13px;
  margin-bottom: var(--space-xs);
}

.btn-full {
  width: 100%;
  justify-content: center;
  text-align: center;
  padding: 11px var(--space-lg);
  font-size: 14px;
  font-weight: 600;
}

.hint-text {
  font-size: 12px;
  color: var(--color-text-muted);
  text-align: center;
  line-height: 1.5;
  margin-top: var(--space-xs);
}
</style>
