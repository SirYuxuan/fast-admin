import { requestClient } from '#/api/request';

export namespace FlowFormApi {
  export interface Field {
    component: string;
    fieldName: string;
    label: string;
    required?: boolean;
    options?: { label: string; value: any }[];
    [key: string]: any;
  }
  export interface Form {
    [key: string]: any;
    id: string;
    formKey: string;
    name: string;
    content?: string;
    remark?: string;
  }
}

const Url = '/flow/form';

export function getFormPage(params: Record<string, any>) {
  return requestClient.get(Url, { params });
}

export function getFormByKey(formKey: string) {
  return requestClient.get<FlowFormApi.Form>(`${Url}/key/${formKey}`);
}

export function createForm(data: Partial<FlowFormApi.Form>) {
  return requestClient.post(Url, data);
}

export function updateForm(data: Partial<FlowFormApi.Form>) {
  return requestClient.put(Url, data);
}

export function deleteForm(id: string) {
  return requestClient.delete(`${Url}/${id}`);
}
