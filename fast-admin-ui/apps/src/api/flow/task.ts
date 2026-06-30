import { requestClient } from '#/api/request';

export namespace FlowTaskApi {
  export interface TodoTask {
    [key: string]: any;
    taskId: string;
    taskName: string;
    processInstanceId: string;
    processName?: string;
    assignee?: string;
    claimed?: boolean;
    createTime?: string;
  }
  export interface CompletePayload {
    outcome: 'approve' | 'reject';
    comment?: string;
    variables?: Record<string, any>;
    ccUserIds?: string[];
  }
}

const Url = '/flow/task';

export function getTodoTasks(params: Record<string, any>) {
  return requestClient.get(`${Url}/todo`, { params });
}

export function getDoneTasks(params: Record<string, any>) {
  return requestClient.get(`${Url}/done`, { params });
}

export function getTaskDetail(taskId: string) {
  return requestClient.get(`${Url}/${taskId}`);
}

export function completeTask(
  taskId: string,
  payload: FlowTaskApi.CompletePayload,
) {
  return requestClient.post(`${Url}/${taskId}/complete`, payload);
}

export function transferTask(
  taskId: string,
  toUserId: string,
  comment?: string,
) {
  return requestClient.post(`${Url}/${taskId}/transfer`, null, {
    params: { toUserId, comment },
  });
}

export function claimTask(taskId: string) {
  return requestClient.post(`${Url}/${taskId}/claim`);
}
