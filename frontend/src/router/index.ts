import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import AppLayout from '@/layout/AppLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    meta: { title: 'Login' },
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/',
    component: AppLayout,
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        meta: { title: 'Dashboard' },
        component: () => import('@/views/dashboard/index.vue')
      },
      {
        path: '/courses',
        name: 'Courses',
        meta: { title: 'My Courses' },
        component: () => import('@/views/courses/index.vue')
      },
      {
        path: '/courses/:id',
        name: 'CourseDetail',
        meta: { title: 'Course Detail' },
        component: () => import('@/views/course/index.vue')
      },
      {
        path: '/recommendations',
        name: 'Recommendations',
        meta: { title: 'AI Recommendations' },
        component: () => import('@/views/recommendation/index.vue')
      },
      {
        path: '/study/video/:resourceId',
        name: 'VideoStudy',
        meta: { title: 'Video Learning' },
        component: () => import('@/views/study/video.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// Navigation guard for authentication
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')

  // If going to login page and already authenticated, redirect to dashboard
  if (to.path === '/login' && token) {
    next('/dashboard')
    return
  }

  // If not going to login page and not authenticated, redirect to login
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }

  next()
})

export default router
