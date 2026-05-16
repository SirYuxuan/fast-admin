import { requestClient } from '#/api/request';

export namespace SysJobApi {
  export interface Job {
    [key: string]: any;
    id: string;
    jobName: string;
    jobGroup?: string;
    beanName: string;
    methodName?: string;
    methodParams?: string;
    cronExpression: string;
    misfirePolicy?: number; // 1 立即执行 2 忽略 3 触发一次
    concurrent?: number; // 1 允许 0 不允许
    status?: number; // 1 启动 0 暂停
    remark?: string;
    createdAt?: string;
  }

  export interface JobLog {
    id: string;
    jobId: string;
    jobName: string;
    jobGroup?: string;
    beanName?: string;
    methodName?: string;
    methodParams?: string;
    status: number;
    costTime?: number;
    errorMsg?: string;
    createdAt: string;
  }
}

const JobUrl = '/system/job';
const LogUrl = '/system/job/log';

export function getJobPage(params: Record<string, any>) {
  return requestClient.get(JobUrl, { params });
}
export function getJobDetail(id: string) {
  return requestClient.get<SysJobApi.Job>(`${JobUrl}/${id}`);
}
export function createJob(data: Partial<SysJobApi.Job>) {
  return requestClient.post(JobUrl, data);
}
export function updateJob(data: Partial<SysJobApi.Job>) {
  return requestClient.put(JobUrl, data);
}
export function deleteJob(id: string) {
  return requestClient.delete(`${JobUrl}/${id}`);
}
export function startJob(id: string) {
  return requestClient.post(`${JobUrl}/${id}/start`);
}
export function pauseJob(id: string) {
  return requestClient.post(`${JobUrl}/${id}/pause`);
}
export function runJobOnce(id: string) {
  return requestClient.post(`${JobUrl}/${id}/run`);
}

export function getJobLogPage(params: Record<string, any>) {
  return requestClient.get(LogUrl, { params });
}
export function deleteJobLog(id: string) {
  return requestClient.delete(`${LogUrl}/${id}`);
}
export function cleanJobLog() {
  return requestClient.delete(`${LogUrl}/clean`);
}
