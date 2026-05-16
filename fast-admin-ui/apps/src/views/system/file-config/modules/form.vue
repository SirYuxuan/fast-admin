<script lang="ts" setup>
import type { SystemFileApi } from '#/api/system/file';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Button } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { createFileConfig, updateFileConfig } from '#/api/system/file';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<SystemFileApi.FileConfig>();
const getTitle = computed(() =>
  formData.value?.id ? '编辑文件存储配置' : '新增文件存储配置',
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    labelWidth: 110,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

function resetForm() {
  formApi.resetForm();
  formApi.setValues(formData.value || {});
}

/**
 * 把扁平的 config.xxx 还原为嵌套对象交给后端：
 *   { type:'OSS', 'config.endpoint':'...', 'config.bucket':'...' }
 *   →  { type:'OSS', config: { endpoint:'...', bucket:'...' } }
 */
function normalize(data: Record<string, any>) {
  const out: Record<string, any> = { config: {} };
  for (const [k, v] of Object.entries(data)) {
    if (k.startsWith('config.')) {
      out.config[k.slice(7)] = v;
    } else {
      out[k] = v;
    }
  }
  return out;
}

/**
 * 把 { type, config: {...} } 摊平成表单状态
 */
function flatten(data: SystemFileApi.FileConfig) {
  const out: Record<string, any> = { ...data };
  delete out.config;
  for (const [k, v] of Object.entries(data.config || {})) {
    out[`config.${k}`] = v;
  }
  return out;
}

const [Modal, modalApi] = useVbenModal({
  class: 'w-[560px] top-[6vh] max-h-[88%]',
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (valid) {
      modalApi.lock();
      const raw = await formApi.getValues();
      const data = normalize(raw);
      try {
        if (formData.value?.id) {
          await updateFileConfig({ ...data, id: formData.value.id });
        } else {
          await createFileConfig(data);
        }
        modalApi.close();
        emit('success');
      } finally {
        modalApi.lock(false);
      }
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = modalApi.getData<SystemFileApi.FileConfig>();
      formData.value = data ?? undefined;
      formApi.resetForm();
      if (data) {
        formApi.setValues(flatten(data));
      }
    }
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form class="mx-4" />
    <template #prepend-footer>
      <div class="flex-auto">
        <Button type="primary" danger @click="resetForm">重置</Button>
      </div>
    </template>
  </Modal>
</template>
