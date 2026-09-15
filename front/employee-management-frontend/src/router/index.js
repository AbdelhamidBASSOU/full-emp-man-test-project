import {
  createRouter,
  createWebHistory,
} from 'vue-router'

import { useAuthStore } from '../stores/auth'

import LoginView from '../views/LoginView.vue'
import ForgotPasswordView from '../views/ForgotPasswordView.vue'
import ResetPasswordView from '../views/ResetPasswordView.vue'
import UsersView from '../views/UsersView.vue'
import EmployeesView from '../views/EmployeesView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
  },

  {
    path: '/forgot-password',
    name: 'forgot-password',
    component: ForgotPasswordView,
  },

  {
    path: '/reset-password',
    name: 'reset-password',
    component: ResetPasswordView,
  },

  {
    path: '/',
    redirect: '/employees',
  },

  {
    path: '/users',
    name: 'users',
    component: UsersView,

    meta: {
      requiresAuth: true,
      requiresSuperAdmin: true,
      title: 'Users',
    },
  },

  {
    path: '/employees',
    name: 'employees',
    component: EmployeesView,

    meta: {
      requiresAuth: true,
      title: 'Employees',
    },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  /**
   * Protected route.
   */
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next({ name: 'login' })
    return
  }

  /**
   * Super Admin-only route.
   */
  if (to.meta.requiresSuperAdmin && !authStore.isSuperAdmin) {
    next({ name: 'employees' })
    return
  }

  /**
   * Logged-in users should not return to login.
   */
  if (to.name === 'login' && authStore.isLoggedIn) {
    next({ name: 'employees' })
    return
  }

  next()
})

export default router