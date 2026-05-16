<script setup lang="ts">
import { ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Button, Modal as AModal, Slider, Spin } from 'ant-design-vue';

import 'vue-advanced-cropper/dist/style.css';
import { CircleStencil, Cropper } from 'vue-advanced-cropper';

const props = defineProps<{
  open: boolean;
  /** 待裁剪的图片源（File 或 dataURL 字符串） */
  src: File | string | null;
  /** 上传中状态，由父组件传入 */
  uploading?: boolean;
}>();

const emits = defineEmits<{
  (e: 'update:open', val: boolean): void;
  (e: 'confirm', blob: Blob): void;
}>();

const cropperRef = ref();
const imgSrc = ref<string>('');
const previewSrc = ref<string>('');
const zoomValue = ref(1);

// 当 src 变化时读取图片
watch(
  () => props.src,
  (val) => {
    if (!val) {
      imgSrc.value = '';
      previewSrc.value = '';
      return;
    }
    if (typeof val === 'string') {
      imgSrc.value = val;
      return;
    }
    // 转 File 为 DataURL
    const reader = new FileReader();
    reader.onload = (e) => {
      imgSrc.value = (e.target?.result as string) || '';
    };
    reader.readAsDataURL(val);
  },
  { immediate: true },
);

// 裁剪框变化时更新预览
function onChange({ canvas }: { canvas?: HTMLCanvasElement }) {
  if (canvas) {
    previewSrc.value = canvas.toDataURL('image/png');
  }
}

// 旋转
function rotate(angle: number) {
  cropperRef.value?.rotate?.(angle);
}

// 缩放：滑块变化 → 计算因子
function onZoomChange(val: number | [number, number]) {
  const v = Array.isArray(val) ? val[0] : val;
  if (zoomValue.value === 0) {
    zoomValue.value = v;
    return;
  }
  const factor = v / zoomValue.value;
  zoomValue.value = v;
  cropperRef.value?.zoom?.(factor);
}

// 取消
function onCancel() {
  emits('update:open', false);
}

// 确认裁剪 → 输出 PNG Blob
function onConfirm() {
  const result = cropperRef.value?.getResult?.();
  if (!result?.canvas) return;
  result.canvas.toBlob(
    (blob: Blob | null) => {
      if (blob) emits('confirm', blob);
    },
    'image/png',
    0.92,
  );
}

// Modal 关闭重置
watch(
  () => props.open,
  (val) => {
    if (val) {
      zoomValue.value = 1;
    }
  },
);
</script>

<template>
  <AModal
    :open="open"
    title="裁剪头像"
    :width="780"
    :mask-closable="false"
    :keyboard="false"
    :footer="null"
    @cancel="onCancel"
  >
    <Spin :spinning="!!props.uploading" tip="正在上传...">
      <div class="cropper-wrap">
        <!-- 左侧裁剪区 -->
        <div class="cropper-area">
          <Cropper
            v-if="imgSrc"
            ref="cropperRef"
            class="cropper-canvas"
            :src="imgSrc"
            :stencil-component="CircleStencil"
            :stencil-props="{
              aspectRatio: 1,
              handlers: {},
              movable: false,
              resizable: false,
            }"
            image-restriction="stencil"
            @change="onChange"
          />
        </div>

        <!-- 右侧预览 -->
        <div class="preview-area">
          <div class="preview-title">预览效果</div>
          <div class="preview-list">
            <div class="preview-circle preview-lg">
              <img v-if="previewSrc" :src="previewSrc" alt="" />
            </div>
            <div class="preview-circle preview-md">
              <img v-if="previewSrc" :src="previewSrc" alt="" />
            </div>
            <div class="preview-circle preview-sm">
              <img v-if="previewSrc" :src="previewSrc" alt="" />
            </div>
          </div>
        </div>
      </div>

      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="zoom-row">
          <IconifyIcon icon="lucide:zoom-out" />
          <Slider
            :value="zoomValue"
            :min="0.5"
            :max="3"
            :step="0.1"
            class="zoom-slider"
            @update:value="onZoomChange"
          />
          <IconifyIcon icon="lucide:zoom-in" />
        </div>
        <div class="tool-buttons">
          <Button @click="rotate(-90)">
            <template #icon>
              <IconifyIcon icon="lucide:rotate-ccw" />
            </template>
            向左旋转
          </Button>
          <Button @click="rotate(90)">
            <template #icon>
              <IconifyIcon icon="lucide:rotate-cw" />
            </template>
            向右旋转
          </Button>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="footer-row">
        <Button @click="onCancel">取消</Button>
        <Button type="primary" :loading="props.uploading" @click="onConfirm">
          确认上传
        </Button>
      </div>
    </Spin>
  </AModal>
</template>

<style scoped>
.cropper-wrap {
  display: flex;
  gap: 16px;
}

.cropper-area {
  flex: 1;
  height: 400px;
  background: #1a1a1a;
  border-radius: 4px;
  overflow: hidden;
}

.cropper-canvas {
  height: 100%;
}

.preview-area {
  width: 200px;
  flex-shrink: 0;
  padding-left: 16px;
  border-left: 1px solid #f0f0f0;
}

.preview-title {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 16px;
}

.preview-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.preview-circle {
  border-radius: 50%;
  overflow: hidden;
  background: #f5f5f5;
  border: 1px solid #f0f0f0;
}

.preview-circle img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-lg {
  width: 120px;
  height: 120px;
}

.preview-md {
  width: 64px;
  height: 64px;
}

.preview-sm {
  width: 36px;
  height: 36px;
}

.toolbar {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 4px;
}

.zoom-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  color: #595959;
}

.zoom-slider {
  flex: 1;
  margin: 0 8px;
}

.tool-buttons {
  display: flex;
  gap: 8px;
}

.footer-row {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
