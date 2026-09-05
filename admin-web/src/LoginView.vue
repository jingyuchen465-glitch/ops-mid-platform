<script setup>
import { ref } from 'vue'
import { login } from './api'

const emit = defineEmits(['success'])
const username = ref('demo-admin')
const password = ref('admin123')
const loading = ref(false)
const error = ref('')

async function submit() {
  if (loading.value) {
    return
  }

  loading.value = true
  error.value = ''

  try {
    const loginUser = await login(username.value, password.value)
    emit('success', loginUser)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-panel">
      <div class="login-brand">
        <span>运</span>
        <div>
          <b>运营中台</b>
          <small>管理控制台</small>
        </div>
      </div>

      <div class="login-copy">
        <h1>欢迎回来</h1>
        <p>登录后管理企业 MCP 的访问、配置与审计。</p>
      </div>

      <a-alert
        v-if="error"
        :message="error"
        type="error"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-form layout="vertical" @submit.prevent="submit">
        <a-form-item label="用户名">
          <a-input
            v-model:value="username"
            size="large"
            autocomplete="username"
            @pressEnter="submit"
          />
        </a-form-item>

        <a-form-item label="密码">
          <a-input-password
            v-model:value="password"
            size="large"
            autocomplete="current-password"
            @pressEnter="submit"
          />
        </a-form-item>

        <a-button
          html-type="submit"
          type="primary"
          size="large"
          block
          :loading="loading"
          @click="submit"
        >
          登录管理控制台
        </a-button>
      </a-form>
    </section>
  </main>
</template>
