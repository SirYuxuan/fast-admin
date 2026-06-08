<script lang="ts" setup>
import type { ModelType } from './data-types';

import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';

import { computed, h, onMounted, ref } from 'vue';

import { ColPage, Tree, useVbenModal } from '@vben/common-ui';
import { IconifyIcon, Plus } from '@vben/icons';

import {
  Button,
  Input,
  message,
  Modal as AModal,
  Upload as AUpload,
} from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  crudHelper,
  del as deleteUser,
  downloadUserImportTemplate,
  exportUserExcel,
  importUserExcel,
} from '#/api';

import { crud, useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

type ImportError = { column: string; message: string; rowIndex: number };

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    fieldMappingTime: [['createTime', ['startTime', 'endTime']]],
    schema: useGridFormSchema(),
    submitOnChange: false,
    submitOnEnter: true,
    collapsed: true,
  },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await crud.list({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },

    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<ModelType>,
});

function onActionClick(e: OnActionClickParams<ModelType>) {
  switch (e.code) {
    case 'delete': {
      onDelete(e.row);
      break;
    }
    case 'edit': {
      onEdit(e.row);
      break;
    }
  }
}

function onEdit(row: ModelType) {
  formModalApi.setData(row).open();
}

function onDelete(row: ModelType) {
  const hideLoading = message.loading({
    content: `正在删除 ${row.nickname}`,
    duration: 0,
    key: 'action_process_msg',
  });
  deleteUser(row.id)
    .then(() => {
      message.success({
        content: `${row.nickname} 删除成功`,
        key: 'action_process_msg',
      });
      onRefresh();
    })
    .catch(() => {
      hideLoading();
    });
}

function onRefresh() {
  gridApi.query();
}

function onCreate() {
  formModalApi.setData({}).open();
}

