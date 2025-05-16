<template>
  <div class="app-container">
    <el-container>
      <el-header>
        <div class="header-container">
          <div class="logo">
            <NuxtLink to="/" class="logo-link">
              <h1>小小书店</h1>
            </NuxtLink>
          </div>
          <div class="nav-menu">
            <el-menu
              mode="horizontal"
              :router="true"
              :default-active="activeIndex"
            >
              <el-menu-item index="/">首页</el-menu-item>
              <el-menu-item index="/books">图书</el-menu-item>
              <el-menu-item index="/cart">购物车</el-menu-item>
              <template v-if="userStore.getUserRole === 'admin'">
                <el-menu-item index="/admin/books">图书管理</el-menu-item>
                <el-menu-item index="/admin/orders">订单管理</el-menu-item>
                <el-menu-item index="/admin/stats">销售统计</el-menu-item>
              </template>
            </el-menu>
          </div>
          <div class="user-actions">
            <template v-if="userStore.getIsLoggedIn">
              <el-dropdown trigger="click">
                <span class="user-dropdown-link">
                  {{ userStore.getUsername }} <el-icon class="el-icon--right"><arrow-down /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="navigateTo('/user/orders')">
                      <el-icon><shopping-bag /></el-icon> 我的订单
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="handleLogout">
                      <el-icon><switch-button /></el-icon> 退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <NuxtLink to="/login">
                <el-button type="primary" plain>登录</el-button>
              </NuxtLink>
              <NuxtLink to="/register">
                <el-button type="primary">注册</el-button>
              </NuxtLink>
            </template>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <slot />
      </el-main>
      
      <el-footer>
        <div class="footer-container">
          <p>&copy; {{ new Date().getFullYear() }} 线上书城系统</p>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { ArrowDown, User, ShoppingBag, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const activeIndex = ref('/')

onMounted(() => {
  // 从localStorage加载用户状态
  userStore.initUserFromStorage()
})

const handleLogout = async () => {
  try {
    await userStore.logout()
    ElMessage.success('退出成功')
    router.push('/login')
  } catch (error) {
    console.error('退出失败:', error)
    ElMessage.error('退出失败')
  }
}
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo-link {
  text-decoration: none;
  color: inherit;
}

.logo h1 {
  margin: 0;
  color: #409EFF;
  font-size: 24px;
}

.nav-menu {
  flex-grow: 1;
  margin: 0 20px;
}

.user-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.user-dropdown-link {
  cursor: pointer;
  color: #409EFF;
  display: flex;
  align-items: center;
  font-size: 14px;
}

.el-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0;
  height: auto !important;
}

.el-main {
  padding: 20px;
  flex-grow: 1;
  background-color: #f5f7fa;
}

.el-footer {
  background-color: #f5f5f5;
  padding: 20px;
  text-align: center;
}

.footer-container {
  color: #666;
}
</style> 