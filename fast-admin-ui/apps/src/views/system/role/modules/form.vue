<script lang="ts" setup>
import type { DataNode } from 'ant-design-vue/es/tree';

import type { Recordable } from '@vben/types';

import type { SystemRoleApi } from '#/api/system/role';

import { computed, nextTick, ref } from 'vue';

import { Tree, useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Spin } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { getDeptList } from '#/api/system/dept';
import { getMenuList } from '#/api/system/menu';
import { createRole, getRoleById, updateRole } from '#/api/system/role';

import { useFormSchema } from '../data';

const emits = defineEmits(['success']);

const formData = ref<SystemRoleApi.SystemRole>();

const [Form, formApi] = useVbenForm({
  schema: useFormSchema(),
  showDefaultActions: false,
});

const permissions = ref<DataNode[]>([]);
const loadingPermissions = ref(false);
const loadingRole = ref(false);

const depts = ref<DataNode[]>([]);
const loadingDepts = ref(false);

const id = ref();
const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    modalApi.lock();
    (id.value ? updateRole({ id: id.value, ...values }) : createRole(values))
      .then(() => {
        emits('success');
        modalApi.close();
      })
      .catch(() => {
        modalApi.unlock();
      });
  },

  async onOpenChange(isOpen) {
    if (isOpen) {
      const data = modalApi.getData<SystemRoleApi.SystemRole>();
      formApi.resetForm();
      formData.value = data;

      if (data?.id) {
        id.value = data.id;
      } else {
        id.value = undefined;
      }

      const permissionTask =
        permissions.value.length === 0 ? loadPermissions() : Promise.resolve();
      const deptTask = depts.value.length === 0 ? loadDepts() : Promise.resolve();
      const roleTask = id.value ? loadRoleDetail(id.value) : Promise.resolve(data);

      const [, , roleDetail] = await Promise.all([permissionTask, deptTask, roleTask]);
      // Wait for Vue to flush DOM updates (form fields mounted)
      await nextTick();
      if (roleDetail) {
        formData.value = roleDetail;
        formApi.setValues(roleDetail);
      }
    }
  },
});

async function loadRoleDetail(roleId: string) {
  loadingRole.value = true;
  try {
    return await getRoleById(roleId);
  } finally {
    loadingRole.value = false;
  }
}

async function loadPermissions() {
  loadingPermissions.value = true;
  try {
    const res = await getMenuList();
    permissions.value = res as unknown as DataNode[];
  } finally {
    loadingPermissions.value = false;
  }
}

async function loadDepts() {
  loadingDepts.value = true;
  try {
    const res = await getDeptList();
    depts.value = res as unknown as DataNode[];
  } finally {
    loadingDepts.value = false;
  }
}

const getModalTitle = computed(() => {
  return formData.value?.id ? '编辑角色' : '新增角色';
});

function getNodeClass(node: Recordable<any>) {
  const classes: string[] = [];
  if (node.value?.type === 'button') {
    classes.push('inline-flex');
  }

  return classes.join(' ');
}
</script>
<template>
  <Modal :title="getModalTitle">
    <Spin :spinning="loadingRole" wrapper-class-name="w-full">
      <Form>
        <template #deptIds="slotProps">
          <Spin :spinning="loadingDepts" wrapper-class-name="w-full">
            <Tree
              :tree-data="depts"
              multiple
              bordered
              :default-expanded-level="2"
              v-bind="slotProps"
              value-field="id"
              label-field="name"
              :propagate-select="false"
              :bubble-select="false"
            />
          </Spin>
        </template>

        <template #permissions="slotProps">
          <Spin :spinning="loadingPermissions" wrapper-class-name="w-full">
            <Tree
              :tree-data="permissions"
              multiple
              bordered
              :default-expanded-level="2"
              :get-node-class="getNodeClass"
              v-bind="slotProps"
              value-field="id"
              label-field="meta.title"
              icon-field="meta.icon"
              :propagate-select="false"
              :bubble-select="false"
            >
              <template #node="{ value }">
                <IconifyIcon v-if="value.meta.icon" :icon="value.meta.icon" />
                {{ value.meta.title }}
              </template>
            </Tree>
          </Spin>
        </template>
      </Form>
    </Spin>
  </Modal>
</template>
<style lang="css" scoped>
:deep(.ant-tree-title) {
  .tree-actions {
    display: none;
    margin-left: 20px;
  }
}

:deep(.ant-tree-title:hover) {
  .tree-actions {
    display: flex;
    flex: auto;
    justify-content: flex-end;
    margin-left: 20px;
  }
}
</style>
