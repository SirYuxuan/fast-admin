<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { preferences } from '@vben/preferences';
import { useUserStore } from '@vben/stores';

import {
  Avatar,
  Badge,
  Button,
  Card,
  Col,
  Divider,
  List,
  ListItem,
  ListItemMeta,
  Progress,
  Row,
  Tag,
} from 'ant-design-vue';

const userStore = useUserStore();
const router = useRouter();

// 显示名：优先昵称，其次 realName，最后 username
const displayName = computed(() => {
  const info = userStore.userInfo as any;
  return info?.nickname || info?.realName || info?.username || '';
});

// 当前时间
const currentTime = ref('');
const greetingMessage = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) return '凌晨好';
  if (hour < 9) return '早上好';
  if (hour < 12) return '上午好';
  if (hour < 14) return '中午好';
  if (hour < 17) return '下午好';
  if (hour < 19) return '傍晚好';
  return '晚上好';
});

function updateTime() {
  const now = new Date();
  const y = now.getFullYear();
  const mo = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  const h = String(now.getHours()).padStart(2, '0');
  const m = String(now.getMinutes()).padStart(2, '0');
  const s = String(now.getSeconds()).padStart(2, '0');
  const weeks = ['日', '一', '二', '三', '四', '五', '六'];
  currentTime.value = `${y}-${mo}-${d} 星期${weeks[now.getDay()]} ${h}:${m}:${s}`;
}

onMounted(() => {
  updateTime();
  setInterval(updateTime, 1000);
});

// 系统统计指标
const stats = [
  {
    title: '系统用户',
    value: 245,
    unit: '人',
    icon: 'lucide:users',
    iconBg: 'rgba(24, 144, 255, 0.1)',
    iconColor: '#1890ff',
    trend: 8.2,
    trendUp: true,
    link: '/system/user',
  },
  {
    title: '角色数量',
    value: 12,
    unit: '个',
    icon: 'lucide:shield-check',
    iconBg: 'rgba(82, 196, 26, 0.1)',
    iconColor: '#52c41a',
    trend: 2.5,
    trendUp: true,
    link: '/system/role',
  },
  {
    title: '菜单总数',
    value: 38,
    unit: '项',
    icon: 'lucide:list-tree',
    iconBg: 'rgba(250, 173, 20, 0.1)',
    iconColor: '#faad14',
    trend: 1.1,
    trendUp: false,
    link: '/system/menu',
  },
  {
    title: '文件总数',
    value: 1284,
    unit: '个',
    icon: 'lucide:folder-archive',
    iconBg: 'rgba(114, 46, 209, 0.1)',
    iconColor: '#722ed1',
    trend: 12.6,
    trendUp: true,
    link: '/system/file',
  },
];

// 快捷入口
const shortcuts = [
  { title: '用户管理', icon: 'lucide:user-cog', color: '#1890ff', link: '/system/user' },
  { title: '角色管理', icon: 'lucide:shield', color: '#52c41a', link: '/system/role' },
  { title: '菜单管理', icon: 'lucide:menu', color: '#faad14', link: '/system/menu' },
  { title: '部门管理', icon: 'lucide:building-2', color: '#722ed1', link: '/system/dept' },
  { title: '文件列表', icon: 'lucide:files', color: '#13c2c2', link: '/system/file' },
  { title: '存储配置', icon: 'lucide:database', color: '#eb2f96', link: '/system/file-config' },
];

// 最近操作日志
const recentLogs = ref([
  { user: 'admin', action: '登录系统', module: '认证', time: '刚刚', status: 'success' },
  { user: 'admin', action: '修改用户信息', module: '用户管理', time: '2 分钟前', status: 'success' },
  { user: 'zhangsan', action: '上传文件 report.pdf', module: '文件管理', time: '5 分钟前', status: 'success' },
  { user: 'admin', action: '新增角色「运维」', module: '角色管理', time: '15 分钟前', status: 'success' },
  { user: 'lisi', action: '尝试访问受限页面', module: '权限', time: '32 分钟前', status: 'warning' },
  { user: 'admin', action: '激活 OSS 存储配置', module: '文件配置', time: '1 小时前', status: 'success' },
]);

