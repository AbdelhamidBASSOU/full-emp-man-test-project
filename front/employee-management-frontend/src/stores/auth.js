import { defineStore } from 'pinia'
import { userManager } from '../oidc'
import api from '../api/axios'
import { useToastStore } from './toast'

function parseJwt(token) {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
  } catch {
    return {}
  }
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
    setOidcUser(user) {
      if (!user || user.expired) {
        this.clearSession()
        return
      }

      this.token = user.access_token
      this.email = user.profile?.email || user.profile?.preferred_username || null

      const roles = user.profile?.realm_access?.roles || []
      if (roles.includes('SUPER_ADMIN')) {
        this.userType = 'SUPER_ADMIN'
      } else {
        this.userType = 'NORMAL_USER'
      }
    },

    clearSession() {
      this.token = null
      this.email = null
      this.userType = null
      this.canCreate = false
      this.canRead = false
      this.canUpdate = false
      this.canDelete = false
      sessionStorage.removeItem('kc_access_token')
      sessionStorage.removeItem('kc_refresh_token')
    },

    async loginWithCredentials(username, password) {
      const authority = import.meta.env.VITE_KEYCLOAK_AUTHORITY || 'http://localhost:9090/realms/employee-management'
      const clientId = import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'emp-frontend'
      const tokenUrl = `${authority}/protocol/openid-connect/token`

      const body = new URLSearchParams({
        client_id: clientId,
        grant_type: 'password',
        username: username,
        password: password,
        scope: 'openid profile email'
      })

      const response = await fetch(tokenUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body
      })

      if (!response.ok) {
        const errorJson = await response.json().catch(() => ({}))
        throw new Error(errorJson.error_description || 'Invalid email or password.')
      }

      const tokenData = await response.json()
      const payload = parseJwt(tokenData.access_token)

      this.token = tokenData.access_token
      this.email = payload.email || payload.preferred_username || username

      const roles = payload.realm_access?.roles || []
      if (roles.includes('SUPER_ADMIN')) {
        this.userType = 'SUPER_ADMIN'
      } else {
        this.userType = 'NORMAL_USER'
      }

      sessionStorage.setItem('kc_access_token', tokenData.access_token)
      if (tokenData.refresh_token) {
        sessionStorage.setItem('kc_refresh_token', tokenData.refresh_token)
      }

      await this.fetchCurrentUser()
      return tokenData
    },

    async login() {
      await userManager.signinRedirect()
    },

    async handleCallback() {
      const user = await userManager.signinCallback()
      this.setOidcUser(user)
      await this.fetchCurrentUser()
      return user
    },

    async refreshAccessToken() {
      const refreshToken = sessionStorage.getItem('kc_refresh_token')
      if (refreshToken) {
        try {
          const authority = import.meta.env.VITE_KEYCLOAK_AUTHORITY || 'http://localhost:9090/realms/employee-management'
          const clientId = import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'emp-frontend'
          const tokenUrl = `${authority}/protocol/openid-connect/token`

          const response = await fetch(tokenUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({
              client_id: clientId,
              grant_type: 'refresh_token',
              refresh_token: refreshToken
            })
          })

          if (response.ok) {
            const data = await response.json()
            this.token = data.access_token
            sessionStorage.setItem('kc_access_token', data.access_token)
            if (data.refresh_token) {
              sessionStorage.setItem('kc_refresh_token', data.refresh_token)
            }
            return data.access_token
          }
        } catch {
          // Fall through to OIDC refresh
        }
      }

      try {
        const user = await userManager.signinSilent()
        this.setOidcUser(user)
        return user.access_token
      } catch (err) {
        this.clearSession()
        throw err
      }
    },

    async tryRestoreSession() {
      try {
        const savedToken = sessionStorage.getItem('kc_access_token')
        if (savedToken) {
          const payload = parseJwt(savedToken)
          const exp = payload.exp ? payload.exp * 1000 : 0
          if (exp > Date.now()) {
            this.token = savedToken
            this.email = payload.email || payload.preferred_username || null
            const roles = payload.realm_access?.roles || []
            this.userType = roles.includes('SUPER_ADMIN') ? 'SUPER_ADMIN' : 'NORMAL_USER'
            await this.fetchCurrentUser()
            return true
          } else {
            try {
              const newToken = await this.refreshAccessToken()
              if (newToken) {
                await this.fetchCurrentUser()
                return true
              }
            } catch {
              this.clearSession()
            }
          }
        }

        let user = await userManager.getUser()
        if (user && !user.expired) {
          this.setOidcUser(user)
          await this.fetchCurrentUser()
          return true
        }

        try {
          user = await userManager.signinSilent()
          if (user && !user.expired) {
            this.setOidcUser(user)
            await this.fetchCurrentUser()
            return true
          }
        } catch {
          // Silent renew failed
        }

        this.clearSession()
        return false
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

        if (userEmail) this.email = userEmail
        if (userType) this.userType = userType
        this.canCreate = Boolean(canCreate)
        this.canRead = Boolean(canRead)
        this.canUpdate = Boolean(canUpdate)
        this.canDelete = Boolean(canDelete)
      } catch (err) {
        console.warn('Failed to fetch user permissions:', err)
      }
    },

    async logout() {
      this.clearSession()
      try {
        await userManager.signoutRedirect()
      } catch (err) {
        console.warn('OIDC logout failed:', err)
      } finally {
        this.initialized = true
      }
    },

    initOidcEvents() {
      const toastStore = useToastStore()

      userManager.events.addAccessTokenExpiring(() => {
        toastStore.show('Your session will refresh automatically.', 'warning')
      })

      userManager.events.addUserLoaded((user) => {
        this.setOidcUser(user)
      })

      userManager.events.addUserSignedOut(() => {
        this.clearSession()
      })
    },
  },
})
