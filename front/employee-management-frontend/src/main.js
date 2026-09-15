import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import './style.css'

import { useAuthStore } from './stores/auth'

async function bootstrap() {
  const app = createApp(App)

  const pinia = createPinia()
  app.use(pinia)

  const authStore = useAuthStore(pinia)

  /**
   * Try restoring session with a 1.5s timeout.
   * If XSRF-TOKEN cookie exists, ensureCsrfToken() skips the extra GET request,
   * making it 1 round-trip. If not logged in or backend down, it resolves fast (1.5s max).
   */
  try {
    await Promise.race([
      authStore.tryRestoreSession(),
      new Promise((resolve) => setTimeout(resolve, 1500)),
    ])
  } catch (error) {
    console.warn('Session restore failed:', error)
  }

  app.use(router)

  await router.isReady()

  app.mount('#app')
}

bootstrap().catch((error) => {
  console.error('Failed to start the application:', error)
})
