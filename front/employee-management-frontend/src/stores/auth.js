import { defineStore } from 'pinia'
import { userManager } from '../oidc'
import api from '../api/axios'
import { useToastStore } from './toast'

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

      // Check roles in realm_access claim
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
          // Silent renew failed / not logged in
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
