import { defineStore } from 'pinia'
import api, {
  startLogout,
  finishLogout,
  ensureCsrfToken,
} from '../api/axios'

import { useToastStore } from './toast'
import { getTokenExpiryMs } from '../utils/jwt'

const WARNING_BEFORE_EXPIRY_MS = 5 * 60 * 1000
const REFRESH_BEFORE_EXPIRY_MS = 60 * 1000

let expiryWarningTimer = null
let automaticRefreshTimer = null

function clearAuthTimers() {
  if (expiryWarningTimer) {
    clearTimeout(expiryWarningTimer)
    expiryWarningTimer = null
  }

  if (automaticRefreshTimer) {
    clearTimeout(automaticRefreshTimer)
    automaticRefreshTimer = null
  }
}

function scheduleExpiryWarning(token) {
  if (expiryWarningTimer) {
    clearTimeout(expiryWarningTimer)
    expiryWarningTimer = null
  }

  const expiryMs = getTokenExpiryMs(token)
  if (!expiryMs) return

  const warnAt = expiryMs - WARNING_BEFORE_EXPIRY_MS
  const delay = warnAt - Date.now()
  if (delay <= 0) return

  expiryWarningTimer = setTimeout(() => {
    const toastStore = useToastStore()
    toastStore.show('Your session will refresh automatically.', 'warning')
  }, delay)
}

function scheduleAutomaticRefresh(token, refreshCallback) {
  if (automaticRefreshTimer) {
    clearTimeout(automaticRefreshTimer)
    automaticRefreshTimer = null
  }

  const expiryMs = getTokenExpiryMs(token)
  if (!expiryMs) return

  const refreshAt = expiryMs - REFRESH_BEFORE_EXPIRY_MS
  const delay = Math.max(refreshAt - Date.now(), 1000)

  automaticRefreshTimer = setTimeout(async () => {
    try {
      await refreshCallback()
    } catch {
      /* If refresh fails, auth flow handles session clear */
    }
  }, delay)
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: null,
    email: null,
    userType: null,
    canCreate: false,
    canRead: false,
    canUpdate: false,
    canDelete: false,
    initialized: false,
  }),

  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    isSuperAdmin: (state) => state.userType === 'SUPER_ADMIN',
  },

  actions: {
    setSession(token, email, userType, permissions = {}) {
      clearAuthTimers()

      this.token = token
      this.email = email
      this.userType = userType
      this.canCreate = Boolean(permissions.canCreate)
      this.canRead = Boolean(permissions.canRead)
      this.canUpdate = Boolean(permissions.canUpdate)
      this.canDelete = Boolean(permissions.canDelete)

      scheduleExpiryWarning(token)
      scheduleAutomaticRefresh(token, () => this.refreshAccessToken())
    },

    clearSession() {
      this.token = null
      this.email = null
      this.userType = null
      this.canCreate = false
      this.canRead = false
      this.canUpdate = false
      this.canDelete = false

      clearAuthTimers()
    },

    async login(email, password) {
      finishLogout()

      const response = await api.post('/auth/login', {
        email,
        password,
      })

      const {
        token,
        email: userEmail,
        userType,
        canCreate,
        canRead,
        canUpdate,
        canDelete,
        permissions,
      } = response.data

      if (!token) {
        throw new Error('The server did not return an access token.')
      }

      this.setSession(token, userEmail, userType, {
        canCreate: canCreate ?? permissions?.canCreate,
        canRead: canRead ?? permissions?.canRead,
        canUpdate: canUpdate ?? permissions?.canUpdate,
        canDelete: canDelete ?? permissions?.canDelete,
      })
      return response.data
    },

    async refreshAccessToken() {
      const response = await api.post('/auth/refresh')

      const {
        token,
        email,
        userType,
        canCreate,
        canRead,
        canUpdate,
        canDelete,
        permissions,
      } = response.data

      if (!token) {
        throw new Error('The server did not return a new access token.')
      }

      this.setSession(token, email, userType, {
        canCreate: canCreate ?? permissions?.canCreate,
        canRead: canRead ?? permissions?.canRead,
        canUpdate: canUpdate ?? permissions?.canUpdate,
        canDelete: canDelete ?? permissions?.canDelete,
      })
      return token
    },

    async tryRestoreSession() {
      try {
        await ensureCsrfToken()
        await this.refreshAccessToken()
        return true
      } catch {
        this.clearSession()
        return false
      } finally {
        this.initialized = true
      }
    },

    async fetchCurrentUser() {
      if (!this.token) return
      try {
        const response = await api.get('/auth/me')
        const {
          email: userEmail,
          userType,
          canCreate,
          canRead,
          canUpdate,
          canDelete,
        } = response.data

        this.email = userEmail
        this.userType = userType
        this.canCreate = Boolean(canCreate)
        this.canRead = Boolean(canRead)
        this.canUpdate = Boolean(canUpdate)
        this.canDelete = Boolean(canDelete)
      } catch {
        // ignore if network fails
      }
    },

    async logout() {
      startLogout()
      this.clearSession()

      try {
        await ensureCsrfToken()
        await api.post('/auth/logout')
      } catch (error) {
        console.warn('Server logout request failed:', error)
      } finally {
        this.initialized = true
        finishLogout()
      }
    },
  },
})
