import { requestClient } from '#/api/request';

export namespace SysOnlineApi {
  export interface OnlineUser {
    tokenValue: string;
    userId: string;
    username: string;
    nickname?: string;
    loginIp?: string;
    browser?: string;
    os?: string;
    loginTime?: number;
    tokenTimeout?: number;
  }
}

const Url = '/system/online';

export function getOnlineUsers(keyword?: string) {
  return requestClient.get<SysOnlineApi.OnlineUser[]>(Url, {
    params: keyword ? { keyword } : {},
  });
}

export function kickoutUser(tokenValue: string) {
  return requestClient.delete(`${Url}/${tokenValue}`);
}
