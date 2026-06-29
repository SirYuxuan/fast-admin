<script lang="ts" setup>
import type { AiRagApi } from '#/api/ai/rag';

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import {
  ArrowLeft,
  Copy,
  createIconifyIcon,
  Search,
} from '@vben/icons';

import {
  Button,
  Card,
  Empty,
  Input,
  message,
  Pagination,
  Spin,
  Tag,
} from 'ant-design-vue';

import {
  getAiKnowledgeBaseDetail,
  getAiKnowledgeChunkPage,
  getAiKnowledgeDocumentDetail,
} from '#/api/ai/rag';

import { DOC_STATUS_TAGS, formatSize } from './data';

const DatabaseIcon = createIconifyIcon('lucide:database-zap');
const FileTextIcon = createIconifyIcon('lucide:file-text');

const route = useRoute();
const router = useRouter();

const kbId = computed(() => String(route.params.id ?? ''));
const documentId = computed(() => String(route.params.documentId ?? ''));

const kb = ref<AiRagApi.KnowledgeBase>();
const doc = ref<AiRagApi.KnowledgeDocument>();
const chunks = ref<AiRagApi.KnowledgeChunk[]>([]);
const loading = ref(false);
const keyword = ref('');
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const expandedChunkIds = ref<string[]>([]);

const statusTag = computed(() => {
  const status = doc.value?.status || 'pending';
  return (
    DOC_STATUS_TAGS.find((tag) => tag.value === status) || {
      label: status,
      color: 'default',
      value: status,
    }
  );
});

async function loadBaseInfo() {
  if (!kbId.value || !documentId.value) return;
  const [kbData, docData] = await Promise.all([
    getAiKnowledgeBaseDetail(kbId.value),
    getAiKnowledgeDocumentDetail(documentId.value),
  ]);
  kb.value = kbData;
  doc.value = docData;
}

