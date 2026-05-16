import { requestClient } from '#/api/request';

export namespace SysDictApi {
  export interface DictType {
    [key: string]: any;
    id: string;
    dictName: string;
    dictType: string;
    status?: number;
    remark?: string;
    createdAt?: string;
  }

  export interface DictData {
    [key: string]: any;
    id: string;
    dictType: string;
    dictLabel: string;
    dictValue: string;
    dictSort?: number;
    cssClass?: string;
    listClass?: string;
    isDefault?: number;
    status?: number;
    remark?: string;
  }
}

// 字典类型
const TypeUrl = '/system/dict/type';
export function getDictTypePage(params: Record<string, any>) {
  return requestClient.get(TypeUrl, { params });
}
export function getAllDictType() {
  return requestClient.get<SysDictApi.DictType[]>(`${TypeUrl}/all`);
}
export function createDictType(data: Partial<SysDictApi.DictType>) {
  return requestClient.post(TypeUrl, data);
}
export function updateDictType(data: Partial<SysDictApi.DictType>) {
  return requestClient.put(TypeUrl, data);
}
export function deleteDictType(id: string) {
  return requestClient.delete(`${TypeUrl}/${id}`);
}

// 字典数据
const DataUrl = '/system/dict/data';
export function getDictDataPage(params: Record<string, any>) {
  return requestClient.get(DataUrl, { params });
}
export function listDictDataByType(dictType: string) {
  return requestClient.get<SysDictApi.DictData[]>(`${DataUrl}/type/${dictType}`);
}
export function createDictData(data: Partial<SysDictApi.DictData>) {
  return requestClient.post(DataUrl, data);
}
export function updateDictData(data: Partial<SysDictApi.DictData>) {
  return requestClient.put(DataUrl, data);
}
export function deleteDictData(id: string) {
  return requestClient.delete(`${DataUrl}/${id}`);
}