// ---------- 导出 / 模板下载 / 导入 ----------
function saveBlob(blob: Blob, fileName: string) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = fileName;
  document.body.append(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

function escapeHtml(value?: number | string) {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;');
}

function downloadImportErrorsExcel(errors: ImportError[]) {
  const rows = errors
    .map(
      (error) => `
        <tr>
          <td>${escapeHtml(error.rowIndex)}</td>
          <td>${escapeHtml(error.column)}</td>
          <td>${escapeHtml(error.message)}</td>
        </tr>
      `,
    )
    .join('');
  const html = `
    <html>
      <head><meta charset="UTF-8" /></head>
      <body>
        <table border="1">
          <thead>
            <tr>
              <th>行号</th>
              <th>字段</th>
              <th>失败原因</th>
            </tr>
          </thead>
          <tbody>${rows}</tbody>
        </table>
      </body>
    </html>
  `;
  const blob = new Blob(['\uFEFF', html], {
    type: 'application/vnd.ms-excel;charset=utf-8',
  });
  saveBlob(blob, `用户导入失败明细_${Date.now()}.xls`);
}

async function onExport() {
  const hideLoading = message.loading({
    content: '正在导出用户列表...',
    duration: 0,
    key: 'download_process_msg',
  });
  try {
    exporting.value = true;
    const blob = (await exportUserExcel()) as unknown as Blob;
    saveBlob(blob, `用户列表_${Date.now()}.xlsx`);
    message.success({
      content: '用户列表导出成功',
      key: 'download_process_msg',
    });
  } catch {
    hideLoading();
  } finally {
    exporting.value = false;
  }
}

async function onDownloadTemplate() {
  const hideLoading = message.loading({
    content: '正在下载导入模板...',
    duration: 0,
    key: 'download_process_msg',
  });
  try {
    templateDownloading.value = true;
    const blob = (await downloadUserImportTemplate()) as unknown as Blob;
    saveBlob(blob, '用户导入模板.xlsx');
    message.success({
      content: '导入模板下载成功',
      key: 'download_process_msg',
    });
  } catch {
    hideLoading();
  } finally {
    templateDownloading.value = false;
  }
}

const importing = ref(false);
const exporting = ref(false);
const templateDownloading = ref(false);
const beforeImport = async (file: File) => {
  try {
    importing.value = true;
    const res = await importUserExcel(file);
    const lines: string[] = [
      `总行数：${res.totalRows}`,
      `成功新增：${res.addedCount}`,
      `失败：${res.errorCount}`,
    ];
    if (res.errors?.length) {
      lines.push('', '错误详情：');
      res.errors.slice(0, 10).forEach((e) => {
        lines.push(`第 ${e.rowIndex} 行 [${e.column}] ${e.message}`);
      });
      if (res.errors.length > 10) {
        lines.push(`...还有 ${res.errors.length - 10} 条错误`);
      }
    }
    const modalOptions = {
      title: '导入完成',
      width: 520,
      content: () =>
        h(
          'pre',
          { style: 'white-space:pre-wrap;font-size:12px;margin:0' },
          lines.join('\n'),
        ),
    };
    if (res.errorCount > 0) {
      AModal.confirm({
        ...modalOptions,
        cancelText: '关闭',
        okText: '下载失败 Excel',
        onOk: () => downloadImportErrorsExcel(res.errors || []),
      });
    } else {
      AModal.success({
        ...modalOptions,
        okText: '知道了',
      });
    }
    onRefresh();
  } catch {
    /* 全局拦截器已提示 */
  } finally {
    importing.value = false;
  }
  return false; // 阻止 antdv Upload 自身请求
};
const treeData = ref<any[]>([]);
const deptKeyword = ref('');
const selectedDeptId = ref<string>();
const deptTreeKey = ref(0);
const deptExpandedLevel = ref(2);

const filteredTreeData = computed(() => {
  const keyword = deptKeyword.value.trim().toLowerCase();
  if (!keyword) return treeData.value;
  return filterDeptTree(treeData.value, keyword);
});

onMounted(async () => {
  try {
    const res = await crudHelper.get<any[]>('system/dept');
    treeData.value = res || [];
  } catch (error) {
    // ignore or optionally log
    console.warn('加载部门数据失败', error);
    treeData.value = [];
  }
});

function onTreeSelect(item: any) {
  gridApi.query({ deptId: item?.value?.id || null });
}

function onAllDeptSelect() {
  selectedDeptId.value = undefined;
  gridApi.query({ deptId: null });
}

function onExpandDeptTree() {
  deptExpandedLevel.value = 99;
  deptTreeKey.value += 1;
}

function onCollapseDeptTree() {
  deptExpandedLevel.value = 0;
  deptTreeKey.value += 1;
}

function filterDeptTree(nodes: any[], keyword: string): any[] {
  return nodes
    .map((node) => {
      const children = filterDeptTree(node.children || [], keyword);
      const matched = String(node.name || '')
        .toLowerCase()
        .includes(keyword);
      if (!matched && children.length === 0) return null;
      return { ...node, children };
    })
    .filter(Boolean);
}

function getDeptNodeClass(item: any) {
  return item?.value?.id === selectedDeptId.value
    ? 'dept-tree-node dept-tree-node-active'
    : 'dept-tree-node';
}
</script>
<template>
  <ColPage auto-content-height :left-max-width="15">
    <FormModal @success="onRefresh" />
    <template #left="{ isCollapsed, expand }">
      <div v-if="isCollapsed" @click="expand">
        <Tooltip title="点击展开左侧">
          <Button
            shape="circle"
            type="primary"
            class="flex items-center justify-center"
          >
            <template #icon>
              <IconifyIcon class="text-2xl" icon="bi:arrow-right" />
            </template>
          </Button>
        </Tooltip>
      </div>
      <div
        v-else
        :style="{ minWidth: '200px' }"
        class="dept-filter-panel mr-2 h-full"
      >
        <div class="dept-filter-header">
          <div>
            <div class="dept-filter-title">部门筛选</div>
          </div>
          <div class="dept-filter-actions">
            <Button
              class="dept-filter-action"
              size="small"
              title="展开全部"
              type="text"
              @click="onExpandDeptTree"
            >
              <IconifyIcon icon="lucide:unfold-vertical" />
            </Button>
            <Button
              class="dept-filter-action"
              size="small"
              title="收起全部"
              type="text"
              @click="onCollapseDeptTree"
            >
              <IconifyIcon icon="lucide:fold-vertical" />
            </Button>
            <Button size="small" type="text" @click="onAllDeptSelect">
              全部
            </Button>
          </div>
        </div>
        <Input
          v-model:value="deptKeyword"
          allow-clear
          class="dept-filter-search"
          placeholder="搜索部门"
        >
          <template #prefix>
            <IconifyIcon icon="lucide:search" class="text-muted-foreground" />
          </template>
        </Input>
        <Tree
          :key="deptTreeKey"
          v-model="selectedDeptId"
          :tree-data="filteredTreeData"
          :default-expanded-level="deptExpandedLevel"
          :get-node-class="getDeptNodeClass"
          value-field="id"
          label-field="name"
          @select="onTreeSelect"
        >
          <template #node="{ value }">
            <div class="dept-tree-label">
              <IconifyIcon icon="lucide:building-2" class="dept-tree-icon" />
              <span class="dept-tree-text">{{ value.name }}</span>
            </div>
          </template>
        </Tree>
      </div>
    </template>
    <Grid table-title="用户列表">
      <template #toolbar-tools>
        <div class="flex items-center gap-2">
          <Button :loading="templateDownloading" @click="onDownloadTemplate">
            <IconifyIcon icon="lucide:file-down" class="text-base" />
            模板
          </Button>
          <AUpload
            :before-upload="beforeImport"
            :show-upload-list="false"
            accept=".xlsx,.xls"
            :disabled="importing"
          >
            <Button :loading="importing">
              <IconifyIcon icon="lucide:upload" class="text-base" />
              导入
            </Button>
          </AUpload>
          <Button :loading="exporting" @click="onExport">
            <IconifyIcon icon="lucide:download" class="text-base" />
            导出
          </Button>
          <Button type="primary" @click="onCreate">
            <Plus class="size-5" />
            新增用户
          </Button>
        </div>
      </template>
    </Grid>
  </ColPage>
</template>

<style scoped>
.dept-filter-panel {
  display: flex;
  flex-direction: column;
  min-width: 220px;
  padding: 12px;
  overflow: hidden;
  background: hsl(var(--card));
  border: 1px solid hsl(var(--border));
  border-radius: var(--radius);
}

.dept-filter-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.dept-filter-title {
  font-size: 14px;
  font-weight: 600;
  line-height: 20px;
}

.dept-filter-actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 2px;
}

.dept-filter-action {
  width: 28px;
  padding: 0;
}

.dept-filter-search {
  margin-bottom: 10px;
}

.dept-filter-panel :deep(.container) {
  flex: 1;
  min-height: 0;
  padding: 2px;
  overflow: auto;
}

.dept-filter-panel :deep(.container > div:first-of-type) {
  display: none;
}

.dept-filter-panel :deep(.tree-node) {
  min-height: 32px;
  padding: 6px 8px;
  border-radius: 6px;
  transition:
    background-color 0.16s ease,
    color 0.16s ease;
}

.dept-filter-panel :deep(.tree-node:hover) {
  background: hsl(var(--accent));
}

.dept-filter-panel :deep(.dept-tree-node-active) {
  color: hsl(var(--primary));
  background: hsl(var(--primary) / 0.1);
}

.dept-tree-label {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 8px;
}

.dept-tree-icon {
  flex-shrink: 0;
  width: 15px;
  height: 15px;
  color: hsl(var(--muted-foreground));
}

.dept-tree-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
