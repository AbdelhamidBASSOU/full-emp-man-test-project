import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const api = axios.create({
  baseURL: '/api',
  withCredentials: true,

  headers: {
    'Content-Type': 'application/json',
  },

  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
})

/**
 * Read a cookie from document.cookie.
 */
function getCookie(name) {
  const cookies = document.cookie
    ? document.cookie.split('; ')
    : []

  for (const cookie of cookies) {
    const separatorIndex = cookie.indexOf('=')

    if (separatorIndex === -1) {
      continue
    }

    const key = decodeURIComponent(
      cookie.substring(0, separatorIndex),
    )

    const value = decodeURIComponent(
      cookie.substring(separatorIndex + 1),
    )

    if (key === name) {
      return value
    }
  }

  return null
}

/**
 * Make sure Spring Security has created the CSRF cookie.
 */
let csrfPromise = null

export async function ensureCsrfToken() {
  const existingToken = getCookie('XSRF-TOKEN')

  if (existingToken) {
    return existingToken
  }

  if (!csrfPromise) {
    csrfPromise = api
      .get('/auth/csrf')
      .then(() => getCookie('XSRF-TOKEN'))
      .finally(() => {
        csrfPromise = null
      })
  }

  return csrfPromise
}

/**
 * Only one refresh operation can run at a time.
 */
let refreshPromise = null

/**
 * Prevent authentication refreshes while logout is in progress.
 */
let logoutInProgress = false

function isPublicAuthRequest(url = '') {
  return (
    url.includes('/auth/login') ||
    url.includes('/auth/forgot-password') ||
    url.includes('/auth/reset-password') ||
    url.includes('/auth/csrf')
  )
}

/**
 * Request interceptor.
 */
api.interceptors.request.use(
  async (config) => {
    const authStore = useAuthStore()

    const url = config.url || ''

    /**
     * Protected API requests receive the access token.
     */
    if (
      !isPublicAuthRequest(url) &&
      !url.includes('/auth/refresh') &&
      !url.includes('/auth/logout') &&
      authStore.token
    ) {
      config.headers = config.headers || {}

      config.headers.Authorization =
        `Bearer ${authStore.token}`
    }

    /**
     * Refresh and logout use the HttpOnly refresh-token
     * cookie and therefore need the CSRF header.
     */
    if (
      url.includes('/auth/refresh') ||
      url.includes('/auth/logout')
    ) {
      const csrfToken = getCookie('XSRF-TOKEN')

      if (csrfToken) {
        config.headers = config.headers || {}

        config.headers['X-XSRF-TOKEN'] = csrfToken
      }
    }

    return config
  },

  (error) => Promise.reject(error),
)

/**
 * Response interceptor.
 *
 * If a protected request receives 401:
 *
 *   1. Refresh the access token.
 *   2. Store the new session.
 *   3. Retry the original request.
 */
api.interceptors.response.use(
  (response) => response,

  async (error) => {
    const originalRequest = error.config
    const status = error.response?.status

    if (!originalRequest) {
      return Promise.reject(error)
    }

    const url = originalRequest.url || ''

    const isLoginRequest =
      url.includes('/auth/login')

    const isRefreshRequest =
      url.includes('/auth/refresh')

    const isLogoutRequest =
      url.includes('/auth/logout')

    const isForgotPasswordRequest =
      url.includes('/auth/forgot-password')

    const isResetPasswordRequest =
      url.includes('/auth/reset-password')

    /**
     * Never attempt automatic refresh for authentication
     * endpoints themselves.
     */
    if (
      status !== 401 ||
      isLoginRequest ||
      isRefreshRequest ||
      isLogoutRequest ||
      isForgotPasswordRequest ||
      isResetPasswordRequest ||
      originalRequest._retry ||
      logoutInProgress
    ) {
      return Promise.reject(error)
    }

    originalRequest._retry = true

    const authStore = useAuthStore()

    /**
     * Reuse an already-running refresh request.
     */
    if (!refreshPromise) {
      refreshPromise = (async () => {
        try {
          await ensureCsrfToken()

          const response = await api.post(
            '/auth/refresh',
          )

          const {
            token,
            email,
            userType,
          } = response.data

          if (!token) {
            throw new Error(
              'Refresh response did not contain an access token.',
            )
          }

          /**
           * IMPORTANT:
           *
           * Use setSession() rather than assigning the
           * values directly. This also schedules the
           * next automatic refresh.
           */
          authStore.setSession(
            token,
            email,
            userType,
          )

          return token
        } catch (refreshError) {
          authStore.clearSession()

          throw refreshError
        } finally {
          refreshPromise = null
        }
      })()
    }

    try {
      const newToken = await refreshPromise

      originalRequest.headers =
        originalRequest.headers || {}

      originalRequest.headers.Authorization =
        `Bearer ${newToken}`

      return api(originalRequest)
    } catch (refreshError) {
      return Promise.reject(refreshError)
    }
  },
)

/**
 * Called by the auth store immediately before logout.
 *
 * This prevents an automatic refresh from starting while
 * logout is being performed.
 */
export function startLogout() {
  logoutInProgress = true
}

/**
 * Called after logout has completely finished.
 */
export function finishLogout() {
  logoutInProgress = false
}

export default api

