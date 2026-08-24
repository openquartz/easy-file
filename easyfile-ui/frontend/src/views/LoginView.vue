<template>
  <div class="login-page">
    <div class="login-card">
      <div class="traffic-lights" aria-hidden="true">
        <span class="dot red" />
        <span class="dot yellow" />
        <span class="dot green" />
      </div>
      <h1>EasyFile Admin</h1>
      <p class="subtitle">登录管理控制台</p>
      <el-form :model="form" @submit.prevent="onSubmit">
        <el-form-item>
          <el-input
            v-model="form.username"
            size="large"
            placeholder="用户名"
            autocomplete="username"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="form.password"
            size="large"
            type="password"
            show-password
            placeholder="密码"
            autocomplete="current-password"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.rememberMe">记住密码</el-checkbox>
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          class="submit"
          :loading="loading"
          native-type="submit"
        >
          登 入
        </el-button>
      </el-form>
      <p class="footer">@svnlab @svnee</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'

const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  rememberMe: false
})

async function onSubmit() {
  if (!form.username.trim() || !form.password.trim()) {
    ElMessage.error('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await login(form.username.trim(), form.password, form.rememberMe)
    window.location.href = '/easyfile-ui/'
  } catch (error) {
    ElMessage.error(`登录失败!失败原因:${error instanceof Error ? error.message : String(error)}`)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at 20% 20%, rgba(91, 163, 232, 0.18), transparent 36%),
    linear-gradient(180deg, #f7fbff 0%, #eef3f9 100%);
}

.login-card {
  width: 400px;
  padding: 28px 32px 24px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid var(--ef-border);
  border-radius: 16px;
  box-shadow: var(--ef-shadow);
  backdrop-filter: blur(18px);
}

.traffic-lights {
  display: flex;
  gap: 7px;
  margin-bottom: 22px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.dot.red {
  background: #ff5f57;
}

.dot.yellow {
  background: #febc2e;
}

.dot.green {
  background: #28c840;
}

h1 {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 600;
  letter-spacing: -0.03em;
}

.subtitle {
  margin: 0 0 24px;
  color: var(--ef-text-secondary);
  font-size: 14px;
}

.submit {
  width: 100%;
}

.footer {
  margin: 22px 0 0;
  text-align: center;
  color: var(--ef-text-secondary);
  font-size: 12px;
}
</style>
