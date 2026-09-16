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
  authStore.initOidcEvents()

  // Try restoring existing session from storage/silent renew (1.5s timeout)
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