// 系统通知
const notifications = ref([
  { title: '系统将在今晚 22:00 进行例行维护', type: '公告', color: 'blue', time: '今天' },
  { title: '新版本 v1.2.0 已发布，建议尽快升级', type: '更新', color: 'green', time: '昨天' },
  { title: '检测到异常登录，请检查账户安全', type: '警告', color: 'orange', time: '3 天前' },
]);

// 系统资源占用（模拟数据）
const resources = ref([
  { name: 'CPU 使用率', percent: 35, status: 'normal' },
  { name: '内存占用', percent: 62, status: 'active' },
  { name: '磁盘空间', percent: 78, status: 'active' },
  { name: '网络流量', percent: 24, status: 'normal' },
]);

function navTo(path: string) {
  router.push(path);
}
</script>

<template>
  <Page>
    <!-- 顶部欢迎条 -->
    <div class="welcome-bar mb-4">
      <div class="flex items-center gap-4">
        <Avatar
          :size="56"
          :src="userStore.userInfo?.avatar || preferences.app.defaultAvatar"
        />
        <div>
          <div class="text-xl font-medium">
            {{ greetingMessage }}，{{ displayName || 'Admin' }}
            <span class="text-sm text-gray-500 ml-2">今天又是元气满满的一天 🎯</span>
          </div>
          <div class="text-sm text-gray-500 mt-1">{{ currentTime }}</div>
        </div>
      </div>
      <div class="welcome-meta">
        <div class="meta-item">
          <div class="meta-label">在线用户</div>
          <div class="meta-value">12</div>
        </div>
        <Divider type="vertical" class="meta-divider" />
        <div class="meta-item">
          <div class="meta-label">待办</div>
          <div class="meta-value">3</div>
        </div>
        <Divider type="vertical" class="meta-divider" />
        <div class="meta-item">
          <div class="meta-label">消息</div>
          <div class="meta-value">
            <Badge :count="5" />
          </div>
        </div>
      </div>
    </div>

    <!-- 统计指标 -->
    <Row :gutter="[16, 16]" class="mb-4">
      <Col v-for="item in stats" :key="item.title" :xs="24" :sm="12" :lg="6">
        <Card class="stat-card" :body-style="{ padding: '20px' }" hoverable @click="navTo(item.link)">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="text-sm text-gray-500">{{ item.title }}</div>
              <div class="mt-2 flex items-baseline gap-1">
                <span class="text-2xl font-semibold">{{ item.value.toLocaleString() }}</span>
                <span class="text-xs text-gray-400">{{ item.unit }}</span>
              </div>
              <div class="mt-2 flex items-center text-xs">
                <span class="text-gray-400">较上周</span>
                <span
                  class="ml-2 flex items-center"
                  :class="item.trendUp ? 'text-green-600' : 'text-red-500'"
                >
                  <IconifyIcon
                    :icon="item.trendUp ? 'lucide:trending-up' : 'lucide:trending-down'"
                    class="mr-1"
                  />
                  {{ item.trend }}%
                </span>
              </div>
            </div>
            <div
              class="stat-icon"
              :style="{ background: item.iconBg, color: item.iconColor }"
            >
              <IconifyIcon :icon="item.icon" class="text-xl" />
            </div>
          </div>
        </Card>
      </Col>
    </Row>

    <!-- 主内容区域 -->
    <Row :gutter="[16, 16]">
      <!-- 左侧 -->
      <Col :xs="24" :lg="16">
        <!-- 快捷入口 -->
        <Card title="快捷入口" class="mb-6" size="small">
          <template #extra>
            <span class="text-xs text-gray-400">常用功能</span>
          </template>
          <div class="shortcut-grid">
            <div
              v-for="sc in shortcuts"
              :key="sc.title"
              class="shortcut-item"
              @click="navTo(sc.link)"
            >
              <div
                class="shortcut-icon"
                :style="{ background: sc.color + '15', color: sc.color }"
              >
                <IconifyIcon :icon="sc.icon" class="text-xl" />
              </div>
              <div class="shortcut-title">{{ sc.title }}</div>
            </div>
          </div>
        </Card>

        <!-- 操作日志 -->
        <Card title="操作日志" size="small" class="mt-[10px]">
          <template #extra>
            <Button type="link" size="small">查看全部</Button>
          </template>
          <List :data-source="recentLogs" item-layout="horizontal" size="small">
            <template #renderItem="{ item }">
              <ListItem>
                <ListItemMeta>
                  <template #avatar>
                    <Avatar
                      :style="{
                        backgroundColor:
                          item.status === 'success' ? '#52c41a' : '#faad14',
                      }"
                    >
                      {{ item.user.charAt(0).toUpperCase() }}
                    </Avatar>
                  </template>
                  <template #title>
                    <span class="text-sm">
                      <span class="font-medium">{{ item.user }}</span>
                      <span class="ml-2 text-gray-600">{{ item.action }}</span>
                    </span>
                  </template>
                  <template #description>
                    <Tag
                      :color="item.status === 'success' ? 'green' : 'orange'"
                      class="mr-2"
                    >
                      {{ item.module }}
                    </Tag>
                    <span class="text-xs text-gray-400">{{ item.time }}</span>
                  </template>
                </ListItemMeta>
              </ListItem>
            </template>
          </List>
        </Card>
      </Col>

      <!-- 右侧 -->
      <Col :xs="24" :lg="8">
        <!-- 系统通知 -->
        <Card title="系统通知" class="mb-6" size="small">
          <template #extra>
            <Button type="link" size="small">更多</Button>
          </template>
          <List :data-source="notifications" size="small">
            <template #renderItem="{ item }">
              <ListItem>
                <div class="w-full">
                  <div class="flex items-center justify-between">
                    <Tag :color="item.color">{{ item.type }}</Tag>
                    <span class="text-xs text-gray-400">{{ item.time }}</span>
                  </div>
                  <div class="mt-2 text-sm">{{ item.title }}</div>
                </div>
              </ListItem>
            </template>
          </List>
        </Card>

        <!-- 系统资源 -->
        <Card title="系统资源" size="small" class="mt-[10px]">
          <template #extra>
            <span class="text-xs text-gray-400">实时</span>
          </template>
          <div class="resource-list">
            <div v-for="r in resources" :key="r.name" class="resource-item">
              <div class="flex justify-between mb-1">
                <span class="text-sm text-gray-600">{{ r.name }}</span>
                <span class="text-sm font-medium">{{ r.percent }}%</span>
              </div>
              <Progress
                :percent="r.percent"
                :status="r.status === 'active' ? 'active' : 'normal'"
                :show-info="false"
                size="small"
                :stroke-color="
                  r.percent > 80
                    ? '#ff4d4f'
                    : r.percent > 60
                      ? '#faad14'
                      : '#52c41a'
                "
              />
            </div>
          </div>
        </Card>
      </Col>
    </Row>
  </Page>
