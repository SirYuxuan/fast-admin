import { requestClient } from '#/api/request';

export namespace AiUsageApi {
  export interface ModelUsage {
    modelCode: string;
    modelName?: string;
    messages: number;
    totalTokens: number;
  }

  export interface DailyUsage {
    day: string;
    messages: number;
    totalTokens: number;
  }

  export interface UsageStats {
    totalMessages: number;
    promptTokens: number;
    completionTokens: number;
    totalTokens: number;
    byModel: ModelUsage[];
    byDay: DailyUsage[];
  }
}

export function getAiUsageStats(days = 14) {
  return requestClient.get<AiUsageApi.UsageStats>('/ai/usage/stats', {
    params: { days },
  });
}
