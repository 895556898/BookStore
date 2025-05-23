<template>
  <div class="register-container">
    <el-card class="register-box">
      <h2 class="register-title">用户注册</h2>

      <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-width="80px"
      >
        <!-- 用户名 -->
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
          />
        </el-form-item>

        <!-- 密码 -->
        <el-form-item label="密码" prop="password">
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
          />
        </el-form-item>

        <!-- 重复密码 -->
        <el-form-item label="重复密码" prop="confirmPassword">
          <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              prefix-icon="Lock"
              show-password
          />
        </el-form-item>

        <!-- 手机号 -->
        <el-form-item label="手机号" prop="phone">
          <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              prefix-icon="Phone"
          />
        </el-form-item>

        <!-- 注册按钮 -->
        <el-button
            type="primary"
            class="register-btn"
            :loading="loading"
            @click="handleRegister"
        >
          立即注册
        </el-button>
      </el-form>

      <!-- 登录链接 -->
      <div class="login-link">
        <span>已有账号？</span>
        <el-button
            type="text"
            class="login-btn"
            @click="navigateTo('/login')"
        >
          立即登录
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
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const errorDialogVisible = ref(false)
const errorMessage = ref('')
const registerFormRef = ref() // 获取表单引用
const loading = ref(false)

// 表单数据
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  phone: ''
})

// 验证规则
const validateConfirmPassword = (value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.value.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的11位手机号'))
  } else {
    callback()
  }
}

const registerRules = ref({
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
  ],
  confirmPassword: [
    {
      required: true,
      trigger: 'blur',
      validator: validateConfirmPassword
    },
    {
      trigger: 'input',
      validator: validateConfirmPassword
    }
  ],
  phone: [
    {
      required: true,
      trigger: 'blur',
      validator: validatePhone
    },
    {
      trigger: 'input',
      validator: validatePhone
    }
  ]
})

const baseUrl = ref(process.env.BASE_URL || 'http://localhost:8080')

// 处理注册逻辑
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        
        // 准备注册数据
        const registerData = {
          username: registerForm.value.username,
          password: registerForm.value.password,
          phone: registerForm.value.phone
        }
        
        const response = await fetch(`${baseUrl.value}/api/user/register`, {
          method: 'POST',
          body: JSON.stringify(registerData),
          headers: {
            'Content-Type': 'application/json'
          },
          credentials: 'include',
          mode: 'cors'
        })
        
        const responseData = await response.json()
        console.log('收到响应数据:', responseData)
        
        // 处理注册结果
        if (responseData && responseData.code === 200) {
          ElMessage.success('注册成功，即将跳转至登录页面')
          
          setTimeout(() => {
            navigateTo('/login')
          }, 3000)
        } else {
          // 显示错误提示
          console.error('注册失败:', responseData.message)
          errorMessage.value = `注册失败: ${responseData?.message || '未知错误'}（错误码: ${responseData?.code || '无'}）`
          errorDialogVisible.value = true
        }
      } catch (error) {
        console.error('注册请求异常:', error)
        errorMessage.value = `注册异常: ${error.message || '网络异常，请稍后重试'}`
        errorDialogVisible.value = true
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;

  .register-box {
    width: 450px;
    padding: 30px 35px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.1);

    .register-title {
      text-align: center;
      margin-bottom: 30px;
      color: var(--el-color-primary);
    }

    .register-btn {
      width: 100%;
      margin-top: 10px;
    }

    .login-link {
      text-align: center;
      margin-top: 20px;
      color: #606266;

      .login-btn {
        padding: 0;
        margin-left: 5px;
      }
    }
  }
}
</style> 