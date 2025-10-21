<template>
  <teleport to="body">
    <div class="global-message-container">
      <transition-group name="message" tag="div">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          :class="[
            'message-item',
            `message-${notification.type}`
          ]"
        >
          <div class="message-icon">
            <component :is="getIcon(notification.type)" />
          </div>
          <div class="message-content">
            <div class="message-title">{{ notification.title }}</div>
            <div class="message-text">{{ notification.message }}</div>
          </div>
          <div class="message-actions">
            <button
              v-for="action in notification.actions"
              :key="action.label"
              :class="['action-btn', `action-${action.type || 'default'}`]"
              @click="action.action"
            >
              {{ action.label }}
            </button>
          </div>
          <button
            class="message-close"
            @click="removeNotification(notification.id)"
          >
            <Close />
          </button>
        </div>
      </transition-group>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'
import {
  SuccessFilled,
  WarningFilled,
  CircleCloseFilled,
  InfoFilled,
  Close
} from '@element-plus/icons-vue'

const appStore = useAppStore()

// 获取通知列表
const notifications = computed(() => appStore.notifications)

// 移除通知
const removeNotification = (id: string) => {
  appStore.removeNotification(id)
}

// 获取图标组件
const getIcon = (type: string) => {
  const iconMap = {
    success: SuccessFilled,
    warning: WarningFilled,
    error: CircleCloseFilled,
    info: InfoFilled
  }
  return iconMap[type as keyof typeof iconMap] || InfoFilled
}
</script>

<style scoped lang="scss">
.global-message-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9998;
  max-width: 400px;
  pointer-events: none;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  margin-bottom: 12px;
  background: var(--bg-color);
  border-radius: 8px;
  box-shadow: var(--box-shadow-base);
  border-left: 4px solid;
  pointer-events: auto;
  position: relative;
  min-width: 300px;
}

.message-success {
  border-left-color: var(--color-success);
  
  .message-icon {
    color: var(--color-success);
  }
}

.message-warning {
  border-left-color: var(--color-warning);
  
  .message-icon {
    color: var(--color-warning);
  }
}

.message-error {
  border-left-color: var(--color-danger);
  
  .message-icon {
    color: var(--color-danger);
  }
}

.message-info {
  border-left-color: var(--color-info);
  
  .message-icon {
    color: var(--color-info);
  }
}

.message-icon {
  flex-shrink: 0;
  font-size: 20px;
  margin-top: 2px;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-title {
  font-weight: 600;
  font-size: var(--font-size-base);
  color: var(--text-color-primary);
  margin-bottom: 4px;
}

.message-text {
  font-size: var(--font-size-small);
  color: var(--text-color-regular);
  line-height: 1.4;
  word-break: break-word;
}

.message-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.action-btn {
  padding: 4px 12px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background: var(--bg-color);
  color: var(--text-color-primary);
  font-size: var(--font-size-small);
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    background: var(--border-color-extra-light);
  }
  
  &.action-primary {
    background: var(--color-primary);
    border-color: var(--color-primary);
    color: #fff;
    
    &:hover {
      opacity: 0.8;
    }
  }
}

.message-close {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 20px;
  height: 20px;
  border: none;
  background: none;
  color: var(--text-color-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s ease;
  
  &:hover {
    background: var(--border-color-extra-light);
    color: var(--text-color-primary);
  }
}

// 过渡动画
.message-enter-active,
.message-leave-active {
  transition: all 0.3s ease;
}

.message-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.message-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

.message-move {
  transition: transform 0.3s ease;
}
</style>