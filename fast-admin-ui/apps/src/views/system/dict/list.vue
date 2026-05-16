<script lang="ts" setup>
import type { SysDictApi } from '#/api/system/dict';

import { onMounted, ref } from 'vue';

import { AccessControl } from '@vben/access';
import { Page, useVbenModal } from '@vben/common-ui';
import { IconifyIcon, Plus } from '@vben/icons';

import {
  Button,
  Card,
  Empty,
  Input,
  List,
  ListItem,
  message,
  Modal as AModal,
  Popconfirm,
  Tag,
} from 'ant-design-vue';

import {
  deleteDictData,
  deleteDictType,
  getDictTypePage,
  listDictDataByType,
} from '#/api/system/dict';

import DataForm from './modules/data-form.vue';
import TypeForm from './modules/type-form.vue';

const [TypeFormModal, typeFormApi] = useVbenModal({ connectedComponent: TypeForm });
const [DataFormModal, dataFormApi] = useVbenModal({ connectedComponent: DataForm });

const typeList = ref<SysDictApi.DictType[]>([]);
const filteredTypes = ref<SysDictApi.DictType[]>([]);
const typeSearch = ref('');
const selectedType = ref<SysDictApi.DictType | null>(null);
const dataList = ref<SysDictApi.DictData[]>([]);
const dataLoading = ref(false);

async function loadTypes() {
  const res: any = await getDictTypePage({ page: 1, pageSize: 1000 });
  typeList.value = res.items || [];
  applyTypeFilter();
  // 默认选中第一项
  if (!selectedType.value && typeList.value.length > 0) {
    onSelectType(typeList.value[0]);
  } else if (selectedType.value) {
    // 选中项可能被删除
    const found = typeList.value.find((t) => t.id === selectedType.value!.id);
    if (!found) {
      selectedType.value = typeList.value[0] || null;
      if (selectedType.value) onSelectType(selectedType.value);
      else dataList.value = [];
    }
  }
}

function applyTypeFilter() {
  const kw = typeSearch.value.trim().toLowerCase();
  filteredTypes.value = kw
    ? typeList.value.filter(
        (t) =>
          t.dictName.toLowerCase().includes(kw) ||
          t.dictType.toLowerCase().includes(kw),
      )
    : typeList.value;
}

async function onSelectType(t: SysDictApi.DictType) {
  selectedType.value = t;
  dataLoading.value = true;
  try {
    dataList.value = await listDictDataByType(t.dictType);
  } finally {
    dataLoading.value = false;
  }
}

function onAddType() {
  typeFormApi.setData(null).open();
}

function onEditType(t: SysDictApi.DictType, e: Event) {
  e.stopPropagation();
  typeFormApi.setData(t).open();
}

function onDelType(t: SysDictApi.DictType, e: Event) {
  e.stopPropagation();
  AModal.confirm({
    title: '删除确认',
    content: `确认删除「${t.dictName}」？该类型下的所有字典数据也会被一并删除。`,
    onOk: () =>
      deleteDictType(t.id).then(() => {
        message.success('删除成功');
        if (selectedType.value?.id === t.id) {
          selectedType.value = null;
          dataList.value = [];
        }
        loadTypes();
      }),
  });
}

function onAddData() {
  if (!selectedType.value) {
    message.warning('请先选择字典类型');
    return;
  }
  dataFormApi.setData({ dictType: selectedType.value.dictType }).open();
}

function onEditData(d: SysDictApi.DictData) {
  dataFormApi.setData(d).open();
}

function onDelData(d: SysDictApi.DictData) {
  deleteDictData(d.id).then(() => {
    message.success('删除成功');
    if (selectedType.value) onSelectType(selectedType.value);
  });
}

function onTypeSaved() {
  message.success('保存成功');
  loadTypes();
}

function onDataSaved() {
  message.success('保存成功');
  if (selectedType.value) onSelectType(selectedType.value);
}

onMounted(loadTypes);
</script>

