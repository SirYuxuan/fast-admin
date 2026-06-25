<script lang="ts" setup>
import type { SysConfigApi } from '#/api/system/config';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { createConfig, updateConfig } from '#/api/system/config';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<SysConfigApi.Config>();
const getTitle = computed(() =>
  formData.value?.id ? '编辑参数' : '新增参数',
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    labelWidth: 100,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  class: 'w-[560px]',
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    try {
      const data = await formApi.getValues();
      if (formData.value?.id) {
        await updateConfig({ ...data, id: formData.value.id });
      } else {
        await createConfig(data);
      }
      modalApi.close();
      emit('success');
    } finally {
      modalApi.lock(false);
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = modalApi.getData<SysConfigApi.Config>();
      formData.value = data ?? undefined;
      formApi.resetForm();
      if (data) {
        formApi.setValues(data);
        const isBuiltin = data.configType === 1;
        const locked = { componentProps: { disabled: isBuiltin } };
        formApi.updateSchema([
          { fieldName: 'configName', ...locked },
          { fieldName: 'configKey', ...locked },
          { fieldName: 'configType', ...locked },
        ]);
      }
    }
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
