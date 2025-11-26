<template>
  <div class="app-layout">
    <aside class="sidebar">
      <Sidebar />
    </aside>
    <div class="main-container">
      <header class="header">
        <Header />
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.app-layout {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;

  .sidebar {
    width: 200px;
    flex-shrink: 0;
    background-color: $navy-blue;
  }

  .main-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .header {
      flex-shrink: 0;
    }

    .content {
      flex: 1;
      overflow-y: auto;
      padding: $spacing-lg;
      background-color: $background-color;
    }
  }
}
</style>
