import { defHttp } from '/@/utils/http/axios';
import { LoginParams, LoginResultModel, GetUserInfoModel } from './model/userModel';

import { ErrorMessageMode } from '/#/axios';

enum Api {
  Login = '/login',
  SmsLogin = '/loginSms',
  Logout = '/logout',
  GetUserInfo = '/user/getUserInfo',
  GetPermCode = '/user/getPermCode',
  TestRetry = '/testRetry',
}

import JSEncrypt from 'jsencrypt'

const publicKey =
    'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzKYx4aie8aXprDV8osKA\n' +
    'MqZ6LUGk5OFfK31P3urfqaDkO+WE6wI4+ZQELUHQ6DMuPjwt+uxv26e7a1OFXa8s\n' +
    'seErYDFrWOuTv3zm2s7UISci4Psjs/95LK9ILIN74jkvQgC3TATiG4JjKfkf0iUD\n' +
    '1m8zpgPX/2yZUBckxbwTtsm8XdBajRwD8HCI/yj5Z0Su1Zu/C9cXk6NYs2FR0Gyl\n' +
    'vulzFmBtUN4Q+e0e6Qkp1CmYAKsBm35JRDwmyqttiNQhuvfG1Z9ikaOPm53XB205\n' +
    'obtCJfpVIOEJ1zRuAEVYE6vf+LPaq024ipE3Dx4MOnXbd8VcnorttVOMP5OrLTw0\n' +
    'zwIDAQAB'

/**
 * rsa加密
 * @param str 密文
 */
export function encryptedStr(str): string {
  const encrypt = new JSEncrypt()
  encrypt.setPublicKey(`-----BEGIN PUBLIC KEY-----${publicKey}-----END PUBLIC KEY-----`)
  const data = encrypt.encrypt(str)
  if (!data) {
    return ''
  }
  return data
}

/**
 * @description: user login api
 */
export function loginApi(params: LoginParams, mode: ErrorMessageMode = 'modal') {
  params['password'] = encryptedStr(params['password'])
  return defHttp.post<LoginResultModel>(
    {
      url: Api.Login,
      params,
    },
    {
      errorMessageMode: mode,
    },
  );
}

/**
 * 短信验证码登陆
 * @param params 验证码信息
 * @constructor
 */
export function loginSms(params: any) {
  return defHttp.post<LoginResultModel>(
    {
      url: Api.SmsLogin,
      params,
    }
  );
}

/**
 * @description: getUserInfo
 */
export function getUserInfo() {
  return defHttp.get<GetUserInfoModel>({ url: Api.GetUserInfo }, { errorMessageMode: 'none' });
}

export function getPermCode() {
  return defHttp.get<string[]>({ url: Api.GetPermCode });
}

export function doLogout() {
  return defHttp.get({ url: Api.Logout });
}

export function testRetry() {
  return defHttp.get(
    { url: Api.TestRetry },
    {
      retryRequest: {
        isOpenRetry: true,
        count: 5,
        waitTime: 1000,
      },
    },
  );
}
