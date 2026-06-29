import { requestClient } from '#/api/request';

export namespace AiRagApi {
  export interface KnowledgeBase {
    [key: string]: any;
    id: string;
    name: string;
    description?: string;
    enabled: boolean;
    chunkSize: number;
    chunkOverlap: number;
    chunkDelimiter?: string;
    documentCount?: number;
    chunkCount?: number;
    lastIndexedAt?: string;
    remark?: string;
    createdAt?: string;
  }

  export interface KnowledgeDocument {
    [key: string]: any;
    id: string;
    knowledgeBaseId: string;
    fileId: string;
    fileName: string;
    contentType?: string;
    fileSize?: number;
    status: 'failed' | 'indexed' | 'indexing' | 'pending';
    chunkCount?: number;
    errorMsg?: string;
    indexedAt?: string;
    createdAt?: string;
  }

  export interface KnowledgeChunk {
    [key: string]: any;
    id: string;
    knowledgeBaseId: string;
    documentId: string;
    pointId: string;
    chunkIndex: number;
    tokenCount?: number;
    content: string;
    createdAt?: string;
  }

  export interface RecallResult {
    query: string;
    topK: number;
    latencyMs: number;
    items: Array<{
      chunkId: string;
      documentId: string;
      fileName?: string;
      chunkIndex?: number;
      score?: number;
      content: string;
    }>;
  }

  export interface VectorStoreStatus {
    collections: string[];
    connected: boolean;
    defaultCollection: string;
    defaultCollectionExists: boolean;
    enabled: boolean;
    latencyMs?: number;
    message?: string;
    status?: string;
    url: string;
    version?: string;
  }
}

const Url = '/ai/rag';

export function getAiKnowledgeBasePage(params: Record<string, any>) {
  return requestClient.get(`${Url}/knowledge`, { params });
}

export function getAiKnowledgeBaseDetail(id: string) {
  return requestClient.get<AiRagApi.KnowledgeBase>(`${Url}/knowledge/${id}`);
}

export function createAiKnowledgeBase(data: Partial<AiRagApi.KnowledgeBase>) {
  return requestClient.post(`${Url}/knowledge`, data);
}

export function updateAiKnowledgeBase(data: Partial<AiRagApi.KnowledgeBase>) {
  return requestClient.put(`${Url}/knowledge`, data);
}

export function changeAiKnowledgeBaseEnabled(id: string, enabled: boolean) {
  return requestClient.post(`${Url}/knowledge/${id}/enabled?enabled=${enabled}`);
}

export function deleteAiKnowledgeBase(id: string) {
  return requestClient.delete(`${Url}/knowledge/${id}`);
}

export function getAiKnowledgeDocumentPage(params: Record<string, any>) {
  return requestClient.get(`${Url}/documents`, { params });
}

export function getAiKnowledgeDocumentDetail(id: string) {
  return requestClient.get<AiRagApi.KnowledgeDocument>(`${Url}/documents/${id}`);
}

export function getAiKnowledgeChunkPage(id: string, params: Record<string, any>) {
  return requestClient.get(`${Url}/documents/${id}/chunks`, { params });
}

export function uploadAiKnowledgeDocument(knowledgeBaseId: string, file: File) {
  const form = new FormData();
  form.append('file', file);
  return requestClient.post<AiRagApi.KnowledgeDocument>(
    `${Url}/knowledge/${knowledgeBaseId}/documents/upload`,
    form,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 900000,
    },
  );
}

export function reindexAiKnowledgeDocument(id: string) {
  return requestClient.post(`${Url}/documents/${id}/reindex`);
}

export function deleteAiKnowledgeDocument(id: string, deleteSourceFile = false) {
  return requestClient.delete(`${Url}/documents/${id}`, {
    params: { deleteSourceFile },
  });
}

export function recallAiKnowledgeBase(data: {
  knowledgeBaseId: string;
  query: string;
  topK?: number;
}) {
  return requestClient.post<AiRagApi.RecallResult>(`${Url}/recall-test`, data);
}

export function getAiRagVectorStoreStatus() {
  return requestClient.get<AiRagApi.VectorStoreStatus>(
    `${Url}/vector-store/status`,
  );
}
