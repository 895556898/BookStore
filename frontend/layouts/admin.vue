<template>
  <div class="admin-container">
    <el-container>
      <el-aside width="250px">
        <div class="sidebar-header">
          <h2>管理后台</h2>
        </div>
        <el-menu
          :default-active="activeIndex"
          class="el-menu-vertical"
          :router="true"
        >
          <el-menu-item index="/admin/books">
            <el-icon><Document /></el-icon>
            <span>图书管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/stats">
            <el-icon><Histogram /></el-icon>
            <span>销售统计</span>
          </el-menu-item>
          <el-menu-item index="/">
            <el-icon><Back /></el-icon>
            <span>返回商城</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header>
          <div class="admin-header">
            <h3>{{ pageTitle }}</h3>
            <div class="user-info">
              <el-dropdown trigger="click">
                <span class="admin-dropdown-link">
                  管理员: {{ username }} <el-icon><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>
        
        <el-main>
          <slot />
        </el-main>
        
        <el-footer>
          <p>&copy; {{ new Date().getFullYear() }} 线上书城管理系统</p>
        </el-footer>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Back, Document, List, Histogram, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const username = ref('')
const activeIndex = computed(() => route.path)

const pageTitle = computed(() => {
  switch (route.path) {
    case '/admin/books':
      return '图书管理'
    case '/admin/orders':
      return '订单管理'
    case '/admin/stats':
      return '销售统计'
    default:
      return '管理后台'
  }
})

onMounted(() => {
  // 检查用户是否已登录及是否为管理员
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    const user = JSON.parse(userInfo)
    username.value = user.username
    
    // 如果不是管理员，重定向到首页
    if (user.role !== 'ADMIN') {
      ElMessage.error('无权访问管理后台')
      router.push('/')
    }
  } else {
    ElMessage.error('请先登录')
    router.push('/login')
  }
})

const logout = async () => {
  try {
    const response = await fetch('/api/user/logout', {
      method: 'POST',
      credentials: 'include'
    })
    const result = await response.json()
    
    if (result.code === 1003) { // 假设1003是登出成功状态码
      localStorage.removeItem('userInfo')
      ElMessage.success('退出成功')
      router.push('/login')
    } else {
      ElMessage.error('退出失败')
    }
  } catch (error) {
    console.error('退出失败:', error)
    ElMessage.error('退出失败')
  }
}
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
}

.sidebar-header {
  padding: 20px 0;
  text-align: center;
  background-color: #409EFF;
  color: white;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 1.5rem;
}

.el-menu-vertical {
  border-right: none;
  height: calc(100vh - 60px);
}

.el-aside {
  background-color: #263445;
  box-shadow: 2px 0 6px rgba(0, 0, 0, 0.1);
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.admin-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #eee;
  line-height: 60px;
  padding: 0;
}

.el-main {
  padding: 20px;
  background-color: #f5f7fa;
}

.el-footer {
  padding: 20px;
  text-align: center;
  color: #666;
  background-color: #f5f5f5;
}
</style> 