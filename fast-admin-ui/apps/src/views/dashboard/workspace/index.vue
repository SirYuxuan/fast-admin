<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Card, Col, Row, Space } from 'ant-design-vue';
import {
  ArrowDown,
  ArrowUp,
} from '@vben/icons';
import { preferences } from '@vben/preferences';
import { useUserStore } from '@vben/stores';
import { openWindow } from '@vben/utils';

import AnalyticsVisitsSource from '../analytics/analytics-visits-source.vue';

const userStore = useUserStore();
const router = useRouter();

// 获取当前时间的问候语
const greetingMessage = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) return '夜深了，注意休息';
  if (hour < 9) return '早上好';
  if (hour < 12) return '上午好';
  if (hour < 14) return '中午好';
  if (hour < 17) return '下午好';
  if (hour < 19) return '傍晚好';
  return '晚上好';
});

// 系统统计数据
const statistics = ref([
  {
    title: '系统用户',
    value: 245,
    icon: '👤',
    color: '#1890ff',
    trend: 'up',
    trendValue: 8,
    link: '/system/user',
  },
  {
    title: '角色权限',
    value: 12,
    icon: '🔐',
    color: '#52c41a',
    trend: 'up',
    trendValue: 2,
    link: '/system/role',
  },
  {
    title: '菜单配置',
    value: 38,
    icon: '📋',
    color: '#faad14',
    trend: 'down',
    trendValue: 1,
    link: '/system/menu',
  },
  {
    title: '用户部门',
    value: 8,
    icon: '👥',
    color: '#722ed1',
    trend: 'up',
    trendValue: 1,
    link: '/system/dept',
  },
]);

// 快捷操作
const quickActions = ref([
  {
    title: '用户管理',
    description: '管理系统用户',
    icon: 'ion:people-outline',
    color: '#1890ff',
    link: '/system/user',
  },
  {
    title: '角色管理',
    description: '配置用户角色',
    icon: 'ion:shield-outline',
    color: '#52c41a',
    link: '/system/role',
  },
  {
    title: '菜单管理',
    description: '配置系统菜单',
    icon: 'ion:list-outline',
    color: '#faad14',
    link: '/system/menu',
  },
  {
    title: '部门管理',
    description: '管理部门信息',
    icon: 'ion:sitemap-outline',
    color: '#722ed1',
    link: '/system/dept',
  },
  {
    title: '文件管理',
    description: '上传下载文件',
    icon: 'ion:document-outline',
    color: '#13c2c2',
    link: '/system/file',
  },
  {
    title: '文件配置',
    description: '配置存储方案',
    icon: 'ion:settings-outline',
    color: '#eb2f96',
    link: '/system/file-config',
  },
]);

// 导航到指定页面
function navigateTo(path: string) {
  if (path?.startsWith('http')) {
    openWindow(path);
    return;
  }
  router.push(path).catch((error) => {
    console.error('Navigation failed:', error);
  });
}
</script>

<template>
  <div class="workspace-container p-6">
    <!-- 头部欢迎区域 -->
    <div class="mb-6">
      <div class="flex items-center justify-between rounded-lg bg-gradient-to-r from-blue-500 to-cyan-500 p-6 text-white shadow-lg">
        <div>
          <h1 class="text-3xl font-bold">{{ greetingMessage }}，{{ userStore.userInfo?.realName }}！</h1>
          <p class="mt-2 text-lg opacity-90">
            欢迎回到 {{ preferences.app.name }}，祝你有个美好的一天
          </p>
        </div>
        <div class="text-6xl opacity-20">📊</div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <Row :gutter="[16, 16]" class="mb-6">
      <Col :xs="24" :sm="12" :lg="6" v-for="stat in statistics" :key="stat.title">
        <Card
          class="cursor-pointer transition-all hover:shadow-lg"
          @click="navigateTo(stat.link)"
        >
          <div class="flex items-center justify-between">
            <div>
              <div class="text-sm text-gray-500">{{ stat.title }}</div>
              <div class="mt-2 text-2xl font-bold">{{ stat.value }}</div>
              <div class="mt-2 flex items-center text-sm">
                <span v-if="stat.trend === 'up'" class="flex items-center text-green-500">
                  <ArrowUp class="mr-1" />
                  {{ stat.trendValue }}%
                </span>
                <span v-else class="flex items-center text-red-500">
                  <ArrowDown class="mr-1" />
                  {{ stat.trendValue }}%
                </span>
              </div>
            </div>
            <div
              class="flex h-16 w-16 items-center justify-center rounded-full text-3xl"
              :style="{ backgroundColor: stat.color + '20' }"
            >
              {{ stat.icon }}
            </div>
          </div>
        </Card>
      </Col>
    </Row>

    <!-- 快捷操作 -->
    <div class="mb-6">
      <h2 class="mb-4 text-xl font-bold">快捷操作</h2>
      <Row :gutter="[16, 16]">
        <Col :xs="24" :sm="12" :lg="8" v-for="action in quickActions" :key="action.title">
          <Card
            class="h-full cursor-pointer transition-all hover:shadow-lg"
            @click="navigateTo(action.link)"
          >
            <div class="flex items-start justify-between">
              <div>
                <h3 class="font-semibold">{{ action.title }}</h3>
                <p class="mt-2 text-sm text-gray-500">{{ action.description }}</p>
              </div>
              <div
                class="flex h-12 w-12 items-center justify-center rounded-lg text-xl"
                :style="{ backgroundColor: action.color + '20', color: action.color }"
              >
                <i :class="`${action.icon}`"></i>
              </div>
            </div>
          </Card>
        </Col>
      </Row>
    </div>

    <!-- 数据展示 -->
    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="12">
        <Card title="访问来源分析" class="h-full">
          <AnalyticsVisitsSource />
        </Card>
      </Col>
      <Col :xs="24" :lg="12">
        <Card title="系统信息" class="h-full">
          <Space direction="vertical" style="width: 100%">
            <div class="flex justify-between border-b pb-4">
              <span class="text-gray-600">Node 版本</span>
              <span class="font-semibold">v18.0.0</span>
            </div>
            <div class="flex justify-between border-b pb-4">
              <span class="text-gray-600">Vue 版本</span>
              <span class="font-semibold">v3.3.4</span>
            </div>
            <div class="flex justify-between border-b pb-4">
              <span class="text-gray-600">Vite 版本</span>
              <span class="font-semibold">v4.0.0</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">构建时间</span>
              <span class="font-semibold">{{ new Date().toLocaleDateString() }}</span>
            </div>
          </Space>
        </Card>
      </Col>
    </Row>
  </div>
</template>

<style scoped>
.workspace-container {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

:deep(.ant-card) {
  border-radius: 8px;
  border: none;
}

:deep(.ant-card-head) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom: none;
}

:deep(.ant-card-head-title) {
  color: white !important;
  font-weight: 600;
}
</style>
