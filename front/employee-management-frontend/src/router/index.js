import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

import LoginView from '../views/LoginView.vue'
import UsersView from '../views/UsersView.vue'
import EmployeesView from '../views/EmployeesView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
  },
  {
    path: '/',
    redirect: '/employees',
  },
  {
    path: '/users',
    name: 'users',
    component: UsersView,
    meta: { requiresAuth: true, requiresSuperAdmin: true },
  },
  {
    path: '/employees',
    name: 'employees',
    component: EmployeesView,
    meta: { requiresAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Route guard: runs before every navigation
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    // Not logged in, trying to access a protected page -> bounce to login
    next('/login')
  } else if (to.meta.requiresSuperAdmin && !authStore.isSuperAdmin) {
    // Logged in but not a super admin, trying to access Users page -> bounce to employees
    next('/employees')
  } else if (to.path === '/login' && authStore.isLoggedIn) {
    // Already logged in, trying to visit login page -> bounce to employees
    next('/employees')
  } else {
    next()
  }
})

export default router