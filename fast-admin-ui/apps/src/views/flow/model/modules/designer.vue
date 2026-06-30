<script lang="ts" setup>
import type { FlowModelApi } from '#/api/flow/model';

import { computed, markRaw, nextTick, reactive, ref, shallowRef } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Button, Input, message, Select, TreeSelect } from 'ant-design-vue';
import BpmnModeler from 'bpmn-js/lib/Modeler';

import {
  deployModel,
  getModelDetail,
  saveModelBpmn,
} from '#/api/flow/model';
import {
  getDeptTree,
  getRoleOptions,
  getUserOptions,
  type TreeOption,
} from '#/api/flow/org';

import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css';

const emit = defineEmits(['success']);

/** Flowable 命名空间扩展，使设计器能读写 flowable:* 属性 */
const flowableModdle = {
  name: 'Flowable',
  uri: 'http://flowable.org/bpmn',
  prefix: 'flowable',
  xml: { tagAlias: 'lowerCase' },
  associations: [],
  types: [
    {
      name: 'InitiatorMixin',
      isAbstract: true,
      extends: ['bpmn:StartEvent'],
      properties: [
        { name: 'initiator', isAttr: true, type: 'String' },
        { name: 'formKey', isAttr: true, type: 'String' },
      ],
    },
    {
      name: 'AssignableMixin',
      isAbstract: true,
      extends: ['bpmn:UserTask'],
      properties: [
        { name: 'assignee', isAttr: true, type: 'String' },
        { name: 'candidateUsers', isAttr: true, type: 'String' },
        { name: 'candidateGroups', isAttr: true, type: 'String' },
        { name: 'formKey', isAttr: true, type: 'String' },
        { name: 'dueDate', isAttr: true, type: 'String' },
      ],
    },
  ],
};

const canvasRef = ref<HTMLDivElement>();
const modeler = shallowRef<any>();
const current = ref<FlowModelApi.Model>();

const userOptions = ref<{ label: string; value: string }[]>([]);
const roleOptions = ref<{ label: string; value: string }[]>([]);
const deptTree = ref<TreeOption[]>([]);
const filterOption = (input: string, option: any) =>
  (option?.label ?? '').toLowerCase().includes(input.toLowerCase());

const ROLE_PREFIX = 'ROLE:';
const DEPT_PREFIX = 'DEPT:';

const selected = shallowRef<any>();
const props = reactive({
  id: '',
  name: '',
  candidateUsers: [] as string[],
  candidateRoles: [] as string[],
  candidateDepts: [] as string[],
  formKey: '',
  condition: '',
});

const splitCsv = (v?: string) =>
  v ? v.split(',').map((s) => s.trim()).filter(Boolean) : [];
const elType = computed(() => selected.value?.businessObject?.$type ?? '');
const isUserTask = computed(() => elType.value === 'bpmn:UserTask');
const isStartEvent = computed(() => elType.value === 'bpmn:StartEvent');
const canHaveForm = computed(() => isUserTask.value || isStartEvent.value);
const isSequenceFlow = computed(() => elType.value === 'bpmn:SequenceFlow');
const isSelected = computed(() => !!selected.value);

function syncPanelFromElement(el: any) {
  selected.value = el;
  const bo = el?.businessObject;
  props.id = bo?.id ?? '';
  props.name = bo?.name ?? '';
  props.candidateUsers = splitCsv(bo?.get?.('flowable:candidateUsers'));
  // candidateGroups 混存角色(ROLE:)与部门(DEPT:)，按前缀拆分；无前缀视作历史角色
  const groups = splitCsv(bo?.get?.('flowable:candidateGroups'));
  props.candidateRoles = groups
    .filter((g) => !g.startsWith(DEPT_PREFIX))
    .map((g) => (g.startsWith(ROLE_PREFIX) ? g.slice(ROLE_PREFIX.length) : g));
  props.candidateDepts = groups
    .filter((g) => g.startsWith(DEPT_PREFIX))
    .map((g) => g.slice(DEPT_PREFIX.length));
  props.formKey = bo?.get?.('flowable:formKey') ?? '';
  props.condition = bo?.conditionExpression?.body ?? '';
}

function update(key: string, value: string) {
  if (!selected.value || !modeler.value) return;
  const modeling = modeler.value.get('modeling');
  modeling.updateProperties(selected.value, { [key]: value || undefined });
}

/** 候选人多选写回 */
function updateUsers() {
  update('flowable:candidateUsers', props.candidateUsers.join(','));
}

/** 角色 + 部门合并为带前缀的 candidateGroups 写回 */
function updateGroups() {
  const groups = [
    ...props.candidateRoles.map((r) => ROLE_PREFIX + r),
    ...props.candidateDepts.map((d) => DEPT_PREFIX + d),
  ];
  update('flowable:candidateGroups', groups.join(','));
}

/** 连线条件表达式，如 ${outcome=='approve'} */
function updateCondition(value: string) {
  if (!selected.value || !modeler.value) return;
  const modeling = modeler.value.get('modeling');
  const moddle = modeler.value.get('moddle');
  const condition = value
    ? moddle.create('bpmn:FormalExpression', { body: value })
    : undefined;
  modeling.updateProperties(selected.value, { conditionExpression: condition });
}

async function initModeler(xml: string) {
  destroyModeler();
  const instance = new BpmnModeler({
    container: canvasRef.value,
    moddleExtensions: { flowable: flowableModdle },
  });
  modeler.value = markRaw(instance);
  instance.on('selection.changed', (e: any) => {
    const el = e.newSelection?.[0];
    if (el) {
      syncPanelFromElement(el);
    } else {
      selected.value = undefined;
    }
  });
  instance.on('element.changed', (e: any) => {
    if (selected.value && e.element?.id === selected.value.id) {
      syncPanelFromElement(e.element);
    }
  });
  try {
    await instance.importXML(xml);
    instance.get('canvas').zoom('fit-viewport');
  } catch (error: any) {
    message.error(`流程图加载失败：${error?.message ?? error}`);
  }
}

