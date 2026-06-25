<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { AiMcpApi } from '#/api/ai/mcp';

import { ref } from 'vue';

import { AccessControl } from '@vben/access';
import { Page, useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import {
  Alert,
  Button,
  Descriptions,
  DescriptionsItem,
  Drawer,
  Empty,
  List,
  ListItem,
  message,
  Modal as AModal,
  Spin,
  Tabs,
  TabPane,
  Tag,
} from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteAiMcpServer,
  getAiMcpServerDetail,
  getAiMcpServerPage,
  inspectAiMcpServer,
  reloadAiMcpServer,
} from '#/api/ai/mcp';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const inspectOpen = ref(false);
const inspectLoading = ref(false);
const inspectDetail = ref<AiMcpApi.McpInspect>();

function onActionClick({ code, row }: OnActionClickParams<AiMcpApi.McpServer>) {
  if (code === 'edit') {
    formModalApi.setData(row).open();
  } else if (code === 'detail') {
    openInspect(row);
  } else if (code === 'reload') {
    reloadMcpServer(row);
  } else if (code === 'delete') {
    AModal.confirm({
      content: `确认删除 MCP 服务「${row.name}」？`,
      title: '删除确认',
      onOk: () =>
        deleteAiMcpServer(row.id).then(() => {
          message.success('删除成功');
          refreshGrid();
        }),
    });
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnEnter: true,
    collapsed: true,
  },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) =>
          getAiMcpServerPage({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<AiMcpApi.McpServer>,
});

function refreshGrid() {
  gridApi.query();
}

async function openInspect(row: AiMcpApi.McpServer) {
  inspectOpen.value = true;
  inspectLoading.value = true;
  inspectDetail.value = undefined;
  try {
    inspectDetail.value = await inspectAiMcpServer(row.id);
  } finally {
    inspectLoading.value = false;
  }
}

async function reloadMcpServer(row: AiMcpApi.McpServer) {
  if (row.refreshing) {
    return;
  }
  const messageKey = `mcp-reload-${row.id}`;
  row.refreshing = true;
  message.loading({
    content: `正在刷新「${row.name}」连接...`,
    duration: 0,
    key: messageKey,
  });
  try {
    await reloadAiMcpServer(row.id);
    const detail = await getAiMcpServerDetail(row.id);
    if (detail?.connected) {
      message.success({
        content: `「${row.name}」连接成功，工具数 ${detail.toolCount ?? 0}`,
        key: messageKey,
      });
    } else {
      message.warning({
        content: `「${row.name}」未连接：${detail?.statusMessage || '请检查配置'}`,
        duration: 5,
        key: messageKey,
      });
    }
    refreshGrid();
    if (inspectOpen.value && inspectDetail.value?.server.id === row.id) {
      inspectDetail.value = await inspectAiMcpServer(row.id);
    }
  } catch (error) {
    message.error({
      content: `「${row.name}」刷新失败`,
      key: messageKey,
    });
  } finally {
    row.refreshing = false;
  }
}

