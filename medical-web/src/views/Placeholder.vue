<template>
  <div class="placeholder-page">
    <el-empty description="功能开发中，敬请期待">
      <el-button class="back-btn" @click="goHome">返回工作台</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { resolveDefaultHomePath } from '@/config/menu-config'

const route = useRoute()
const router = useRouter()

function goHome() {
  const p = route.path
  if (p.startsWith('/reception')) {
    router.push('/reception/dashboard')
  } else if (p.startsWith('/nurse')) {
    router.push('/nurse/dashboard')
  } else if (p.startsWith('/doctor')) {
    router.push('/doctor/dashboard')
  } else if (p.startsWith('/patient')) {
    router.push('/patient/dashboard')
  } else {
    router.push(resolveDefaultHomePath())
  }
}
</script>

<style scoped>
.placeholder-page {
  padding: 60px;
  text-align: center;
}

.placeholder-page :deep(.el-empty__description p) {
  color: #3d2914;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
}

.back-btn {
  background: linear-gradient(135deg, #e8a54b 0%, #d48232 100%);
  border: none;
  color: #fff;
  box-shadow: 0 4px 16px rgba(212, 130, 50, 0.35);
}
.back-btn:hover {
  background: linear-gradient(135deg, #f0b55c 0%, #e08d3a 100%);
  box-shadow: 0 6px 20px rgba(212, 130, 50, 0.45);
  color: #fff;
}
</style>