<template>
  <Page auto-content-height>
    <div class="dict-wrap">
      <!-- 左：字典类型 -->
      <Card title="字典类型" :body-style="{ padding: 0 }" class="type-card">
        <template #extra>
          <AccessControl :codes="['system:dict:type:add']">
            <Button type="primary" size="small" @click="onAddType">
              <Plus class="size-4" />
              新增
            </Button>
          </AccessControl>
        </template>
        <div class="search-bar">
          <Input
            v-model:value="typeSearch"
            allow-clear
            placeholder="搜索字典名/编码"
            @update:value="applyTypeFilter"
          >
            <template #prefix>
              <IconifyIcon icon="lucide:search" class="text-gray-400" />
            </template>
          </Input>
        </div>
        <div class="type-list">
          <Empty v-if="filteredTypes.length === 0" />
          <div
            v-for="t in filteredTypes"
            :key="t.id"
            class="type-item"
            :class="{ active: selectedType?.id === t.id }"
            @click="onSelectType(t)"
          >
            <div class="flex-1 overflow-hidden">
              <div class="type-name">{{ t.dictName }}</div>
              <div class="type-code">{{ t.dictType }}</div>
            </div>
            <div class="type-actions">
              <AccessControl :codes="['system:dict:type:edit']">
                <span class="icon-btn" @click="onEditType(t, $event)">
                  <IconifyIcon icon="lucide:pencil" />
                </span>
              </AccessControl>
              <AccessControl :codes="['system:dict:type:delete']">
                <span class="icon-btn danger" @click="onDelType(t, $event)">
                  <IconifyIcon icon="lucide:trash-2" />
                </span>
              </AccessControl>
            </div>
          </div>
        </div>
      </Card>

      <!-- 右：字典数据 -->
      <Card
        :title="selectedType ? `字典数据 - ${selectedType.dictName}` : '字典数据'"
        class="data-card"
      >
        <template #extra>
          <AccessControl :codes="['system:dict:data:add']">
            <Button type="primary" size="small" :disabled="!selectedType" @click="onAddData">
              <Plus class="size-4" />
              新增数据
            </Button>
          </AccessControl>
        </template>
        <Empty v-if="!selectedType" description="请选择左侧字典类型" />
        <List v-else :loading="dataLoading" :data-source="dataList" :split="true">
          <template #renderItem="{ item }">
            <ListItem>
              <div class="data-row">
                <div class="data-info">
                  <Tag :color="item.listClass || 'default'">{{ item.dictLabel }}</Tag>
                  <span class="data-value">{{ item.dictValue }}</span>
                  <span class="data-sort">排序 {{ item.dictSort ?? 0 }}</span>
                  <Tag v-if="item.status === 0" color="red">禁用</Tag>
                </div>
                <div class="data-actions">
                  <AccessControl :codes="['system:dict:data:edit']">
                    <Button type="link" size="small" @click="onEditData(item)">编辑</Button>
                  </AccessControl>
                  <AccessControl :codes="['system:dict:data:delete']">
                    <Popconfirm
                      title="确认删除该项？"
                      @confirm="onDelData(item)"
                    >
                      <Button type="link" danger size="small">删除</Button>
                    </Popconfirm>
                  </AccessControl>
                </div>
              </div>
            </ListItem>
          </template>
        </List>
      </Card>
    </div>

    <TypeFormModal @success="onTypeSaved" />
    <DataFormModal @success="onDataSaved" />
  </Page>
</template>

<style scoped>
.dict-wrap {
  display: flex;
  gap: 12px;
  height: 100%;
}

.type-card {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.type-card :deep(.ant-card-body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.search-bar {
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
}

.type-list {
  flex: 1;
  overflow-y: auto;
}

.type-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.15s;
}

.type-item:hover {
  background: rgba(0, 0, 0, 0.02);
}

.type-item.active {
  background: rgba(24, 144, 255, 0.08);
  border-left: 3px solid #1890ff;
  padding-left: 9px;
}

.type-name {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.type-code {
  font-size: 12px;
  color: #8c8c8c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.type-actions {
  display: none;
  gap: 4px;
  margin-left: 8px;
}

.type-item:hover .type-actions {
  display: flex;
}

.icon-btn {
  padding: 4px;
  border-radius: 4px;
  color: #595959;
  cursor: pointer;
}

.icon-btn:hover {
  background: rgba(0, 0, 0, 0.06);
}

.icon-btn.danger:hover {
  color: #ff4d4f;
}

.data-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.data-card :deep(.ant-card-body) {
  flex: 1;
  overflow: auto;
}

.data-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.data-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.data-value {
  color: #595959;
  font-family: monospace;
}

.data-sort {
  color: #8c8c8c;
  font-size: 12px;
}
</style>
