<script lang="ts" setup>
import type { SysJobApi } from '#/api/system/job';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { createJob, updateJob } from '#/api/system/job';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<SysJobApi.Job>();
const getTitle = computed(() =>
  formData.value?.id ? '编辑定时任务' : '新增定时任务',
);

const [Form, formApi] = useVbenForm({
  commonConfig: { componentProps: { class: 'w-full' }, labelWidth: 100 },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  class: 'w-[640px]',
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    try {
      const data = await formApi.getValues();
      if (formData.value?.id) {
        await updateJob({ ...data, id: formData.value.id });
      } else {
        await createJob(data);
      }
      modalApi.close();
      emit('success');
    } finally {
      modalApi.lock(false);
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = modalApi.getData<SysJobApi.Job>();
      formData.value = data ?? undefined;
      formApi.resetForm();
      if (data) formApi.setValues(data);
    }
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