async function loadChunks() {
  if (!documentId.value) return;
  loading.value = true;
  try {
    const result = await getAiKnowledgeChunkPage(documentId.value, {
      content: keyword.value.trim() || undefined,
      page: page.value,
      pageSize: pageSize.value,
    });
    chunks.value = result.items || [];
    total.value = result.total || 0;
    expandedChunkIds.value = [];
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.push(`/ai/rag/${kbId.value}`);
}

function searchChunks() {
  page.value = 1;
  loadChunks();
}

function onPageChange(current: number, size: number) {
  page.value = current;
  pageSize.value = size;
  loadChunks();
}

async function copyChunk(content: string) {
  await navigator.clipboard.writeText(content);
  message.success('已复制切片内容');
}

function isExpanded(id: string) {
  return expandedChunkIds.value.includes(id);
}

function toggleChunk(id: string) {
  expandedChunkIds.value = isExpanded(id)
    ? expandedChunkIds.value.filter((item) => item !== id)
    : [...expandedChunkIds.value, id];
}

onMounted(async () => {
  await loadBaseInfo();
  await loadChunks();
});
</script>

<template>
  <Page auto-content-height>
    <div class="rag-chunks">
      <Card class="rag-chunks__header" :body-style="{ padding: '20px 24px' }">
        <div class="flex items-center gap-2">
          <Button type="text" size="small" class="-ml-2" @click="goBack">
            <ArrowLeft class="size-4" />
            返回文档
          </Button>
        </div>

        <div class="mt-3 flex flex-wrap items-center gap-5">
          <div
            class="flex size-14 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary"
          >
            <DatabaseIcon class="size-7" />
          </div>
          <div class="min-w-0 flex-1">
            <div class="flex min-w-0 items-center gap-3">
              <span class="truncate text-lg font-semibold">
                {{ doc?.fileName || '文档切片' }}
              </span>
              <Tag :color="statusTag.color">{{ statusTag.label }}</Tag>
            </div>
            <div class="mt-1 flex flex-wrap items-center gap-2 text-sm text-gray-500">
              <span class="truncate">{{ kb?.name || '知识库' }}</span>
              <span>·</span>
              <span>{{ formatSize(doc?.fileSize) }}</span>
              <span>·</span>
              <span>共 {{ doc?.chunkCount ?? 0 }} 个切片</span>
            </div>
          </div>
        </div>
      </Card>

      <Card class="rag-chunks__content" :body-style="{ padding: '16px 20px' }">
        <div class="rag-chunks__toolbar">
          <Input
            v-model:value="keyword"
            allow-clear
            class="max-w-md"
            placeholder="搜索切片内容"
            @press-enter="searchChunks"
          >
            <template #prefix>
              <Search class="size-4 text-gray-400" />
            </template>
          </Input>
          <Button type="primary" @click="searchChunks">搜索</Button>
        </div>

        <Spin :spinning="loading">
          <Empty
            v-if="chunks.length === 0"
            class="py-16"
            description="暂无切片"
          />
          <div v-else class="rag-chunks__list">
            <div
              v-for="chunk in chunks"
              :key="chunk.id"
              class="rag-chunks__item"
              :class="{ 'is-expanded': isExpanded(chunk.id) }"
            >
              <div class="rag-chunks__item-header" @click="toggleChunk(chunk.id)">
                <div class="flex min-w-0 items-center gap-2">
                  <span class="rag-chunks__index">
                    #{{ (chunk.chunkIndex ?? 0) + 1 }}
                  </span>
                  <FileTextIcon class="size-4 shrink-0 text-gray-400" />
                  <span class="truncate text-sm font-medium">
                    Segment {{ (chunk.chunkIndex ?? 0) + 1 }}
                  </span>
                </div>
                <div class="flex shrink-0 items-center gap-2">
                  <Tag color="blue" :bordered="false">
                    {{ chunk.tokenCount || 0 }} tokens
                  </Tag>
                  <Button
                    type="text"
                    size="small"
                    @click.stop="copyChunk(chunk.content)"
                  >
                    <Copy class="size-4" />
                  </Button>
                </div>
              </div>
              <div class="rag-chunks__text" @click="toggleChunk(chunk.id)">
                {{ chunk.content }}
              </div>
              <div class="rag-chunks__item-footer" @click="toggleChunk(chunk.id)">
                {{ isExpanded(chunk.id) ? '收起' : '展开全文' }}
              </div>
            </div>
          </div>
        </Spin>

        <div class="rag-chunks__pagination">
          <Pagination
            v-model:current="page"
            v-model:page-size="pageSize"
            show-size-changer
            :total="total"
            :show-total="(t) => `共 ${t} 条`"
            @change="onPageChange"
            @show-size-change="onPageChange"
          />
        </div>
      </Card>
    </div>
  </Page>
</template>

<style scoped>
.rag-chunks {
  display: flex;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  gap: 16px;
}

.rag-chunks__header {
  flex: 0 0 auto;
}

.rag-chunks__content {
  display: flex;
  min-height: 0;
  flex: 1 1 auto;
  flex-direction: column;
}

.rag-chunks__content :deep(.ant-card-body) {
  display: flex;
  min-height: 0;
  flex: 1 1 auto;
  flex-direction: column;
}

.rag-chunks__toolbar {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.rag-chunks__list {
  display: flex;
  min-height: 0;
  flex: 1 1 auto;
  flex-direction: column;
  gap: 12px;
}

.rag-chunks__item {
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
  background: hsl(var(--background));
  transition: border-color 0.2s ease;
}

.rag-chunks__item:hover {
  border-color: hsl(var(--primary) / 45%);
}

.rag-chunks__item-header {
  display: flex;
  cursor: pointer;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid hsl(var(--border));
  padding: 10px 14px;
}

.rag-chunks__index {
  display: inline-flex;
  min-width: 42px;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: hsl(var(--muted));
  padding: 2px 8px;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
}

.rag-chunks__text {
  max-height: 104px;
  overflow: auto;
  white-space: pre-wrap;
  padding: 14px;
  color: hsl(var(--foreground));
  font-size: 14px;
  line-height: 1.75;
}

.rag-chunks__item.is-expanded .rag-chunks__text {
  max-height: 520px;
}

.rag-chunks__item-footer {
  cursor: pointer;
  border-top: 1px solid hsl(var(--border));
  padding: 8px 14px;
  color: hsl(var(--primary));
  font-size: 13px;
  text-align: center;
}

.rag-chunks__pagination {
  display: flex;
  flex: 0 0 auto;
  justify-content: flex-end;
  padding-top: 14px;
}

@media (max-width: 640px) {
  .rag-chunks__toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .rag-chunks__toolbar :deep(.ant-input-affix-wrapper),
  .rag-chunks__toolbar :deep(.ant-btn) {
    width: 100%;
  }
}
</style>
