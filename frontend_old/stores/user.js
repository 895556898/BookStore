import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    isLoggedIn: false
  }),
  
  getters: {
    getUser: (state) => state.user,
    getUsername: (state) => state.user?.username || '',
    getUserRole: (state) => state.user?.role || '',
    getIsLoggedIn: (state) => state.isLoggedIn
  },
  
  actions: {
    // 设置用户信息
    setUser(user) {
      this.user = user
      this.isLoggedIn = true
      // 将用户信息存储到localStorage
      localStorage.setItem('user', JSON.stringify(user))
    },
    
    // 清除用户信息
    clearUser() {
      this.user = null
      this.isLoggedIn = false
      // 从localStorage中移除用户信息
      localStorage.removeItem('user')
    },
    
    // 从localStorage中初始化用户状态
    initUserFromStorage() {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        try {
          const user = JSON.parse(userStr)
          this.user = user
          this.isLoggedIn = true
        } catch (error) {
          console.error('Failed to parse user from localStorage:', error)
          this.clearUser()
        }
      }
    },
    
    // 登出
    async logout() {
      try {
        const response = await fetch('/api/user/logout', {
          method: 'POST',
          credentials: 'include'
        })
        const result = await response.json()
        
        // 无论后端是否成功，都清除前端状态
        this.clearUser()
        return result
      } catch (error) {
        console.error('登出失败:', error)
        // 即使API失败，也清除本地状态
        this.clearUser()
        throw error
      }
    }
  }
}) 