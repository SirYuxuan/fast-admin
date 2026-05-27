<script lang="ts" setup>
import type { SysOnlineApi } from '#/api/system/online';

import { onMounted, ref } from 'vue';

import type { TableColumnsType } from 'ant-design-vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import {
  Avatar,
  Button,
  Card,
  Input,
  message,
  Modal as AModal,
  Table,
} from 'ant-design-vue';

import { getOnlineUsers, kickoutUser } from '#/api/system/online';

const list = ref<SysOnlineApi.OnlineUser[]>([]);
const loading = ref(false);
const keyword = ref('');

async function load() {
  try {
    loading.value = true;
    list.value = (await getOnlineUsers(keyword.value || undefined)) || [];
  } finally {
    loading.value = false;
  }
}

function onKickout(row: SysOnlineApi.OnlineUser) {
  AModal.confirm({
    title: '确认强制下线',
    content: `将强制下线用户「${row.nickname || row.username}」，确认吗？`,
    okText: '强制下线',
    okType: 'danger',
    onOk: () =>
      kickoutUser(row.tokenValue).then(() => {
        message.success('已下线');
        load();
      }),
  });
}

function formatTime(t?: number) {
  if (!t) return '-';
  return new Date(t).toLocaleString();
}

function formatTimeout(s?: number) {
  if (!s || s < 0) return '永久';
  if (s < 60) return `${s} 秒`;
  if (s < 3600) return `${Math.floor(s / 60)} 分钟`;
  if (s < 86400) return `${Math.floor(s / 3600)} 小时`;
  return `${Math.floor(s / 86400)} 天`;
}

const columns: TableColumnsType = [
  {
    title: '用户',
    key: 'user',
    minWidth: 180,
  },
  { title: 'IP', dataIndex: 'loginIp', width: 140 },
  {
    title: '浏览器',
    dataIndex: 'browser',
    width: 100,
    customRender: ({ text }: any) => text || '-',
  },
  {
    title: '操作系统',
    dataIndex: 'os',
    width: 130,
    customRender: ({ text }: any) => text || '-',
  },
  {
    title: '登录时间',
    dataIndex: 'loginTime',
    width: 180,
    customRender: ({ text }: any) => formatTime(text),
  },
  {
    title: '剩余有效期',
    dataIndex: 'tokenTimeout',
    width: 130,
    customRender: ({ text }: any) => formatTimeout(text),
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    align: 'center',
    fixed: 'right',
  },
];

onMounted(load);
</script>

<template>
  <Page>
    <Card size="small">
      <div class="toolbar mb-3">
        <div class="flex items-center gap-2">
          <Input
            v-model:value="keyword"
            allow-clear
            placeholder="搜索用户名/昵称"
            class="w-60"
            @press-enter="load"
          >
            <template #prefix>
              <IconifyIcon icon="lucide:search" class="text-gray-400" />
            </template>
          </Input>
          <Button :loading="loading" @click="load">
            <template #icon>
              <IconifyIcon icon="lucide:refresh-cw" />
            </template>
            刷新
          </Button>
        </div>
        <div class="text-sm text-gray-500">
          当前在线 <span class="text-blue-500 font-medium">{{ list.length }}</span> 人
        </div>
      </div>

      <Table
        :data-source="list"
        :columns="columns"
        :pagination="false"
        :loading="loading"
        size="middle"
        row-key="tokenValue"
        :scroll="{ x: 1200 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'user'">
            <div class="flex items-center gap-2">
              <Avatar size="small">
                {{ (record.username || '').charAt(0).toUpperCase() }}
              </Avatar>
              <div>
                <div class="font-medium">{{ record.nickname || record.username }}</div>
                <div class="text-xs text-gray-500">@{{ record.username }}</div>
              </div>
            </div>
          </template>
          <template v-if="column.key === 'action'">
            <Button type="link" danger size="small" @click="onKickout(record as SysOnlineApi.OnlineUser)">
              <template #icon>
                <IconifyIcon icon="lucide:log-out" />
              </template>
              下线
            </Button>
          </template>
        </template>
      </Table>
    </Card>
  </Page>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
