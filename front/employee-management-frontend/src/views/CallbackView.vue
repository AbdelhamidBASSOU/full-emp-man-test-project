<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const error = ref('')

onMounted(async () => {
  try {
    await authStore.handleCallback()
    router.replace('/employees')
  } catch (err) {
    console.error('OIDC callback error:', err)
    error.value = 'Failed to complete sign-in. Redirecting to login...'
    setTimeout(() => {
      router.replace('/login')
    }, 2000)
  }
})
</script>

<template>
  <div class="callback-container">
    <div class="card callback-card">
      <div class="spinner"></div>
      <h2>Completing sign in...</h2>
      <p v-if="error" class="error-msg">{{ error }}</p>
      <p v-else class="status-msg">Please wait while we verify your session.</p>
    </div>
  </div>
</template>

<style scoped>
.callback-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--color-chrome);
}

.callback-card {
  padding: var(--space-2xl);
  text-align: center;
  max-width: 400px;
  width: 100%;
}

.spinner {
  width: 36px;
  height: 36px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto var(--space-lg);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

h2 {
  font-size: 18px;
  margin-bottom: var(--space-xs);
}

.status-msg {
  color: var(--color-text-muted);
  font-size: 14px;
}

.error-msg {
  color: var(--color-danger);
  font-size: 14px;
}
</style>
