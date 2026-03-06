<script lang="ts" setup>
import type { ModelType } from './data-types';

import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';

import { onMounted, ref } from 'vue';

import { ColPage, Tree, useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { Button, message } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { crudHelper, deleteRole } from '#/api';

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
        <Button type="primary" @click="onCreate">
          <Plus class="size-5" />
          新增用户
        </Button>
      </template>
    </Grid>
  </ColPage>
</template>
