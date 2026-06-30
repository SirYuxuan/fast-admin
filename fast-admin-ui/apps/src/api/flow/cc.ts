import { requestClient } from '#/api/request';

export namespace FlowCcApi {
  export interface CcRecord {
    [key: string]: any;
    id: string;
    processInstanceId: string;
    processName?: string;
    taskName?: string;
    creatorName?: string;
    isRead?: number;
    createdAt?: string;
  }
}

const Url = '/flow/cc';

export function getMyCc(params: Record<string, any>) {
  return requestClient.get(Url, { params });
}

export function getCcUnread() {
  return requestClient.get<number>(`${Url}/unread`);
}

export function markCcRead(id: string) {
  return requestClient.post(`${Url}/${id}/read`);
}