function stringify(value: unknown) {
  if (!value || Object.keys(value as Record<string, unknown>).length === 0) {
    return '-';
  }
  return JSON.stringify(value, null, 2);
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="refreshGrid" />
    <Grid table-title="MCP 服务管理">
      <template #toolbar-tools>
        <AccessControl type="code" :codes="['ai:mcp:add']">
          <Button type="primary" @click="formModalApi.setData(null).open()">
            <Plus class="size-5" />
            新增服务
          </Button>
        </AccessControl>
      </template>
    </Grid>

    <Drawer
      v-model:open="inspectOpen"
      title="MCP 服务详情"
      :width="760"
      placement="right"
    >
      <Spin :spinning="inspectLoading">
        <div v-if="inspectDetail" class="mcp-inspect">
          <div class="mcp-inspect-header">
            <div>
              <div class="mcp-inspect-title">{{ inspectDetail.server.name }}</div>
              <div class="mcp-inspect-subtitle">
                {{ inspectDetail.server.transport }}
                <span v-if="inspectDetail.server.url"> · {{ inspectDetail.server.url }}</span>
                <span v-else-if="inspectDetail.server.command"> · {{ inspectDetail.server.command }}</span>
              </div>
            </div>
            <Tag :color="inspectDetail.runtime.connected ? 'success' : 'error'">
              {{ inspectDetail.runtime.connected ? '已连接' : '未连接' }}
            </Tag>
          </div>

          <Alert
            v-if="!inspectDetail.runtime.connected"
            class="mb-3"
            type="warning"
            show-icon
            :message="inspectDetail.runtime.statusMessage || '未连接'"
          />

          <div class="mcp-inspect-metrics">
            <div>
              <span>Tools</span>
              <strong>{{ inspectDetail.runtime.toolCount || 0 }}</strong>
            </div>
            <div>
              <span>Prompts</span>
              <strong>{{ inspectDetail.runtime.promptCount || 0 }}</strong>
            </div>
            <div>
              <span>Resources</span>
              <strong>{{ inspectDetail.runtime.resourceCount || 0 }}</strong>
            </div>
            <div>
              <span>上下文</span>
              <strong>{{ inspectDetail.runtime.contextTokenCount || 0 }}</strong>
              <em>token</em>
            </div>
          </div>

          <Tabs>
            <TabPane key="tools" tab="工具">
              <Empty v-if="inspectDetail.tools.length === 0" description="暂无工具" />
              <List v-else item-layout="vertical" :data-source="inspectDetail.tools">
                <template #renderItem="{ item }">
                  <ListItem>
                    <div class="mcp-item-title">{{ item.title || item.name }}</div>
                    <div class="mcp-item-name">{{ item.name }}</div>
                    <p class="mcp-item-desc">{{ item.description || '无描述' }}</p>
                    <details>
                      <summary>Schema</summary>
                      <pre class="mcp-pre">{{ stringify(item.inputSchema) }}</pre>
                    </details>
                  </ListItem>
                </template>
              </List>
            </TabPane>

            <TabPane key="prompts" tab="提示词">
              <Empty v-if="inspectDetail.prompts.length === 0" description="暂无提示词" />
              <List v-else item-layout="vertical" :data-source="inspectDetail.prompts">
                <template #renderItem="{ item }">
                  <ListItem>
                    <div class="mcp-item-title">{{ item.title || item.name }}</div>
                    <div class="mcp-item-name">{{ item.name }}</div>
                    <p class="mcp-item-desc">{{ item.description || '无描述' }}</p>
                    <div v-if="item.arguments?.length" class="mcp-args">
                      <Tag v-for="arg in item.arguments" :key="arg.name" :color="arg.required ? 'blue' : 'default'">
                        {{ arg.name }}{{ arg.required ? ' *' : '' }}
                      </Tag>
                    </div>
                  </ListItem>
                </template>
              </List>
            </TabPane>

            <TabPane key="resources" tab="资源">
              <Empty v-if="inspectDetail.resources.length === 0" description="暂无资源" />
              <List v-else item-layout="vertical" :data-source="inspectDetail.resources">
                <template #renderItem="{ item }">
                  <ListItem>
                    <div class="mcp-item-title">{{ item.title || item.name || item.uri }}</div>
                    <div class="mcp-item-name">{{ item.uri }}</div>
                    <p class="mcp-item-desc">{{ item.description || '无描述' }}</p>
                    <Tag v-if="item.mimeType">{{ item.mimeType }}</Tag>
                    <Tag v-if="item.size">{{ item.size }} bytes</Tag>
                  </ListItem>
                </template>
              </List>
            </TabPane>

            <TabPane key="runtime" tab="运行信息">
              <Descriptions :column="1" bordered size="small">
                <DescriptionsItem label="状态说明">
                  {{ inspectDetail.runtime.statusMessage || '-' }}
                </DescriptionsItem>
                <DescriptionsItem label="服务信息">
                  <pre class="mcp-pre">{{ stringify(inspectDetail.runtime.serverInfo) }}</pre>
                </DescriptionsItem>
                <DescriptionsItem label="能力">
                  <pre class="mcp-pre">{{ stringify(inspectDetail.runtime.capabilities) }}</pre>
                </DescriptionsItem>
                <DescriptionsItem label="Instructions">
                  <pre class="mcp-pre">{{ inspectDetail.runtime.instructions || '-' }}</pre>
                </DescriptionsItem>
              </Descriptions>
            </TabPane>
          </Tabs>
        </div>
      </Spin>
    </Drawer>
  </Page>
</template>

<style scoped>
.mcp-inspect {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mcp-inspect-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.mcp-inspect-title {
  font-size: 18px;
  font-weight: 600;
}

.mcp-inspect-subtitle,
.mcp-item-name,
.mcp-item-desc {
  color: #8c8c8c;
}

.mcp-inspect-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.mcp-inspect-metrics > div {
  min-height: 64px;
  padding: 10px 12px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  background: #fafafa;
}

.mcp-inspect-metrics span,
.mcp-inspect-metrics em {
  display: block;
  color: #8c8c8c;
  font-size: 12px;
  font-style: normal;
}

.mcp-inspect-metrics strong {
  display: inline-block;
  margin-top: 4px;
  font-size: 20px;
}

.mcp-item-title {
  font-weight: 600;
}

.mcp-args {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.mcp-pre {
  margin: 6px 0 0;
  padding: 8px;
  max-height: 260px;
  overflow: auto;
  background: #fafafa;
  border-radius: 4px;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
