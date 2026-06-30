<script lang="ts" setup>
import type { FlowModelApi } from '#/api/flow/model';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { createModel, updateModel } from '#/api/flow/model';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<FlowModelApi.Model>();
const getTitle = computed(() =>
  formData.value?.id ? '编辑流程模型' : '新增流程模型',
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    labelWidth: 90,
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
        await updateModel({ ...data, id: formData.value.id });
      } else {
        await createModel(data);
      }
      modalApi.close();
      emit('success');
    } finally {
      modalApi.lock(false);
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = modalApi.getData<FlowModelApi.Model>();
      formData.value = data ?? undefined;
      formApi.resetForm();
      if (data) {
        formApi.setValues(data);
        // 已存在的模型不允许改流程标识，避免与已部署定义脱节
        formApi.updateSchema([
          { fieldName: 'modelKey', componentProps: { disabled: true } },
        ]);
      } else {
        formApi.updateSchema([
          { fieldName: 'modelKey', componentProps: { disabled: false } },
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
