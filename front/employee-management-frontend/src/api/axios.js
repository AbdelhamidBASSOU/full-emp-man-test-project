import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

let refreshPromise = null

api.interceptors.request.use(
  async (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    const status = error.response?.status

    if (!originalRequest || status !== 401 || originalRequest._retry) {
      return Promise.reject(error)
    }

    originalRequest._retry = true
    const authStore = useAuthStore()

    if (!refreshPromise) {
      refreshPromise = (async () => {
        try {
          const newToken = await authStore.refreshAccessToken()
          return newToken
        } catch (refreshErr) {
          authStore.clearSession()
          throw refreshErr
        } finally {
          refreshPromise = null
        }
      })()
    }

    try {
      const newToken = await refreshPromise
      if (newToken) {
        originalRequest.headers = originalRequest.headers || {}
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return api(originalRequest)
      }
      return Promise.reject(error)
    } catch (refreshErr) {
      return Promise.reject(refreshErr)
    }
  },
)

export default api
