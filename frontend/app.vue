<template>
  <div>
    <NuxtRouteAnnouncer />
<!--    <NuxtWelcome />-->
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from './stores/user'

// 应用初始化时检查用户会话状态
onMounted(async () => {
  const userStore = useUserStore()
  
  // 从本地存储中恢复用户状态
  userStore.initUserFromStorage()
  
  // 如果有用户状态，验证会话是否有效
  if (userStore.getIsLoggedIn) {
    try {
      await userStore.checkSession()
    } catch (error) {
      console.error('会话检查失败:', error)
    }
  }
})
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}
</style>
