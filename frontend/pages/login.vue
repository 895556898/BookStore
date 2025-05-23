<template>
  <div class="login-container">
    <el-card class="login-box">
      <h2 class="login-title">用户登录</h2>

      <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-width="80px"
      >

        <!-- 用户名 -->
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
          />
        </el-form-item>

        <!-- 密码 -->
        <el-form-item label="密码" prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
          />
        </el-form-item>

        <!-- 登录按钮 -->
        <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
        >
          立即登录
        </el-button>
      </el-form>

      <!-- 注册链接 -->
      <div class="register-link">
        <span>没有账号？</span>
        <el-button
            type="text"
            class="register-btn"
            @click="navigateTo('/register')"
        >
          立即注册
        </el-button>
      </div>
    </el-card>
  </div>

  <el-dialog
      v-model="errorDialogVisible"
      title="错误提示"
      width="30%"
  >
    <span>{{ errorMessage }}</span>
    <template #footer>
      <el-button @click="errorDialogVisible = false">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const errorDialogVisible = ref(false)
const errorMessage = ref('')
const loginFormRef = ref() // 获取表单引用
const loading = ref(false)

// 表单数据
const loginForm = ref({
  username: '',
  password: ''
})

// 修改验证规则增强
const loginRules = ref({
  username: [
    {
      required: true,
      trigger: 'blur',
      validator: (value, callback) => {
        if (!value) {
          callback(new Error('请输入用户名'))
        } else {
          callback()
        }
      }
    }
  ],
  password: [
    {
      required: true,
      trigger: 'blur',
      validator: (value, callback) => {
        if (!value) {
          callback(new Error('请输入密码'))
        } else {
          callback()
        }
      }
    }
  ]
})

const baseUrl = ref(process.env.BASE_URL || 'http://localhost:8080')

// 处理登录逻辑
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        
        const formData = new URLSearchParams()
        formData.append('username', loginForm.value.username)
        formData.append('password', loginForm.value.password)
        
        const response = await $fetch(`${baseUrl.value}/api/user/login`, {
          method: 'POST',
          body: formData,
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          credentials: 'include',
          mode: 'cors',
          retry: 0
        })
        
        // 处理登录成功
        if (response && response.code === 200) {
          // 引入并使用Pinia store
          const userStore = useUserStore()
          // 保存用户信息到store
          userStore.setUser(response.data)

          ElMessage.success('登录成功')
          
          navigateTo('/')
        } else {
          errorMessage.value = response?.message || '登录失败，请检查用户名和密码'
          errorDialogVisible.value = true
        }
      } catch (error) {
        console.error('登录错误:', error)
        errorMessage.value = '登录失败: ' + (error.message || '网络异常，请稍后重试')
        errorDialogVisible.value = true
      } finally {
        loading.value = false
      }
    }
  })
}

</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;

  .login-box {
    width: 450px;
    padding: 30px 35px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.1);

    .login-title {
      text-align: center;
      margin-bottom: 30px;
      color: var(--el-color-primary);
    }

    .login-btn {
      width: 100%;
      margin-top: 10px;
    }

    .register-link {
      text-align: center;
      margin-top: 20px;
      color: #606266;

      .register-btn {
        padding: 0;
        margin-left: 5px;
      }
    }
  }
}

.user-type-group {
  width: 100%;

  :deep(.el-radio-button) {
    width: 50%;

    .el-radio-button__inner {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 12px 20px;
    }
  }
}
</style>