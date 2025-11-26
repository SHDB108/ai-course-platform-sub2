<template>
  <div class="header">
    <div class="header-left">
      <h1 class="title">AI Course Platform</h1>
    </div>
    <div class="header-right">
      <el-dropdown>
        <div class="user-info">
          <el-avatar :size="32" :icon="UserFilled" />
          <span class="username">{{ userStore.username || 'Guest' }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>Profile</el-dropdown-item>
            <el-dropdown-item>Settings</el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">Logout</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background-color: $white;
  border-bottom: 1px solid $border-color;
  box-shadow: $shadow-sm;

  .header-left {
    .title {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: $text-primary;
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;
      cursor: pointer;

      .username {
        font-size: 14px;
        color: $text-primary;
      }
    }
  }
}
</style>
