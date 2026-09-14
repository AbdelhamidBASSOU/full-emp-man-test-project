import { defineStore } from 'pinia'
import api from '../api/axios'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    email: localStorage.getItem('email') || null,
    userType: localStorage.getItem('userType') || null,
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    isSuperAdmin: (state) => state.userType === 'SUPER_ADMIN',
  },

  actions: {
    async login(email, password) {
      const response = await api.post('/auth/login', { email, password })
      const { token, email: userEmail, userType } = response.data

      this.token = token
      this.email = userEmail
      this.userType = userType

      localStorage.setItem('token', token)
      localStorage.setItem('email', userEmail)
      localStorage.setItem('userType', userType)
    },

    logout() {
      this.token = null
      this.email = null
      this.userType = null

      localStorage.removeItem('token')
      localStorage.removeItem('email')
      localStorage.removeItem('userType')
    },
  },
})