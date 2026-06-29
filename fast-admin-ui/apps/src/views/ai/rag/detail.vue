<script lang="ts" setup>
import type { UploadProps } from 'ant-design-vue';

import type { AiRagApi } from '#/api/ai/rag';

import { computed, h, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { AccessControl } from '@vben/access';
import { Page } from '@vben/common-ui';
import { ArrowLeft, createIconifyIcon, Plus, RotateCw } from '@vben/icons';

import {
  Button,
  Card,
  Checkbox,
  Empty,
  Input,
  InputNumber,
  message,
  Modal as AModal,
  Spin,
  Table as ATable,
  Tabs,
  Tag,
  Upload as AUpload,
} from 'ant-design-vue';

import {
  deleteAiKnowledgeDocument,
  getAiKnowledgeBaseDetail,
  getAiKnowledgeDocumentPage,
  getAiRagVectorStoreStatus,
  recallAiKnowledgeBase,
  reindexAiKnowledgeDocument,
  uploadAiKnowledgeDocument,
} from '#/api/ai/rag';

import { DOC_STATUS_TAGS, formatSize } from './data';

const DatabaseIcon = createIconifyIcon('lucide:database-zap');
const SearchIcon = createIconifyIcon('lucide:search-check');
const FileIcon = createIconifyIcon('lucide:file-text');
const INDEX_POLL_INTERVAL = 2500;

const route = useRoute();
const router = useRouter();

const kbId = computed(() => String(route.params.id ?? ''));

const kb = ref<AiRagApi.KnowledgeBase>();
const kbLoading = ref(false);
const activeTab = ref('documents');

// 文档管理
const docs = ref<AiRagApi.KnowledgeDocument[]>([]);
const docsLoading = ref(false);
const uploading = ref(false);
const docPage = ref(1);
const docPageSize = ref(10);
const docTotal = ref(0);
let indexPollTimer: number | undefined;

// 召回测试
const recallQuery = ref('');
const recallTopK = ref(5);
const recallLoading = ref(false);
const recallResult = ref<AiRagApi.RecallResult>();
const vectorStatus = ref<AiRagApi.VectorStoreStatus>();

const docColumns = [
  { title: '文件名', dataIndex: 'fileName', key: 'fileName' },
  { title: '大小', dataIndex: 'fileSize', key: 'fileSize', width: 110 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 140 },
  { title: '切片数', dataIndex: 'chunkCount', key: 'chunkCount', width: 90 },
  { title: '索引时间', dataIndex: 'indexedAt', key: 'indexedAt', width: 180 },
  { title: '操作', key: 'operation', width: 140 },
];

const metrics = computed(() => [
  { label: '文档数', value: kb.value?.documentCount ?? 0 },
  { label: '切片数', value: kb.value?.chunkCount ?? 0 },
  { label: '最后索引', value: kb.value?.lastIndexedAt || '尚未索引' },
]);

function statusTag(status: string) {
  return (
    DOC_STATUS_TAGS.find((tag) => tag.value === status) || {
      label: status,
      value: status,
      color: 'default',
    }
  );
}

function goBack() {
  router.push('/ai/rag');
}

function openChunks(row: Record<string, any>) {
  if (!kbId.value || !row.id || !row.chunkCount) return;
  router.push(`/ai/rag/${kbId.value}/documents/${row.id}/chunks`);
}

async function loadKb(showLoading = true) {
  if (!kbId.value) return;
  if (showLoading) {
    kbLoading.value = true;
  }
  try {
    kb.value = await getAiKnowledgeBaseDetail(kbId.value);
  } finally {
    if (showLoading) {
      kbLoading.value = false;
    }
  }
}

async function loadDocuments(showLoading = true) {
  if (!kbId.value) return;
  if (showLoading) {
    docsLoading.value = true;
  }
  try {
    const page = await getAiKnowledgeDocumentPage({
      knowledgeBaseId: kbId.value,
      page: docPage.value,
      pageSize: docPageSize.value,
    });
    docs.value = page.items || [];
    docTotal.value = page.total || 0;
  } finally {
    if (showLoading) {
      docsLoading.value = false;
    }
    scheduleIndexPolling();
  }
}

function hasRunningIndex() {
  return docs.value.some((doc) => doc.status === 'pending' || doc.status === 'indexing');
}

function clearIndexPolling() {
  if (indexPollTimer) {
    window.clearTimeout(indexPollTimer);
    indexPollTimer = undefined;
  }
}

function scheduleIndexPolling() {
  clearIndexPolling();
  if (!hasRunningIndex()) return;
  indexPollTimer = window.setTimeout(async () => {
    indexPollTimer = undefined;
    await Promise.all([loadDocuments(false), loadKb(false)]);
  }, INDEX_POLL_INTERVAL);
}

function onDocTableChange(pagination: { current?: number; pageSize?: number }) {
  docPage.value = pagination.current || 1;
  docPageSize.value = pagination.pageSize || 10;
  loadDocuments();
}

const beforeUpload: UploadProps['beforeUpload'] = async (file) => {
  if (!kbId.value) return false;
  uploading.value = true;
  const hide = message.loading({
    content: `正在上传: ${file.name}`,
    duration: 0,
  });
  try {
    await uploadAiKnowledgeDocument(kbId.value, file as File);
    message.success('上传完成，已提交后台索引');
    await Promise.all([loadDocuments(), loadKb()]);
  } finally {
    hide();
    uploading.value = false;
  }
  return false;
};

function reindex(row: Record<string, any>) {
  AModal.confirm({
    content: `确认重建「${row.fileName}」索引？`,
    title: '重建索引',
    onOk: async () => {
      await reindexAiKnowledgeDocument(row.id);
      message.success('已提交后台重建索引');
      await Promise.all([loadDocuments(), loadKb()]);
    },
  });
}

function deleteDocument(row: Record<string, any>) {
  let deleteSourceFile = false;
  AModal.confirm({
    title: '删除确认',
    content: h('div', [
      h('div', `确认删除文档「${row.fileName}」？知识库索引会同步删除。`),
      h(
        Checkbox,
        {
          class: 'mt-3',
          onChange: (e: any) => {
            deleteSourceFile = e.target.checked;
          },
        },
        () => '同时删除源文件（系统文件列表中的原始文件）',
      ),
    ]),
    onOk: async () => {
      await deleteAiKnowledgeDocument(row.id, deleteSourceFile);
      message.success('删除成功');
      // 删除当前页最后一条且非首页时，回退一页避免空页
      if (docs.value.length === 1 && docPage.value > 1) {
        docPage.value -= 1;
      }
      await Promise.all([loadDocuments(), loadKb()]);
    },
  });
}

async function loadVectorStatus() {
  vectorStatus.value = await getAiRagVectorStoreStatus();
}

async function runRecall() {
  if (!kbId.value || !recallQuery.value.trim()) {
    message.warning('请输入召回问题');
    return;
  }
  recallLoading.value = true;
  try {
    recallResult.value = await recallAiKnowledgeBase({
      knowledgeBaseId: kbId.value,
      query: recallQuery.value,
      topK: recallTopK.value,
    });
  } finally {
    recallLoading.value = false;
  }
}

function onTabChange(key: number | string) {
  if (key === 'recall' && !vectorStatus.value) {
    loadVectorStatus();
  }
}

onMounted(() => {
  loadKb();
  loadDocuments();
});

onBeforeUnmount(() => {
  clearIndexPolling();
});
</script>

<template>
  <Page auto-content-height>
    <div class="rag-detail">
      <!-- 头部信息卡 -->
      <Card class="rag-detail__header" :body-style="{ padding: '20px 24px' }">
        <Spin :spinning="kbLoading">
          <div class="flex items-center gap-2">
            <Button type="text" size="small" class="-ml-2" @click="goBack">
              <ArrowLeft class="size-4" />
              返回知识库
            </Button>
          </div>

          <div class="mt-3 flex flex-wrap items-center gap-5">
            <div
              class="flex size-14 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary"
            >
              <DatabaseIcon class="size-7" />
            </div>

            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-3">
                <span class="truncate text-lg font-semibold">
                  {{ kb?.name || '知识库' }}
                </span>
                <Tag :color="kb?.enabled ? 'success' : 'default'">
                  {{ kb?.enabled ? '启用' : '禁用' }}
                </Tag>
                <Tag color="blue" :bordered="false">
                  切片 {{ kb?.chunkSize ?? '-' }} / 重叠 {{ kb?.chunkOverlap ?? '-' }}
                </Tag>
                <Tag color="cyan" :bordered="false">
                  分隔符 {{ kb?.chunkDelimiter || '\\n\\n' }}
                </Tag>
              </div>
              <div
                class="mt-1 line-clamp-1 text-sm text-gray-500 dark:text-gray-400"
              >
                {{ kb?.description || '暂无描述' }}
              </div>
            </div>

            <div class="flex items-stretch gap-6">
              <div
                v-for="(m, i) in metrics"
                :key="m.label"
                class="flex flex-col justify-center px-1"
                :class="
                  i > 0 ? 'border-l border-gray-200 pl-6 dark:border-gray-700' : ''
                "
              >
                <span class="text-xs text-gray-400">{{ m.label }}</span>
                <span class="mt-1 text-xl font-semibold tabular-nums">
                  {{ m.value }}
                </span>
              </div>
            </div>
          </div>
        </Spin>
      </Card>

      <!-- 功能区 -->
      <Card class="rag-detail__content" :body-style="{ paddingTop: '8px' }">
        <Tabs
          v-model:active-key="activeTab"
          class="rag-detail__tabs"
          @change="onTabChange"
        >
        <Tabs.TabPane key="documents">
          <template #tab>
            <span class="flex items-center gap-1.5">
              <FileIcon class="size-4" />
              文档管理
            </span>
          </template>

          <div class="rag-detail__documents">
            <div class="rag-detail__documents-toolbar">
              <span class="text-xs text-gray-400">
                支持 txt / md / csv / json / xml / html / yml / Word / PPT / Excel，上传后后台自动索引
              </span>
              <AccessControl type="code" :codes="['ai:rag:upload']">
                <AUpload
                  :before-upload="beforeUpload"
                  :show-upload-list="false"
                  :disabled="uploading"
                >
                  <Button type="primary" :loading="uploading">
                    <Plus class="size-4" />
                    上传文档
                  </Button>
                </AUpload>
              </AccessControl>
            </div>

            <ATable
              class="rag-detail__documents-table"
              row-key="id"
              size="middle"
              :columns="docColumns"
              :data-source="docs"
              :loading="docsLoading"
              :pagination="{
                current: docPage,
                pageSize: docPageSize,
                total: docTotal,
                showSizeChanger: true,
                showTotal: (t) => `共 ${t} 条`,
              }"
              :scroll="{ x: 900, y: 'max(320px, calc(100vh - 448px))' }"
              @change="onDocTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'fileName'">
                  <span class="flex items-center gap-2">
                    <FileIcon class="size-4 text-gray-400" />
                    <span class="truncate">{{ record.fileName }}</span>
                  </span>
                </template>
                <template v-else-if="column.key === 'fileSize'">
                  {{ formatSize(record.fileSize) }}
                </template>
                <template v-else-if="column.key === 'chunkCount'">
                  <Button
                    v-if="record.chunkCount > 0"
                    type="link"
                    size="small"
                    class="px-0"
                    @click="openChunks(record)"
                  >
                    {{ record.chunkCount }}
                  </Button>
                  <span v-else class="text-gray-400">0</span>
                </template>
                <template v-else-if="column.key === 'status'">
                  <Tag :color="statusTag(record.status).color">
                    {{ statusTag(record.status).label }}
                  </Tag>
                  <div
                    v-if="record.status === 'failed' && record.errorMsg"
                    class="mt-1 text-xs text-red-500"
                  >
                    {{ record.errorMsg }}
                  </div>
                </template>
                <template v-else-if="column.key === 'operation'">
                  <span class="flex items-center gap-1">
                    <AccessControl type="code" :codes="['ai:rag:reindex']">
                      <Button
                        type="link"
                        size="small"
                        :disabled="record.status === 'pending' || record.status === 'indexing'"
                        @click="reindex(record)"
                      >
                        <RotateCw class="size-3.5" />
                        重建
                      </Button>
                    </AccessControl>
                    <AccessControl type="code" :codes="['ai:rag:delete']">
                      <Button
                        danger
                        type="link"
                        size="small"
                        @click="deleteDocument(record)"
                      >
                        删除
                      </Button>
                    </AccessControl>
                  </span>
                </template>
              </template>
            </ATable>
          </div>
        </Tabs.TabPane>

        <Tabs.TabPane key="recall">
          <template #tab>
            <span class="flex items-center gap-1.5">
              <SearchIcon class="size-4" />
              召回测试
            </span>
          </template>

          <div
            class="mt-2 mb-4 flex items-center justify-between rounded-lg border border-gray-200 px-4 py-3 dark:border-gray-700"
          >
            <div class="flex items-center gap-2 text-sm">
              <span
                class="inline-block size-2 rounded-full"
                :class="vectorStatus?.connected ? 'bg-green-500' : 'bg-gray-300'"
              />
              <span class="font-medium">
                Qdrant {{ vectorStatus?.connected ? '已连接' : '未连接' }}
              </span>
            </div>
            <div class="truncate text-xs text-gray-400">
              {{ vectorStatus?.url }} · {{ vectorStatus?.defaultCollection }}
            </div>
          </div>

          <div class="flex items-start gap-3">
            <Input.TextArea
              v-model:value="recallQuery"
              :rows="3"
              placeholder="输入一个问题，测试知识库能召回哪些片段"
              @press-enter="runRecall"
            />
            <div class="flex w-32 shrink-0 flex-col gap-2">
              <InputNumber
                v-model:value="recallTopK"
                :min="1"
                :max="20"
                class="w-full"
                addon-before="TopK"
              />
              <Button
                type="primary"
                block
                :loading="recallLoading"
                @click="runRecall"
              >
                <SearchIcon class="size-4" />
                召回
              </Button>
            </div>
          </div>

          <Spin :spinning="recallLoading">
            <div v-if="recallResult" class="mt-4">
              <div class="mb-3 text-sm text-gray-500 dark:text-gray-400">
                耗时 {{ recallResult.latencyMs }} ms，返回
                {{ recallResult.items.length }} 条
              </div>
              <Empty
                v-if="recallResult.items.length === 0"
                description="未召回到相关片段"
              />
              <div v-else class="space-y-3">
                <div
                  v-for="(item, idx) in recallResult.items"
                  :key="item.chunkId"
                  class="rounded-lg border border-gray-200 p-4 transition-colors hover:border-primary/40 dark:border-gray-700"
                >
                  <div class="mb-2 flex items-center justify-between gap-2 text-sm">
                    <span class="flex items-center gap-2 truncate font-medium">
                      <span
                        class="flex size-5 shrink-0 items-center justify-center rounded bg-gray-100 text-xs text-gray-500 dark:bg-gray-700 dark:text-gray-300"
                      >
                        {{ idx + 1 }}
                      </span>
                      <FileIcon class="size-4 text-gray-400" />
                      {{ item.fileName || '未知文档' }}
                    </span>
                    <Tag color="blue" :bordered="false">
                      score {{ item.score?.toFixed(4) }}
                    </Tag>
                  </div>
                  <div
                    class="whitespace-pre-wrap rounded bg-gray-50 p-3 text-sm leading-6 text-gray-700 dark:bg-gray-800/50 dark:text-gray-300"
                  >
                    {{ item.content }}
                  </div>
                </div>
              </div>
            </div>
            <Empty
              v-else
              class="py-10"
              description="输入问题并点击「召回」查看命中片段"
            />
          </Spin>
        </Tabs.TabPane>
        </Tabs>
      </Card>
    </div>
  </Page>
