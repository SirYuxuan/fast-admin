import { requestClient } from '#/api/request';

export namespace SystemFileApi {
  export type StorageType = 'FTP' | 'LOCAL' | 'OSS' | 'S3' | 'SFTP';

  export interface FileConfig {
    [key: string]: any;
    id: string;
    name: string;
    type: StorageType;
    config: Record<string, any>;
    urlPrefix: string;
    isActive: boolean;
    remark?: string;
  }

  export interface FileRecord {
    [key: string]: any;
    id: string;
    originalName: string;
    storageKey: string;
    url: string;
    size: number;
    contentType?: string;
    ext?: string;
    storageType: StorageType;
    configId: string;
    bizType?: string;
    bizId?: string;
    createdAt: string;
  }
}

const ConfigUrl = '/system/file/config';
const FileUrl = '/system/file';

// ---- 文件配置 ----

export async function getFileConfigPage(params: Record<string, any>) {
  return requestClient.get(ConfigUrl, { params });
}

export async function getFileConfigDetail(id: string) {
  return requestClient.get<SystemFileApi.FileConfig>(`${ConfigUrl}/${id}`);
}

export async function createFileConfig(data: Partial<SystemFileApi.FileConfig>) {
  return requestClient.post(ConfigUrl, data);
}

export async function updateFileConfig(data: Partial<SystemFileApi.FileConfig>) {
  return requestClient.put(ConfigUrl, data);
}

export async function activateFileConfig(id: string) {
  return requestClient.post(`${ConfigUrl}/${id}/activate`);
}

export async function deleteFileConfig(id: string) {
  return requestClient.delete(`${ConfigUrl}/${id}`);
}

// ---- 文件列表 ----

export async function getFilePage(params: Record<string, any>) {
  return requestClient.get(FileUrl, { params });
}

export async function deleteFile(id: string) {
  return requestClient.delete(`${FileUrl}/${id}`);
}

export function getFileDownloadUrl(id: string) {
  return `${FileUrl}/${id}/download`;
}

export async function uploadFile(file: File, bizType?: string, bizId?: string) {
  const form = new FormData();
  form.append('file', file);
  if (bizType) form.append('bizType', bizType);
  if (bizId) form.append('bizId', bizId);
  return requestClient.post<SystemFileApi.FileRecord>(
    `${FileUrl}/upload`,
    form,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  );
}
