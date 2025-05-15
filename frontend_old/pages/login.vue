<template>
  <div class="login-container">
    <el-card class="login-box">
      <h2 class="login-title">书店管理系统登录</h2>

      <el-form
          :model="loginForm"
          :rules="loginRules"
          ref="loginFormRef"
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
          />
        </el-form-item>

        <!-- 登录按钮 -->
        <el-button
            type="primary"
            @click="handleLogin"
            class="login-btn"
            :loading="loading"
        >
          立即登录
        </el-button>
      </el-form>

      <!-- 注册链接 -->
      <div class="register-link">
        <span>没有账号？</span>
        <el-button
            type="text"
            @click="navigateTo('/register')"
            class="register-btn"
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
import { ref } from 'vue'

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
      validator: (rule, value, callback) => {
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
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请输入密码'))
        } else {
          callback()
        }
      }
    }
  ]
})

// 登录处理
const handleLogin = async () => {
  alert(loginForm.value.username)
  // try {
  //   // 1. 表单验证
  //   const valid = await loginFormRef.value.validate()
  //   if (!valid) return
  //
  //   loading.value = true
  //
  //   // 2. 发送登录请求
  //   const { data: res } = await useFetch('/api/login', {
  //     method: 'POST',
  //     body: loginForm.value
  //   })
  //
  //   // 3. 处理响应
  //   if (res.value.success) {
  //     ElMessage.success('登录成功')
  //     // 跳转到主页并传递用户信息
  //     navigateTo({
  //       path: '/',
  //       query: {
  //         username: loginForm.value.username,
  //         userType: loginForm.value.userType
  //       }
  //     })
  //   } else {
  //     showError(res.value.message || '用户名或密码错误')
  //   }
  // } catch (err) {
  //   // 处理网络错误
  //   showError(err.response?.data?.message || '请求失败，请检查网络')
  // } finally {
  //   loading.value = false
  // }
}


// 新增错误处理函数
const showError = (msg) => {
  errorMessage.value = msg
  errorDialogVisible.value = true
}

// 路由跳转
const navigateTo = useRouter().push
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;  // 浅灰色背景

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

// 调整级联选择器样式
:deep(.el-cascader) {
  width: 100%;

  .el-input__inner {
    height: 40px;
  }

  .el-icon-arrow-down {
    display: none;  // 隐藏下拉箭头
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