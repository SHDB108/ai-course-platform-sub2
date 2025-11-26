<template>
  <el-menu
    :default-active="activeMenu"
    class="sidebar-menu"
    :collapse="false"
    router
  >
    <div class="logo">
      <h2>Learning Cockpit</h2>
    </div>

    <el-menu-item index="/dashboard">
      <el-icon><DataLine /></el-icon>
      <span>Dashboard</span>
    </el-menu-item>

    <el-menu-item index="/courses">
      <el-icon><Reading /></el-icon>
      <span>My Courses</span>
    </el-menu-item>

    <el-menu-item index="/recommendations">
      <el-icon><MagicStick /></el-icon>
      <span>AI Recommendations</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { DataLine, Reading, MagicStick } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.sidebar-menu {
  height: 100%;
  border-right: none;
  background-color: $navy-blue;
  color: $white;

  :deep(.el-menu-item) {
    color: rgba(255, 255, 255, 0.65);

    &:hover {
      color: $white;
      background-color: rgba(255, 255, 255, 0.08);
    }

    &.is-active {
      color: $white;
      background-color: $primary-color;
    }
  }

  .logo {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $white;
    font-size: 18px;
    font-weight: bold;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    h2 {
      margin: 0;
      font-size: 18px;
    }
  }
}
</style>
