import {
  createRouter,
  createWebHistory,
} from 'vue-router'

import { useAuthStore } from '../stores/auth'

import LoginView from '../views/LoginView.vue'
import CallbackView from '../views/CallbackView.vue'
import UsersView from '../views/UsersView.vue'
import EmployeesView from '../views/EmployeesView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
  },

  {
    path: '/callback',
    name: 'callback',
    component: CallbackView,
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

  // Allow callback processing route without interruption
  if (to.name === 'callback') {
    next()
    return
  }

  // Protected route
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next({ name: 'login' })
    return
  }

  // Super Admin-only route
  if (to.meta.requiresSuperAdmin && !authStore.isSuperAdmin) {
    next({ name: 'employees' })
    return
  }

  // Logged-in users should not return to login
  if (to.name === 'login' && authStore.isLoggedIn) {
    next({ name: 'employees' })
    return
  }

  next()
})

export default router