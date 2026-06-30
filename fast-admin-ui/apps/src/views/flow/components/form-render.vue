<script lang="ts" setup>
import type { VbenFormSchema } from '#/adapter/form';
import type { FlowFormApi } from '#/api/flow/form';

import { onMounted } from 'vue';

import { useVbenForm } from '#/adapter/form';

const props = withDefaults(
  defineProps<{
    disabled?: boolean;
    schema: FlowFormApi.Field[];
    values?: Record<string, any>;
  }>(),
  { disabled: false, values: () => ({}) },
);

function toSchema(fields: FlowFormApi.Field[]): VbenFormSchema[] {
  return (fields || []).map((f) => ({
    component: f.component as any,
    fieldName: f.fieldName,
    label: f.label,
    rules: f.required ? 'required' : undefined,
    componentProps: {
      class: 'w-full',
      disabled: props.disabled,
      ...(f.options ? { options: f.options } : {}),
      ...(f.componentProps ?? {}),
    },
  }));
}

const [Form, formApi] = useVbenForm({
  commonConfig: { labelWidth: 100 },
  layout: 'horizontal',
  schema: toSchema(props.schema),
  showDefaultActions: false,
});

onMounted(() => {
  if (props.values && Object.keys(props.values).length > 0) {
    formApi.setValues(props.values);
  }
});

defineExpose({
  async getValidatedValues() {
    const { valid } = await formApi.validate();
    if (!valid) return null;
    return await formApi.getValues();
  },
  getValues: () => formApi.getValues(),
});
</script>

<template>
  <Form />
</template>
