import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // Initialize from localStorage
  const storedUserId = localStorage.getItem('userId')
  const storedUsername = localStorage.getItem('username')
  const storedToken = localStorage.getItem('token')

  const isLoggedIn = ref(!!storedToken)
  const username = ref(storedUsername || '')
  const userId = ref<number | null>(storedUserId ? Number(storedUserId) : null)

  const login = (user: { username: string; userId: number }) => {
    isLoggedIn.value = true
    username.value = user.username
    userId.value = user.userId
  }

  const logout = () => {
    isLoggedIn.value = false
    username.value = ''
    userId.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
  }

  return {
    isLoggedIn,
    username,
    userId,
    login,
    logout
  }
})