function destroyModeler() {
  if (modeler.value) {
    modeler.value.destroy();
    modeler.value = undefined;
  }
  selected.value = undefined;
}

async function getXml(): Promise<string> {
  const { xml } = await modeler.value.saveXML({ format: true });
  return xml;
}

async function onSave() {
  if (!current.value) return;
  try {
    const xml = await getXml();
    await saveModelBpmn(current.value.id, xml);
    message.success('已保存');
    emit('success');
  } catch (error: any) {
    message.error(`保存失败：${error?.message ?? error}`);
  }
}

async function onSaveAndDeploy() {
  if (!current.value) return;
  try {
    const xml = await getXml();
    await saveModelBpmn(current.value.id, xml);
    await deployModel(current.value.id);
    message.success('保存并部署成功');
    emit('success');
    modalApi.close();
  } catch (error: any) {
    message.error(`部署失败：${error?.response?.data?.message ?? error?.message ?? error}`);
  }
}

const [Modal, modalApi] = useVbenModal({
  fullscreen: true,
  fullscreenButton: false,
  showConfirmButton: false,
  showCancelButton: false,
  async onOpenChange(isOpen) {
    if (!isOpen) {
      destroyModeler();
      return;
    }
    const data = modalApi.getData<FlowModelApi.Model>();
    if (!data?.id) return;
    modalApi.setState({ loading: true });
    try {
      if (userOptions.value.length === 0) {
        userOptions.value = await getUserOptions();
      }
      if (roleOptions.value.length === 0) {
        roleOptions.value = await getRoleOptions();
      }
      if (deptTree.value.length === 0) {
        deptTree.value = await getDeptTree();
      }
      const detail = await getModelDetail(data.id);
      current.value = detail;
      await nextTick();
      await initModeler(detail.bpmnXml || '');
    } finally {
      modalApi.setState({ loading: false });
    }
  },
});
</script>

<template>
  <Modal :title="`流程设计 - ${current?.name ?? ''}`">
    <div class="flex h-full flex-col">
      <div class="flex items-center gap-2 border-b px-3 py-2">
        <Button type="primary" @click="onSave">保存</Button>
        <Button type="primary" ghost @click="onSaveAndDeploy">保存并部署</Button>
        <span class="text-muted-foreground ml-2 text-xs">
          流程标识：{{ current?.modelKey }}
        </span>
      </div>
      <div class="flex min-h-0 flex-1">
        <div ref="canvasRef" class="min-w-0 flex-1 bg-white"></div>
        <div class="w-[300px] overflow-auto border-l p-4">
          <div class="mb-3 font-medium">属性</div>
          <template v-if="isSelected">
            <div class="mb-3">
              <div class="mb-1 text-xs text-gray-500">节点 ID</div>
              <Input :value="props.id" disabled size="small" />
            </div>
            <div class="mb-3">
              <div class="mb-1 text-xs text-gray-500">名称</div>
              <Input
                v-model:value="props.name"
                size="small"
                placeholder="节点名称"
                @change="update('name', props.name)"
              />
            </div>
            <template v-if="isUserTask">
              <div class="mb-2 text-xs font-medium text-gray-600">
                审批人（人员 / 角色 / 部门，可任意组合，满足其一即可处理）
              </div>
              <div class="mb-3">
                <div class="mb-1 text-xs text-gray-500">指定人员</div>
                <Select
                  v-model:value="props.candidateUsers"
                  size="small"
                  mode="multiple"
                  class="w-full"
                  allow-clear
                  option-filter-prop="label"
                  :filter-option="filterOption"
                  :options="userOptions"
                  placeholder="选择一人或多人"
                  @change="updateUsers"
                />
              </div>
              <div class="mb-3">
                <div class="mb-1 text-xs text-gray-500">按角色</div>
                <Select
                  v-model:value="props.candidateRoles"
                  size="small"
                  mode="multiple"
                  class="w-full"
                  allow-clear
                  option-filter-prop="label"
                  :filter-option="filterOption"
                  :options="roleOptions"
                  placeholder="选择角色"
                  @change="updateGroups"
                />
              </div>
              <div class="mb-3">
                <div class="mb-1 text-xs text-gray-500">按部门（含子部门）</div>
                <TreeSelect
                  v-model:value="props.candidateDepts"
                  size="small"
                  class="w-full"
                  allow-clear
                  multiple
                  tree-default-expand-all
                  :tree-data="deptTree"
                  placeholder="选择部门"
                  @change="updateGroups"
                />
              </div>
            </template>
            <div v-if="canHaveForm" class="mb-3">
              <div class="mb-1 text-xs text-gray-500">表单标识 (formKey)</div>
              <Input
                v-model:value="props.formKey"
                size="small"
                placeholder="绑定自定义表单的 formKey"
                @change="update('flowable:formKey', props.formKey)"
              />
            </div>
            <div v-if="isSequenceFlow" class="mb-3">
              <div class="mb-1 text-xs text-gray-500">流转条件</div>
              <Input
                v-model:value="props.condition"
                size="small"
                placeholder="如 ${outcome=='approve'}"
                @change="updateCondition(props.condition)"
              />
            </div>
          </template>
          <div v-else class="text-xs text-gray-400">
            在左侧画布选择一个节点以编辑其属性
          </div>
        </div>
      </div>
    </div>
  </Modal>
</template>
