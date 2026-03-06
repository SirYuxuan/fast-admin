/**
 * 用户性别枚举
 */
export enum SysUserSex {
  /** 女 */
  FEMALE = 2,
  /** 男 */
  MALE = 1,
  /** 未知 */
  UNKNOWN = 0,
}

/**
 * 用户状态枚举
 */
export enum SysUserStatus {
  /** 冻结 */
  FROZEN = 1,
  /** 锁定 */
  LOCKED = 2,
  /** 正常 */
  NORMAL = 0,
}

/**
 * 系统用户接口
 */
export interface ModelType {
  [key: string]: any;
  /** 用户ID */
  id: string;
  /** 部门ID */
  deptId: string;
  /** 部门名称 */
  deptName: string;
  /** 用户名 */
  username: string;
  /** 邮箱 */
  email: string;
  /** 手机号 */
  phone: string;
  /** 昵称 */
  nickname: string;
  /** 性别：1男，2女，0未知 */
  sex: SysUserSex;
  /** 状态：0正常，1冻结，2锁定 */
  status: SysUserStatus;
  /** 最后登录IP */
  loginIp: string;
  /** 最后登录城市 */
  loginCity: string;
  /** 最后登录时间 */
  loginTime: string;
  /** 创建时间 */
  createTime: string;
}
