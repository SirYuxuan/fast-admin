<script lang="ts" setup>
import type { ModelType } from './data-types';

import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';

import { h, onMounted, ref } from 'vue';

import { ColPage, Tree, useVbenModal } from '@vben/common-ui';
import { IconifyIcon, Plus } from '@vben/icons';

import {
  Button,
  message,
  Modal as AModal,
  Upload as AUpload,
} from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  crudHelper,
  deleteRole,
  downloadUserImportTemplate,
  exportUserExcel,
  importUserExcel,
} from '#/api';

import { crud, useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

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
  deleteRole(row.id)
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

async function onExport() {
  try {
    const blob = (await exportUserExcel()) as unknown as Blob;
    saveBlob(blob, `用户列表_${Date.now()}.xlsx`);
  } catch {
    /* 全局拦截器已提示 */
  }
}

async function onDownloadTemplate() {
  try {
    const blob = (await downloadUserImportTemplate()) as unknown as Blob;
    saveBlob(blob, '用户导入模板.xlsx');
  } catch {
    /* 全局拦截器已提示 */
  }
}

const importing = ref(false);
const beforeImport = async (file: File) => {
  try {
    importing.value = true;
    const res = await importUserExcel(file);
    const lines: string[] = [
      `总行数：${res.totalRows}`,
      `成功：${res.successCount}`,
      `失败：${res.errorCount}`,
      `实际新增：${res.addedCount}`,
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
    AModal[res.errorCount > 0 ? 'warning' : 'success']({
      title: '导入完成',
      width: 520,
      content: () =>
        h(
          'pre',
          { style: 'white-space:pre-wrap;font-size:12px;margin:0' },
          lines.join('\n'),
        ),
    });
    onRefresh();
  } catch {
    /* 全局拦截器已提示 */
  } finally {
    importing.value = false;
  }
  return false; // 阻止 antdv Upload 自身请求
};
const treeData = ref<any[]>([]);

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
        class="border-border bg-card mr-2 h-full rounded-[var(--radius)] border p-2"
      >
        <Tree
          :tree-data="treeData"
          :default-expanded-level="2"
          value-field="id"
          label-field="name"
          @select="onTreeSelect"
        />
      </div>
    </template>
    <Grid table-title="用户列表">
      <template #toolbar-tools>
        <div class="flex items-center gap-2">
          <Button @click="onDownloadTemplate">
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
          <Button @click="onExport">
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
