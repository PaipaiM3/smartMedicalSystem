<template>
  <div class="login-page">
    <!-- 背景图 -->
    <div class="login-bg"></div>
    <!-- 主内容区 -->
    <div class="login-content">
      <!-- 顶部 logo 及标题 -->
      <div class="login-header">
        <div>
          <div class="login-title-zh">智能医疗服务管理系统</div>
          <div class="login-title-sub">Smart Medical Service System</div>
        </div>
      </div>
      <!-- 登录主卡片 -->
      <el-card class="login-card" shadow="never">
        <div class="login-tabs">
          <div
            class="tab"
            :class="{ active: activeTab === 'login' }"
            @click="activeTab = 'login'"
          >
            账户密码登录
          </div>
          <div
            class="tab"
            :class="{ active: activeTab === 'register' }"
            @click="activeTab = 'register'"
          >
            新用户注册
          </div>
        </div>
        <el-form
          v-show="activeTab === 'login'"
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          autocomplete="off"
          @keydown.enter.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入账户名/工号"
              size="large"
              clearable
              autocomplete="off"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              autocomplete="current-password"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item style="margin-top: 26px">
            <el-button
              type="primary"
              class="login-btn"
              size="large"
              :loading="loading"
              @click="handleLogin"
            >
              登&nbsp;&nbsp;录
            </el-button>
          </el-form-item>
        </el-form>
        <el-form
          v-show="activeTab === 'register'"
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="login-form"
          autocomplete="off"
          @keydown.enter.prevent="handleRegister"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="用户名（3～32 个字符）"
              size="large"
              clearable
              maxlength="32"
              autocomplete="off"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="name">
            <el-input
              v-model="registerForm.name"
              placeholder="姓名（可选，默认与用户名相同）"
              size="large"
              clearable
              maxlength="50"
              autocomplete="off"
            >
              <template #prefix>
                <el-icon><UserFilled /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="mobilePhone">
            <el-input
              v-model="registerForm.mobilePhone"
              placeholder="手机号（可选）"
              size="large"
              clearable
              maxlength="11"
              autocomplete="off"
            >
              <template #prefix>
                <el-icon><Iphone /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码（至少 6 位）"
              size="large"
              show-password
              autocomplete="new-password"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              size="large"
              show-password
              autocomplete="new-password"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <p class="register-hint">注册成功后将获得「患者」角色，可使用患者端功能。</p>
          <el-form-item style="margin-top: 10px">
            <el-button
              type="primary"
              class="login-btn"
              size="large"
              :loading="registerLoading"
              @click="handleRegister"
            >
              注&nbsp;&nbsp;册
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
      <!-- 页脚 -->
      <div class="login-footer">Copyright © 智能医疗服务管理系统 2026</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, UserFilled, Lock, Iphone } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api/auth'
import { resolveDefaultHomePath } from '@/config/menu-config'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const registerFormRef = ref(null)
const loading = ref(false)
const registerLoading = ref(false)
const activeTab = ref('login')

