<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { preferences } from '@vben/preferences';
import { useUserStore } from '@vben/stores';

import {
  Avatar,
  Card,
  Col,
  Descriptions,
  DescriptionsItem,
  Row,
  Tabs,
  TabPane,
  Tag,
} from 'ant-design-vue';

import { getUserProfile } from '#/api/system/user';

import ProfileBase from './base-setting.vue';
import ProfileNotification from './notification-setting.vue';
import ProfilePassword from './password-setting.vue';
import ProfileSecurity from './security-setting.vue';

const userStore = useUserStore();
const activeTab = ref<string>('basic');

const userDetail = ref<Record<string, any>>({});

async function loadUserInfo() {
  try {
    const data = await getUserProfile();
    userDetail.value = data ?? {};
  } catch {
    /* 异常由全局拦截器提示 */
  }
}

onMounted(() => {
  loadUserInfo();
});
</script>

<template>
  <Page>
    <Row :gutter="[16, 16]">
      <!-- 左侧：个人卡片 -->
      <Col :xs="24" :lg="8">
        <Card class="profile-card">
          <div class="profile-header">
            <Avatar
              :size="96"
              :src="userStore.userInfo?.avatar || preferences.app.defaultAvatar"
            />
            <div class="profile-name">
              {{ userDetail.nickname || userStore.userInfo?.realName || userDetail.username }}
            </div>
            <div class="profile-username">@{{ userDetail.username || userStore.userInfo?.username }}</div>
            <div class="profile-roles mt-3">
              <Tag
                v-for="role in userStore.userInfo?.roles ?? []"
                :key="String(role)"
                color="blue"
              >
                {{ role }}
              </Tag>
            </div>
          </div>

          <div class="profile-divider"></div>

          <Descriptions :column="1" size="small" :label-style="{ color: '#8c8c8c' }">
            <DescriptionsItem>
              <template #label>
                <span class="flex items-center gap-1">
                  <IconifyIcon icon="lucide:mail" /> 邮箱
                </span>
              </template>
              {{ userDetail.email || '未设置' }}
            </DescriptionsItem>
            <DescriptionsItem>
              <template #label>
                <span class="flex items-center gap-1">
                  <IconifyIcon icon="lucide:phone" /> 手机号
                </span>
              </template>
              {{ userDetail.phone || '未设置' }}
            </DescriptionsItem>
            <DescriptionsItem>
              <template #label>
                <span class="flex items-center gap-1">
                  <IconifyIcon icon="lucide:id-card" /> 账号 ID
                </span>
              </template>
              <span class="text-xs text-gray-500">{{ userDetail.id || '-' }}</span>
            </DescriptionsItem>
          </Descriptions>
        </Card>
      </Col>

      <!-- 右侧：设置 Tab -->
      <Col :xs="24" :lg="16">
        <Card>
          <Tabs v-model:active-key="activeTab" tab-position="top">
            <TabPane key="basic">
              <template #tab>
                <span class="flex items-center gap-1">
                  <IconifyIcon icon="lucide:user" /> 基本设置
                </span>
              </template>
              <ProfileBase :detail="userDetail" @updated="loadUserInfo" />
            </TabPane>

            <TabPane key="password">
              <template #tab>
                <span class="flex items-center gap-1">
                  <IconifyIcon icon="lucide:lock-keyhole" /> 修改密码
                </span>
              </template>
              <ProfilePassword />
            </TabPane>

            <TabPane key="security">
              <template #tab>
                <span class="flex items-center gap-1">
                  <IconifyIcon icon="lucide:shield-check" /> 安全设置
                </span>
              </template>
              <ProfileSecurity />
            </TabPane>

            <TabPane key="notice">
              <template #tab>
                <span class="flex items-center gap-1">
                  <IconifyIcon icon="lucide:bell" /> 消息提醒
                </span>
              </template>
              <ProfileNotification />
            </TabPane>
          </Tabs>
        </Card>
      </Col>
    </Row>
  </Page>
</template>

<style scoped>
.profile-card :deep(.ant-card-body) {
  padding: 24px;
}

.profile-header {
  text-align: center;
  padding-bottom: 16px;
}

.profile-name {
  margin-top: 12px;
  font-size: 18px;
  font-weight: 600;
}

.profile-username {
  margin-top: 4px;
  font-size: 13px;
  color: #8c8c8c;
}

.profile-roles {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 4px;
}

.profile-divider {
  height: 1px;
  background: var(--vben-border, #f0f0f0);
  margin: 16px 0;
}

:deep(.ant-descriptions-item-label),
:deep(.ant-descriptions-item-content) {
  padding: 8px 0 !important;
}
</style>
