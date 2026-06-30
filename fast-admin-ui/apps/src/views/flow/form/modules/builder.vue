<script lang="ts" setup>
import type { FlowFormApi } from '#/api/flow/form';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import {
  Button,
  Checkbox,
  Input,
  message,
  Select,
  Textarea,
} from 'ant-design-vue';

import { createForm, updateForm } from '#/api/flow/form';

import FormRender from '../../components/form-render.vue';

interface FieldRow {
  label: string;
  fieldName: string;
  component: string;
  required: boolean;
  optionsText?: string;
}

const emit = defineEmits(['success']);

const editingId = ref<string>();
const meta = ref({ name: '', formKey: '', remark: '' });
const fields = ref<FieldRow[]>([]);

const componentOptions = [
  { label: '单行文本', value: 'Input' },
  { label: '多行文本', value: 'Textarea' },
  { label: '数字', value: 'InputNumber' },
  { label: '下拉选择', value: 'Select' },
  { label: '单选', value: 'RadioGroup' },
  { label: '日期', value: 'DatePicker' },
];

const needOptions = (c: string) => c === 'Select' || c === 'RadioGroup';

function addField() {
  fields.value.push({
    label: '',
    fieldName: '',
    component: 'Input',
    required: false,
  });
}

function removeField(idx: number) {
  fields.value.splice(idx, 1);
}

/** 把字段行转成 form-render 可用的 schema */
function toSchema(rows: FieldRow[]): FlowFormApi.Field[] {
  return rows
    .filter((f) => f.fieldName && f.label)
    .map((f) => ({
      component: f.component,
      fieldName: f.fieldName,
      label: f.label,
      required: f.required,
      options: needOptions(f.component) ? parseOptions(f.optionsText) : undefined,
    }));
}

function parseOptions(text?: string) {
  return (text ?? '')
    .split('\n')
    .map((l) => l.trim())
    .filter(Boolean)
    .map((l) => {
      const [label, value] = l.split(':');
      return { label: label?.trim(), value: (value ?? label)?.trim() };
    });
}

const previewSchema = computed(() => toSchema(fields.value));

const [Modal, modalApi] = useVbenModal({
  class: 'w-[900px]',
  async onConfirm() {
    if (!meta.value.name || !meta.value.formKey) {
      message.warning('请填写表单名称与标识');
      return;
    }
    const schema = toSchema(fields.value);
    modalApi.lock();
    try {
      const payload = {
        ...meta.value,
        content: JSON.stringify(schema),
      };
      if (editingId.value) {
        await updateForm({ ...payload, id: editingId.value });
      } else {
        await createForm(payload);
      }
      message.success('保存成功');
      modalApi.close();
      emit('success');
    } finally {
      modalApi.lock(false);
    }
  },
  onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = modalApi.getData<FlowFormApi.Form>();
    editingId.value = data?.id;
    meta.value = {
      name: data?.name ?? '',
      formKey: data?.formKey ?? '',
      remark: data?.remark ?? '',
    };
    fields.value = [];
    if (data?.content) {
      try {
        const parsed: FlowFormApi.Field[] = JSON.parse(data.content);
        fields.value = parsed.map((f) => ({
          label: f.label,
          fieldName: f.fieldName,
          component: f.component,
          required: !!f.required,
          optionsText: (f.options ?? [])
            .map((o) => `${o.label}:${o.value}`)
            .join('\n'),
        }));
      } catch {
        fields.value = [];
      }
    }
  },
});
</script>

<template>
  <Modal :title="editingId ? '设计表单' : '新增表单'">
    <div class="grid grid-cols-2 gap-4 p-1">
      <!-- 左：字段编辑 -->
      <div class="space-y-3">
        <div class="grid grid-cols-3 gap-2">
          <Input v-model:value="meta.name" placeholder="表单名称" />
          <Input v-model:value="meta.formKey" placeholder="表单标识(formKey)" />
          <Input v-model:value="meta.remark" placeholder="备注" />
        </div>
        <div class="flex items-center justify-between">
          <span class="text-sm font-medium">字段</span>
          <Button size="small" @click="addField">
            <Plus class="size-4" /> 添加字段
          </Button>
        </div>
        <div
          v-for="(f, idx) in fields"
          :key="idx"
          class="space-y-2 rounded border p-2"
        >
          <div class="flex gap-2">
            <Input v-model:value="f.label" placeholder="标签" />
            <Input v-model:value="f.fieldName" placeholder="字段名(英文)" />
            <Button danger size="small" @click="removeField(idx)">删除</Button>
          </div>
          <div class="flex items-center gap-2">
            <Select
              v-model:value="f.component"
              class="w-40"
              :options="componentOptions"
            />
            <Checkbox v-model:checked="f.required">必填</Checkbox>
          </div>
          <Textarea
            v-if="needOptions(f.component)"
            v-model:value="f.optionsText"
            :rows="2"
            placeholder="每行一个选项，格式 标签:值"
          />
        </div>
      </div>
      <!-- 右：实时预览 -->
      <div class="rounded border p-3">
        <div class="mb-2 text-sm font-medium text-gray-600">预览</div>
        <FormRender
          v-if="previewSchema.length"
          :key="JSON.stringify(previewSchema)"
          :schema="previewSchema"
        />
        <div v-else class="text-sm text-gray-400">添加字段后在此预览</div>
      </div>
    </div>
  </Modal>
</template>
