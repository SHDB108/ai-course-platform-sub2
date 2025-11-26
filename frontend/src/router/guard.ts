import type { Router } from 'vue-router'
import { useUserStore } from '@/stores/user'

const whiteList = ['/login', '/register', '/404']

export function setupRouterGuard(router: Router) {
  router.beforeEach((to, from, next) => {
    const userStore = useUserStore()
    const token = localStorage.getItem('token')

    if (token) {
      if (to.path === '/login') {
        next({ path: '/' })
      } else {
        if (!userStore.isLoggedIn) {
          userStore.login({
            username: localStorage.getItem('username') || 'Student',
            userId: Number(localStorage.getItem('userId')) || 1
          })
        }
        next()
      }
    } else {
      if (whiteList.includes(to.path)) {
        next()
      } else {
        next()
      }
    }
  })

  router.afterEach((to) => {
    document.title = `${to.meta.title || 'Dashboard'} - Learning Cockpit`
  })
}