</template>

<style scoped>
.rag-detail {
  display: flex;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  gap: 16px;
}

.rag-detail__header {
  flex: 0 0 auto;
}

.rag-detail__content {
  display: flex;
  min-height: 0;
  flex: 1 1 auto;
  flex-direction: column;
}

.rag-detail__content :deep(.ant-card-body) {
  display: flex;
  min-height: 0;
  flex: 1 1 auto;
  flex-direction: column;
}

.rag-detail__tabs {
  display: flex;
  min-height: 0;
  flex: 1 1 auto;
  flex-direction: column;
}

.rag-detail__tabs :deep(.ant-tabs-content-holder) {
  min-height: 0;
  flex: 1 1 auto;
}

.rag-detail__tabs :deep(.ant-tabs-content) {
  height: 100%;
}

.rag-detail__tabs :deep(.ant-tabs-tabpane) {
  height: 100%;
  min-height: 0;
}

.rag-detail__documents {
  display: flex;
  height: 100%;
  min-height: 0;
  flex-direction: column;
}

.rag-detail__documents-toolbar {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 8px 0 12px;
}

.rag-detail__documents-table {
  display: flex;
  min-height: 0;
  flex: 1 1 auto;
  flex-direction: column;
}

.rag-detail__documents-table :deep(.ant-spin-nested-loading),
.rag-detail__documents-table :deep(.ant-spin-container) {
  display: flex;
  min-height: 0;
  flex: 1 1 auto;
  flex-direction: column;
}

.rag-detail__documents-table :deep(.ant-table) {
  min-height: 0;
  flex: 1 1 auto;
}

.rag-detail__documents-table :deep(.ant-table-pagination) {
  flex: 0 0 auto;
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .rag-detail__documents-toolbar {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
