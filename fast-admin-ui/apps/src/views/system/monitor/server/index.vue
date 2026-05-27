<script lang="ts" setup>
import type { SysMonitorApi } from '#/api/system/monitor';

import { computed, onBeforeUnmount, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import {
  Button,
  Card,
  Col,
  Descriptions,
  DescriptionsItem,
  Empty,
  Progress,
  Row,
  Switch,
  Table,
  Tag,
} from 'ant-design-vue';

import { getServerMonitor } from '#/api/system/monitor';

const loading = ref(false);
const data = ref<SysMonitorApi.ServerInfo>({});
const autoRefresh = ref(false);
const refreshInterval = ref<ReturnType<typeof setInterval> | null>(null);
const lastUpdate = ref('');

const cpu = computed(() => data.value.cpu || {});
const memory = computed(() => data.value.memory || {});
const server = computed(() => data.value.server || {});
const disks = computed(() => data.value.disks || []);
const jvm = computed(() => data.value.jvm || {});
const redis = computed(() => data.value.redis || {});
const ds = computed(() => data.value.datasource || {});

const heap = computed(() => (jvm.value as any).heap || {});
const nonHeap = computed(() => (jvm.value as any).nonHeap || {});

async function load() {
  try {
    loading.value = true;
    data.value = (await getServerMonitor()) || {};
    lastUpdate.value = new Date().toLocaleTimeString();
  } finally {
    loading.value = false;
  }
}

function progressColor(percent: number) {
  if (percent >= 80) return '#ff4d4f';
  if (percent >= 60) return '#faad14';
  return '#52c41a';
}

function toggleAutoRefresh(val: boolean | number | string) {
  if (val) {
    refreshInterval.value = setInterval(load, 3000);
  } else if (refreshInterval.value) {
    clearInterval(refreshInterval.value);
    refreshInterval.value = null;
  }
}

const diskColumns = [
  { title: '盘符', dataIndex: 'name', key: 'name' },
  { title: '挂载', dataIndex: 'mount', key: 'mount' },
  { title: '类型', dataIndex: 'type', key: 'type', width: 90 },
  { title: '总容量', dataIndex: 'totalGB', key: 'totalGB', width: 110,
    customRender: ({ text }: any) => `${text} GB` },
  { title: '已用', dataIndex: 'usedGB', key: 'usedGB', width: 110,
    customRender: ({ text }: any) => `${text} GB` },
  { title: '可用', dataIndex: 'freeGB', key: 'freeGB', width: 110,
    customRender: ({ text }: any) => `${text} GB` },
  { title: '使用率', dataIndex: 'usage', key: 'usage', width: 200,
    customRender: ({ text }: any) => text },
];

onMounted(load);
onBeforeUnmount(() => {
  if (refreshInterval.value) clearInterval(refreshInterval.value);
});
</script>

<template>
  <Page>
    <!-- 顶部工具栏 -->
    <div class="toolbar mb-4">
      <div class="flex items-center gap-2">
        <span class="text-base font-medium">服务器监控</span>
        <span v-if="lastUpdate" class="text-xs text-gray-500 ml-2">
          最近更新：{{ lastUpdate }}
        </span>
      </div>
      <div class="flex items-center gap-3">
        <span class="text-sm text-gray-600">自动刷新（3s）</span>
        <Switch v-model:checked="autoRefresh" @change="toggleAutoRefresh" />
        <Button :loading="loading" @click="load">
          <template #icon>
            <IconifyIcon icon="lucide:refresh-cw" />
          </template>
          刷新
        </Button>
      </div>
    </div>

    <!-- 上：核心指标卡 -->
    <Row :gutter="[16, 16]" class="mb-4">
      <!-- CPU -->
      <Col :xs="24" :sm="12" :lg="8" class="metric-col">
        <Card size="small" class="metric-card" :body-style="{ padding: '20px' }">
          <div class="card-header">
            <div class="card-title">
              <IconifyIcon icon="lucide:cpu" class="text-lg mr-2" />
              CPU
            </div>
            <Tag>{{ cpu.logicalCores }} 核</Tag>
          </div>
          <Progress
            type="dashboard"
            :percent="(cpu.usage as number) || 0"
            :stroke-color="progressColor((cpu.usage as number) || 0)"
            class="dashboard-progress"
          />
          <div class="metric-rows mt-3">
            <div><span>用户</span><span>{{ cpu.user }}%</span></div>
            <div><span>系统</span><span>{{ cpu.system }}%</span></div>
            <div><span>空闲</span><span>{{ cpu.idle }}%</span></div>
            <div><span>IO Wait</span><span>{{ cpu.ioWait }}%</span></div>
          </div>
          <div class="text-xs text-gray-500 mt-2 truncate">
            {{ cpu.name }}
          </div>
        </Card>
      </Col>

      <!-- 内存 -->
      <Col :xs="24" :sm="12" :lg="8" class="metric-col">
        <Card size="small" class="metric-card" :body-style="{ padding: '20px' }">
          <div class="card-header">
            <div class="card-title">
              <IconifyIcon icon="lucide:memory-stick" class="text-lg mr-2" />
              内存
            </div>
            <Tag>{{ memory.totalGB }} GB</Tag>
          </div>
          <Progress
            type="dashboard"
            :percent="(memory.usage as number) || 0"
            :stroke-color="progressColor((memory.usage as number) || 0)"
            class="dashboard-progress"
          />
          <div class="metric-rows mt-3">
            <div><span>总量</span><span>{{ memory.totalGB }} GB</span></div>
            <div><span>已用</span><span>{{ memory.usedGB }} GB</span></div>
            <div><span>可用</span><span>{{ memory.freeGB }} GB</span></div>
          </div>
        </Card>
      </Col>

      <!-- JVM -->
      <Col :xs="24" :lg="8" class="metric-col">
        <Card size="small" class="metric-card" :body-style="{ padding: '20px' }">
          <div class="card-header">
            <div class="card-title">
              <IconifyIcon icon="lucide:flame" class="text-lg mr-2" />
              JVM 堆内存
            </div>
            <Tag>{{ heap.maxMB }} MB</Tag>
          </div>
          <Progress
            type="dashboard"
            :percent="(heap.usage as number) || 0"
            :stroke-color="progressColor((heap.usage as number) || 0)"
            class="dashboard-progress"
          />
          <div class="metric-rows mt-3">
            <div><span>最大</span><span>{{ heap.maxMB }} MB</span></div>
            <div><span>已用</span><span>{{ heap.usedMB }} MB</span></div>
            <div><span>线程</span><span>{{ jvm.threads }} (守护 {{ jvm.daemonThreads }})</span></div>
          </div>
        </Card>
      </Col>
    </Row>

    <!-- 下：详细信息 -->
    <Row :gutter="[16, 16]">
      <!-- 服务器信息 -->
      <Col :xs="24" :lg="12">
        <Card title="服务器信息" size="small">
          <Descriptions :column="1" size="small" bordered :label-style="{ width: '140px' }">
            <DescriptionsItem label="主机名">{{ server.hostName }}</DescriptionsItem>
            <DescriptionsItem label="主机 IP">{{ server.hostIp }}</DescriptionsItem>
            <DescriptionsItem label="操作系统">
              {{ server.osName }} ({{ server.osVersion }})
            </DescriptionsItem>
            <DescriptionsItem label="架构">{{ server.osArch }}</DescriptionsItem>
            <DescriptionsItem label="项目目录">
              <span class="text-xs">{{ server.userDir }}</span>
            </DescriptionsItem>
          </Descriptions>
        </Card>
      </Col>

      <!-- JVM 详情 -->
      <Col :xs="24" :lg="12">
        <Card title="JVM 详情" size="small">
          <Descriptions :column="1" size="small" bordered :label-style="{ width: '140px' }">
            <DescriptionsItem label="Java 版本">{{ jvm.javaVersion }}</DescriptionsItem>
            <DescriptionsItem label="Vendor">{{ jvm.javaVendor }}</DescriptionsItem>
            <DescriptionsItem label="运行时长">{{ jvm.uptime }}</DescriptionsItem>
            <DescriptionsItem label="非堆已用">
              {{ nonHeap.usedMB }} MB / {{ nonHeap.committedMB }} MB
            </DescriptionsItem>
            <DescriptionsItem label="峰值线程">{{ jvm.peakThreads }}</DescriptionsItem>
          </Descriptions>
        </Card>
      </Col>

      <!-- Redis -->
      <Col :xs="24" :lg="12">
        <Card title="Redis 状态" size="small">
          <Empty v-if="(redis as any).error" :description="(redis as any).error" />
          <Descriptions v-else :column="1" size="small" bordered :label-style="{ width: '140px' }">
            <DescriptionsItem label="版本">{{ redis.version }}</DescriptionsItem>
            <DescriptionsItem label="模式">{{ redis.mode }}</DescriptionsItem>
            <DescriptionsItem label="运行时长">{{ redis.uptime }}</DescriptionsItem>
            <DescriptionsItem label="连接数">{{ redis.connectedClients }}</DescriptionsItem>
            <DescriptionsItem label="已用内存">
              {{ redis.usedMemoryHuman }} (峰值 {{ redis.usedMemoryPeakHuman }})
            </DescriptionsItem>
            <DescriptionsItem label="Key 数量">{{ redis.dbSize }}</DescriptionsItem>
            <DescriptionsItem label="命中率">
              <Tag color="blue">{{ redis.hitRate }}</Tag>
            </DescriptionsItem>
            <DescriptionsItem label="累计命令">{{ redis.commandsProcessed }}</DescriptionsItem>
          </Descriptions>
        </Card>
      </Col>

      <!-- DataSource -->
      <Col :xs="24" :lg="12">
        <Card title="数据库连接池 (Hikari)" size="small">
          <Descriptions :column="1" size="small" bordered :label-style="{ width: '140px' }">
            <DescriptionsItem label="连接池名">{{ ds.poolName }}</DescriptionsItem>
            <DescriptionsItem label="驱动">{{ ds.driver }}</DescriptionsItem>
            <DescriptionsItem label="最大连接数">{{ ds.maxPoolSize }}</DescriptionsItem>
            <DescriptionsItem label="最小空闲">{{ ds.minIdle }}</DescriptionsItem>
            <DescriptionsItem label="活跃连接">
              <Tag color="green">{{ ds.active }}</Tag>
            </DescriptionsItem>
            <DescriptionsItem label="空闲连接">
              <Tag>{{ ds.idle }}</Tag>
            </DescriptionsItem>
            <DescriptionsItem label="总连接">{{ ds.total }}</DescriptionsItem>
            <DescriptionsItem label="等待中">
              <Tag :color="(ds.waiting as number) > 0 ? 'orange' : 'default'">
                {{ ds.waiting }}
              </Tag>
            </DescriptionsItem>
          </Descriptions>
        </Card>
      </Col>

      <!-- 磁盘 -->
      <Col :span="24">
        <Card title="磁盘信息" size="small">
          <Table
            :data-source="disks"
            :columns="diskColumns"
            :pagination="false"
            size="small"
            row-key="mount"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'usage'">
                <Progress
                  :percent="record.usage"
                  :stroke-color="progressColor(record.usage)"
                  :show-info="true"
                  size="small"
                />
              </template>
            </template>
          </Table>
        </Card>
      </Col>
    </Row>
  </Page>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--vben-content-bg, #fff);
  border-radius: 6px;
  border: 1px solid var(--vben-border, #f0f0f0);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.metric-col {
  display: flex;
}

.metric-card {
  width: 100%;
  height: 100%;
}

.metric-card :deep(.ant-card-body) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-title {
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  color: #595959;
}

.dashboard-progress {
  display: flex;
  justify-content: center;
  flex: 1;
  align-items: center;
}

.dashboard-progress :deep(.ant-progress-text) {
  font-size: 22px !important;
  font-weight: 600;
}

.metric-rows {
  font-size: 12px;
  color: #595959;
  min-height: 116px;
}

.metric-rows > div {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  border-bottom: 1px dashed #f0f0f0;
}

.metric-rows > div:last-child {
  border-bottom: none;
}

.metric-rows > div > span:last-child {
  font-weight: 500;
  color: #262626;
}
</style>
