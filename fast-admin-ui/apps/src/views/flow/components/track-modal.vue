<script lang="ts" setup>
import { markRaw, nextTick, ref, shallowRef } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Empty, message, Timeline, TimelineItem } from 'ant-design-vue';
import BpmnViewer from 'bpmn-js/lib/NavigatedViewer';

import { getDiagram, getRecords } from '#/api/flow/track';

import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css';

const canvasRef = ref<HTMLDivElement>();
const viewer = shallowRef<any>();
const records = ref<any[]>([]);

const outcomeMeta: Record<string, { color: string; text: string }> = {
  approve: { color: 'green', text: '同意' },
  cancel: { color: 'gray', text: '撤销' },
  reject: { color: 'red', text: '驳回' },
  start: { color: 'blue', text: '发起' },
  transfer: { color: 'orange', text: '转办' },
};

function destroy() {
  if (viewer.value) {
    viewer.value.destroy();
    viewer.value = undefined;
  }
}

async function render(instanceId: string) {
  const [diagram, recs] = await Promise.all([
    getDiagram(instanceId),
    getRecords(instanceId),
  ]);
  records.value = recs ?? [];
  destroy();
  const instance = new BpmnViewer({ container: canvasRef.value });
  viewer.value = markRaw(instance);
  try {
    await instance.importXML(diagram.xml);
    const canvas = instance.get('canvas');
    canvas.zoom('fit-viewport');
    diagram.finishedIds?.forEach((id) => canvas.addMarker(id, 'flow-finished'));
    diagram.flowIds?.forEach((id) => canvas.addMarker(id, 'flow-finished'));
    diagram.activeIds?.forEach((id) => canvas.addMarker(id, 'flow-active'));
  } catch (error: any) {
    message.error(`流程图渲染失败：${error?.message ?? error}`);
  }
}

const [Modal, modalApi] = useVbenModal({
  fullscreen: true,
  showConfirmButton: false,
  showCancelButton: false,
  async onOpenChange(isOpen) {
    if (!isOpen) {
      destroy();
      return;
    }
    const { instanceId } = modalApi.getData<{ instanceId: string }>() ?? {};
    if (!instanceId) return;
    modalApi.setState({ loading: true });
    try {
      await nextTick();
      await render(instanceId);
    } finally {
      modalApi.setState({ loading: false });
    }
  },
});
</script>

<template>
  <Modal title="流程跟踪">
    <div class="flex h-full">
      <div ref="canvasRef" class="flow-track-canvas min-w-0 flex-1 bg-white"></div>
      <div class="w-[320px] overflow-auto border-l p-4">
        <div class="mb-3 font-medium">审批轨迹</div>
        <Timeline v-if="records.length">
          <TimelineItem
            v-for="r in records"
            :key="r.id"
            :color="outcomeMeta[r.outcome]?.color ?? 'blue'"
          >
            <div class="text-sm font-medium">
              {{ r.taskName || '流程' }} ·
              {{ outcomeMeta[r.outcome]?.text ?? r.outcome }}
            </div>
            <div class="text-xs text-gray-500">
              {{ r.assigneeName }} · {{ r.createdAt }}
            </div>
            <div v-if="r.comment" class="mt-1 text-xs text-gray-700">
              意见：{{ r.comment }}
            </div>
          </TimelineItem>
        </Timeline>
        <Empty v-else description="暂无审批记录" />
      </div>
    </div>
  </Modal>
</template>

<style>
.flow-track-canvas .flow-finished .djs-visual > :nth-child(1) {
  stroke: #52c41a !important;
  fill: #f6ffed !important;
}
.flow-track-canvas .flow-finished.djs-connection .djs-visual > :nth-child(1) {
  stroke: #52c41a !important;
}
.flow-track-canvas .flow-active .djs-visual > :nth-child(1) {
  stroke: #1677ff !important;
  fill: #e6f4ff !important;
  stroke-width: 2px !important;
}
</style>
