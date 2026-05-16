<script setup lang="ts">
import { ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Button, List, ListItem, Tag, message } from 'ant-design-vue';

interface SecurityItem {
  key: string;
  icon: string;
  title: string;
  description: string;
  enabled: boolean;
  action: string;
}

const items = ref<SecurityItem[]>([
  {
    key: 'password',
    icon: 'lucide:key-round',
    title: '登录密码',
    description: '当前密码强度：中等，建议定期更换密码',
    enabled: true,
    action: '修改',
  },
  {
    key: 'phone',
    icon: 'lucide:smartphone',
    title: '密保手机',
    description: '未绑定手机号，建议绑定以提升账户安全',
    enabled: false,
    action: '绑定',
  },
  {
    key: 'email',
    icon: 'lucide:mail-check',
    title: '备用邮箱',
    description: '未绑定邮箱，可用于密码找回',
    enabled: false,
    action: '绑定',
  },
  {
    key: 'mfa',
    icon: 'lucide:shield-check',
    title: '两步验证',
    description: '开启两步验证可有效提升账户安全',
    enabled: false,
    action: '开启',
  },
  {
    key: 'devices',
    icon: 'lucide:monitor-smartphone',
    title: '登录设备管理',
    description: '查看已登录的设备并管理',
    enabled: true,
    action: '查看',
  },
]);

function onAction(item: SecurityItem) {
  message.info(`「${item.title}」功能开发中...`);
}
</script>

<template>
  <List item-layout="horizontal" :data-source="items" :split="true">
    <template #renderItem="{ item }">
      <ListItem>
        <div class="flex w-full items-center justify-between gap-3">
          <div class="flex items-center gap-3 flex-1">
            <div class="security-icon">
              <IconifyIcon :icon="item.icon" class="text-xl" />
            </div>
            <div>
              <div class="security-title">
                {{ item.title }}
                <Tag v-if="item.enabled" color="green" class="ml-2">已开启</Tag>
                <Tag v-else color="default" class="ml-2">未设置</Tag>
              </div>
              <div class="security-desc">{{ item.description }}</div>
            </div>
          </div>
          <Button type="link" @click="onAction(item)">
            {{ item.action }}
          </Button>
        </div>
      </ListItem>
    </template>
  </List>
</template>

<style scoped>
.security-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.security-title {
  font-size: 14px;
  font-weight: 500;
}

.security-desc {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 2px;
}
</style>
