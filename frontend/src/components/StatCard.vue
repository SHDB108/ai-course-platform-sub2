<template>
  <el-card class="stat-card" :body-style="{ padding: '20px' }">
    <div class="stat-content">
      <div class="stat-icon" :style="{ backgroundColor: bgColor }">
        <el-icon :size="28" :color="color">
          <component :is="icon" />
        </el-icon>
      </div>
      <div class="stat-info">
        <div class="stat-value">{{ formattedValue }}</div>
        <div class="stat-title">{{ title }}</div>
      </div>
    </div>
    <div v-if="subtitle" class="stat-subtitle">
      {{ subtitle }}
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  title: string
  value: number | string
  icon: any
  color?: string
  bgColor?: string
  subtitle?: string
}

const props = withDefaults(defineProps<Props>(), {
  color: '#1890ff',
  bgColor: '#e6f7ff'
})

const formattedValue = computed(() => {
  if (typeof props.value === 'number') {
    return props.value.toLocaleString()
  }
  return props.value
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.stat-card {
  height: 100%;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-md;
  }

  .stat-content {
    display: flex;
    align-items: center;
    gap: 16px;

    .stat-icon {
      width: 56px;
      height: 56px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: $border-radius-lg;
      flex-shrink: 0;
    }

    .stat-info {
      flex: 1;
      min-width: 0;

      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: $text-primary;
        line-height: 1.2;
      }

      .stat-title {
        font-size: 14px;
        color: $text-secondary;
        margin-top: 4px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }

  .stat-subtitle {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid $border-color;
    font-size: 12px;
    color: $text-disabled;
  }
}
</style>
