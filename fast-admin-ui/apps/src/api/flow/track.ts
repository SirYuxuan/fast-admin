import { requestClient } from '#/api/request';

export namespace FlowTrackApi {
  export interface Diagram {
    xml: string;
    finishedIds: string[];
    activeIds: string[];
    flowIds: string[];
  }
  export interface Record {
    id: string;
    taskName?: string;
    assigneeName?: string;
    outcome?: string;
    comment?: string;
    createdAt?: string;
  }
}

const Url = '/flow/track';

export function getDiagram(instanceId: string) {
  return requestClient.get<FlowTrackApi.Diagram>(`${Url}/${instanceId}/diagram`);
}

export function getRecords(instanceId: string) {
  return requestClient.get<FlowTrackApi.Record[]>(
    `${Url}/${instanceId}/records`,
  );
}
