import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    isLoggedIn: false,
    token: null,
    sessionChecked: false
  }),
  
  getters: {
    getUser: (state) => state.user,
    getUsername: (state) => state.user?.username || '',
    getUserRole: (state) => state.user?.role || '',
    getIsLoggedIn: (state) => state.isLoggedIn,
    getToken: (state) => state.token
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
      this.token = null
      // 从localStorage中移除用户信息和token
      localStorage.removeItem('user')
      localStorage.removeItem('token')
    },
    
    // 从localStorage中初始化用户状态
    initUserFromStorage() {
      const userStr = localStorage.getItem('user')
      const token = localStorage.getItem('token')
      
      if (token) {
        this.token = token
      }
      
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
    
    // 检查会话状态
    async checkSession() {
      if (this.sessionChecked) return this.isLoggedIn
      
      // 只有当本地存储中有用户信息才需要验证
      if (!this.user && !localStorage.getItem('user')) {
        this.sessionChecked = true
        return false
      }
      
      try {
        const baseUrl = process.env.BASE_URL || 'http://localhost:8080'
        const response = await fetch(`${baseUrl}/api/user/check`, {
          method: 'GET',
          credentials: 'include',
          headers: this.getAuthHeaders()
        })
        
        if (response.ok) {
          const result = await response.json()
          if (result.code === 200) {
            this.sessionChecked = true
            return true
          }
        }
        
        // 会话验证失败，清除本地状态
        this.clearUser()
        this.sessionChecked = true
        return false
      } catch (error) {
        console.error('Session check failed:', error)
        this.sessionChecked = true
        return false
      }
    },
    
    // 获取带认证的请求头
    getAuthHeaders() {
      const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
      
      if (this.token) {
        headers['Authorization'] = `Bearer ${this.token}`
      }
      
      return headers
    },
    
    // 登出
    async logout() {
      try {
        const baseUrl = process.env.BASE_URL || 'http://localhost:8080'
        const response = await fetch(`${baseUrl}/api/user/logout`, {
          method: 'POST',
          credentials: 'include',
          headers: this.getAuthHeaders()
        })
        const result = await response.json()
        
        this.clearUser()
        return result
      } catch (error) {
        console.error('登出失败:', error)
        this.clearUser()
        throw error
      }
    }
  }
}) 