const form = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  name: '',
  mobilePhone: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  username: [{ required: true, message: '请输入账户名/工号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 32, message: '用户名为 3～32 个字符', trigger: 'blur' }
  ],
  name: [{ max: 50, message: '姓名最多 50 个字符', trigger: 'blur' }],
  mobilePhone: [
    {
      validator: (_rule, value, callback) => {
        if (!value || value.trim() === '') {
          callback()
          return
        }
        if (!/^1\d{10}$/.test(value.trim())) {
          callback(new Error('请输入 11 位有效手机号'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '密码为 6～64 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

/** 登录成功后的跳转：优先 query.redirect（站内路径），否则按角色进默认首页 */
function pickRedirectAfterLogin() {
  const r = route.query.redirect
  const s =
    typeof r === 'string'
      ? r.trim()
      : Array.isArray(r) && typeof r[0] === 'string'
        ? r[0].trim()
        : ''
  if (s && s.startsWith('/') && !s.startsWith('//')) {
    return s
  }
  return resolveDefaultHomePath()
}

const handleLogin = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const data = await login(form)
    sessionStorage.setItem('userInfo', JSON.stringify(data))
    ElMessage.success('登录成功')
    router.replace(pickRedirectAfterLogin())
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  try {
    await registerFormRef.value?.validate()
  } catch {
    return
  }
  registerLoading.value = true
  try {
    const savedUsername = registerForm.username.trim()
    await register({
      username: savedUsername,
      name: registerForm.name.trim() || undefined,
      mobilePhone: registerForm.mobilePhone.trim() || undefined,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword
    })
    ElMessage.success('注册成功，请使用新账号登录')
    form.username = savedUsername
    form.password = ''
    activeTab.value = 'login'
    registerFormRef.value?.resetFields()
    registerForm.username = savedUsername
  } catch (e) {
    console.error(e)
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
/* 与背景图风格统一：温暖日落、传统与现代医疗融合 */
.login-page {
  min-height: 100vh;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
}

/* 全屏背景图 */
.login-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('/images/login%20background.jpg') center center / cover no-repeat;
  z-index: 0;
}

/* 主内容层，置于背景之上 */
.login-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding-top: 28px;
}

.login-header {
  display: flex;
  align-items: center;
  margin: 38px 0 18px 0;
  gap: 13px;
  user-select: none;
}

/* 标题：白色加粗描边，保证在深色背景上清晰可见 */
.login-title-zh {
  font-size: 26px;
  font-weight: bold;
  color: #fff;
  line-height: 32px;
  text-shadow:
    0 0 4px rgba(0, 0, 0, 0.9),
    0 0 8px rgba(0, 0, 0, 0.7),
    1px 1px 0 #000,
    -1px -1px 0 #000,
    1px -1px 0 #000,
    -1px 1px 0 #000,
    0 2px 4px rgba(0, 0, 0, 0.8);
}

.login-title-sub {
  font-size: 18px;
  color: #fff;
  font-family: "KaiTi", "楷体", cursive;
  margin-top: 2px;
  text-shadow:
    0 0 3px rgba(0, 0, 0, 0.9),
    0 0 6px rgba(0, 0, 0, 0.6),
    1px 1px 0 #000,
    -1px -1px 0 #000,
    0 1px 3px rgba(0, 0, 0, 0.8);
}

/* 毛玻璃透明卡片 */
.login-card {
  width: 420px;
  margin-top: 12px;
  border-radius: 16px;
  background: rgba(255, 252, 250, 0.35);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.login-card :deep(.el-card__body) {
  padding: 28px 32px;
}

.login-tabs {
  display: flex;
  margin: 6px 0 0 0;
  height: 40px;
  gap: 8px;
  border-bottom: 1px solid rgba(80, 50, 30, 0.2);
}

.tab {
  flex: 1;
  text-align: center;
  font-size: 15px;
  font-weight: bold;
  line-height: 40px;
  color: #2c1810;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.9);
  cursor: pointer;
  opacity: 0.55;
  margin-bottom: -1px;
  border-bottom: 2px solid transparent;
  transition: opacity 0.15s ease, border-color 0.15s ease;
}

.tab:hover {
  opacity: 0.85;
}

.tab.active {
  opacity: 1;
  border-bottom-color: #c4702a;
}

.register-hint {
  margin: 0;
  font-size: 12px;
  line-height: 1.5;
  color: #4a3520;
  opacity: 0.9;
}

.login-form {
  padding: 20px 0 0 0;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

/* 输入框：半透明毛玻璃，保证可读 */
.login-form :deep(.el-input__wrapper) {
  border-radius: 10px !important;
  background: rgba(255, 255, 255, 0.7) !important;
  backdrop-filter: blur(8px);
  border: 1.5px solid rgba(100, 70, 40, 0.5) !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.login-form :deep(.el-input__wrapper:hover),
.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(210, 130, 50, 0.7) !important;
  box-shadow: 0 0 0 2px rgba(210, 130, 50, 0.15);
}

.login-form :deep(.el-input__inner) {
  color: #1a0f08;
  font-weight: 500;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: #4a3520;
  font-weight: 500;
}

.login-form :deep(.el-icon) {
  color: #3d2914;
}

/* 落日暖橙登录按钮，白色文字加描边 */
.login-btn {
  width: 100%;
  font-size: 18px;
  font-weight: bold;
  background: linear-gradient(135deg, #e8a54b 0%, #d48232 100%);
  border: none;
  border-radius: 10px;
  letter-spacing: 3px;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  box-shadow: 0 4px 16px rgba(212, 130, 50, 0.4);
}

.login-btn:hover,
.login-btn:focus {
  background: linear-gradient(135deg, #f0b55c 0%, #e08d3a 100%);
  box-shadow: 0 6px 20px rgba(212, 130, 50, 0.5);
}

.login-footer {
  text-align: center;
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  margin: 35px 0 0 0;
  padding: 8px 16px;
  user-select: none;
  background: rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(8px);
  border-radius: 8px;
  text-shadow:
    0 0 2px rgba(0, 0, 0, 0.9),
    1px 1px 0 #000,
    -1px -1px 0 #000,
    0 1px 2px rgba(0, 0, 0, 0.8);
}
</style>
