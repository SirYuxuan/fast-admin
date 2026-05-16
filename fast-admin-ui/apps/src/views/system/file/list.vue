<script lang="ts" setup>
import type { UploadProps } from 'ant-design-vue';

import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemFileApi } from '#/api/system/file';

import { ref } from 'vue';

import { AccessControl } from '@vben/access';
import { Page } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import {
  Button,
  Input,
  message,
  Modal as AModal,
  Upload as AUpload,
} from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteFile,
  getFilePage,
  uploadFile,
} from '#/api/system/file';

import { useColumns, useGridFormSchema } from './data';

const bizType = ref('');
const bizId = ref('');

function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemFileApi.FileRecord>) {
  switch (code) {
    case 'delete': {
      onDelete(row);
      break;
    }
    case 'download': {
      onDownload(row);
      break;
    }
    case 'preview': {
      onPreview(row);
      break;
    }
  }
}

function onPreview(row: SystemFileApi.FileRecord) {
  window.open(row.url, '_blank');
}

function onDownload(row: SystemFileApi.FileRecord) {
  // 直接打开后端下载流（带 Content-Disposition）
  const a = document.createElement('a');
  a.href = `/api/system/file/${row.id}/download`;
  a.download = row.originalName;
  document.body.append(a);
  a.click();
  a.remove();
}

function onDelete(row: SystemFileApi.FileRecord) {
  AModal.confirm({
    content: `确认删除文件「${row.originalName}」？`,
    title: '删除确认',
    onOk: () =>
      deleteFile(row.id).then(() => {
        message.success('删除成功');
        refreshGrid();
      }),
  });
}

const beforeUpload: UploadProps['beforeUpload'] = async (file) => {
  try {
    await uploadFile(file as File, bizType.value || undefined, bizId.value || undefined);
    message.success('上传成功');
    refreshGrid();
  } catch {
    /* 错误由全局拦截器提示 */
  }
  // 阻止 antdv Upload 自身的请求
  return false;
};

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
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
          return await getFilePage({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
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
  } as VxeTableGridOptions<SystemFileApi.FileRecord>,
});

function refreshGrid() {
  gridApi.query();
}
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="文件列表">
      <template #toolbar-tools>
        <div class="flex items-center gap-2">
          <Input
            v-model:value="bizType"
            placeholder="业务类型（可选）"
            allow-clear
            class="w-40"
          />
          <Input
            v-model:value="bizId"
            placeholder="业务ID（可选）"
            allow-clear
            class="w-40"
          />
          <AccessControl :codes="['system:file:upload']">
            <AUpload :before-upload="beforeUpload" :show-upload-list="false">
              <Button type="primary">
                <Plus class="size-5" />
                上传
              </Button>
            </AUpload>
          </AccessControl>
        </div>
      </template>
    </Grid>
  </Page>
</template>
