<script setup lang="ts">
import type { UploadProps } from 'ant-design-vue';

import { computed, onMounted, ref } from 'vue';

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
  message,
  Row,
  Spin,
  Tabs,
  TabPane,
  Tag,
  Upload as AUpload,
} from 'ant-design-vue';

import { uploadFile } from '#/api/system/file';
import { changeUserAvatar, getUserProfile } from '#/api/system/user';
import { useAuthStore } from '#/store';

import AvatarCropper from './avatar-cropper.vue';
import ProfileBase from './base-setting.vue';
import ProfileNotification from './notification-setting.vue';
import ProfilePassword from './password-setting.vue';
import ProfileSecurity from './security-setting.vue';

const userStore = useUserStore();
const authStore = useAuthStore();
const activeTab = ref<string>('basic');

const userDetail = ref<Record<string, any>>({});
const avatarUploading = ref(false);
const cropperOpen = ref(false);
const cropperSrc = ref<File | null>(null);

// 头像优先级：用户最新上传 → store 中的 → 默认
const avatarUrl = computed(() => {
  return (
    userDetail.value.avatar ||
    userStore.userInfo?.avatar ||
    preferences.app.defaultAvatar
  );
});

async function loadUserInfo() {
  try {
    const data = await getUserProfile();
    userDetail.value = data ?? {};
  } catch {
    /* 异常由全局拦截器提示 */
  }
}

// 选完文件 → 打开裁剪 Modal
const beforeAvatarUpload: UploadProps['beforeUpload'] = (file) => {
  const f = file as File;

  // 大小校验：5MB 上限（裁剪前的原图）
  const maxSize = 5 * 1024 * 1024;
  if (f.size > maxSize) {
    message.error('图片不能超过 5MB');
    return false;
  }
  // 类型校验
  const allowed = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
  if (!allowed.includes(f.type)) {
    message.error('仅支持 JPG / PNG / WEBP / GIF 图片');
    return false;
  }

  cropperSrc.value = f;
  cropperOpen.value = true;
  // 阻止 antdv Upload 自身的请求
  return false;
};

// 裁剪完成 → 真正上传
async function onCropConfirm(blob: Blob) {
  try {
    avatarUploading.value = true;
    // 将 Blob 包成 File 以便复用 uploadFile 接口
    const ext = blob.type.split('/')[1] || 'png';
    const fileName = `avatar-${Date.now()}.${ext}`;
    const file = new File([blob], fileName, { type: blob.type });

    const result: any = await uploadFile(file, 'user_avatar');
    if (!result?.url) {
      message.error('上传失败，未返回头像地址');
      return;
    }
    await changeUserAvatar(result.url);
    userDetail.value.avatar = result.url;
    await authStore.fetchUserInfo();
    message.success('头像更新成功');
    cropperOpen.value = false;
    cropperSrc.value = null;
  } catch {
    /* 错误由全局拦截器提示 */
  } finally {
    avatarUploading.value = false;
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
            <!-- 头像 + 上传 -->
            <AUpload
              :before-upload="beforeAvatarUpload"
              :show-upload-list="false"
              accept="image/jpeg,image/png,image/webp,image/gif"
              :disabled="avatarUploading"
            >
              <div class="avatar-wrapper">
                <Spin :spinning="avatarUploading">
                  <Avatar :size="96" :src="avatarUrl" />
                </Spin>
                <div class="avatar-mask">
                  <IconifyIcon icon="lucide:camera" class="text-lg" />
                  <span class="text-xs mt-1">更换头像</span>
                </div>
              </div>
            </AUpload>

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

    <!-- 头像裁剪 Modal -->
    <AvatarCropper
      v-model:open="cropperOpen"
      :src="cropperSrc"
      :uploading="avatarUploading"
      @confirm="onCropConfirm"
    />
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

.avatar-wrapper {
  position: relative;
  display: inline-block;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  cursor: pointer;
  overflow: hidden;
}

.avatar-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  border-radius: 50%;
  pointer-events: none;
}

.avatar-wrapper:hover .avatar-mask {
  opacity: 1;
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