</template>

<style scoped>
.welcome-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: var(--vben-content-bg, #fff);
  border-radius: 8px;
  border: 1px solid var(--vben-border, #f0f0f0);
}

.welcome-meta {
  display: flex;
  align-items: center;
  gap: 20px;
}

.meta-item {
  text-align: center;
  min-width: 60px;
}

.meta-label {
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1;
  margin-bottom: 6px;
}

.meta-value {
  font-size: 20px;
  font-weight: 600;
  line-height: 1;
}

.meta-divider {
  height: 32px;
  margin: 0;
}

.stat-card {
  cursor: pointer;
  transition: all 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  flex-shrink: 0;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

@media (max-width: 768px) {
  .shortcut-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.shortcut-item:hover {
  background: rgba(0, 0, 0, 0.02);
}

.shortcut-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  margin-bottom: 8px;
}

.shortcut-title {
  font-size: 12px;
  color: #595959;
}

.resource-item {
  margin-bottom: 16px;
}

.resource-item:last-child {
  margin-bottom: 0;
}

:deep(.ant-list-item) {
  padding: 8px 0;
}

:deep(.ant-card-head) {
  min-height: 40px;
  padding: 0 16px;
}

:deep(.ant-card-head-title) {
  padding: 10px 0;
  font-size: 14px;
  font-weight: 600;
}
</style>
