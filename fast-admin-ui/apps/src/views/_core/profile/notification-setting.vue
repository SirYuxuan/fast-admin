<script setup lang="ts">
import { ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Button, List, ListItem, Switch, message } from 'ant-design-vue';

interface NotificationItem {
  key: string;
  icon: string;
  title: string;
  description: string;
  enabled: boolean;
}

const items = ref<NotificationItem[]>([
  {
    key: 'system',
    icon: 'lucide:bell-ring',
    title: '系统消息',
    description: '系统维护、版本更新等公告通知',
    enabled: true,
  },
  {
    key: 'login',
    icon: 'lucide:log-in',
    title: '登录提醒',
    description: '异地登录、新设备登录时邮件提醒',
    enabled: true,
  },
  {
    key: 'security',
    icon: 'lucide:shield-alert',
    title: '安全提醒',
    description: '密码修改、敏感操作的实时通知',
    enabled: true,
  },
  {
    key: 'todo',
    icon: 'lucide:check-square',
    title: '待办通知',
    description: '工作流任务、待审批事项提醒',
    enabled: false,
  },
  {
    key: 'mention',
    icon: 'lucide:at-sign',
    title: '@ 我的消息',
    description: '被同事在评论或文档中@时通知',
    enabled: false,
  },
]);

function onToggle(item: NotificationItem) {
  message.success(`「${item.title}」已${item.enabled ? '开启' : '关闭'}`);
}

function onSaveAll() {
  message.success('通知设置已保存');
}
</script>

<template>
  <List item-layout="horizontal" :data-source="items" :split="true">
    <template #renderItem="{ item }">
      <ListItem>
        <div class="flex w-full items-center justify-between gap-3">
          <div class="flex items-center gap-3 flex-1">
            <div class="notice-icon">
              <IconifyIcon :icon="item.icon" class="text-xl" />
            </div>
            <div>
              <div class="notice-title">{{ item.title }}</div>
              <div class="notice-desc">{{ item.description }}</div>
            </div>
          </div>
          <Switch v-model:checked="item.enabled" @change="onToggle(item)" />
        </div>
      </ListItem>
    </template>
  </List>

  <div class="mt-4">
    <Button type="primary" @click="onSaveAll">保存所有设置</Button>
  </div>
</template>

<style scoped>
.notice-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: rgba(82, 196, 26, 0.1);
  color: #52c41a;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notice-title {
  font-size: 14px;
  font-weight: 500;
}

.notice-desc {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 2px;
}
</style>
