import { requestClient } from '#/api/request';

export interface Option {
  label: string;
  value: string;
}

/** 用户下拉项（取较大页，覆盖常规规模；按需可换搜索接口） */
export async function getUserOptions(): Promise<Option[]> {
  const res: any = await requestClient.get('/system/user', {
    params: { page: 1, pageSize: 500 },
  });
  const items = res?.items ?? res?.records ?? [];
  return items.map((u: any) => ({
    label: u.nickname || u.name || u.username,
    value: u.id,
  }));
}

/** 角色下拉项（复用系统 /system/role/select，返回 {label,value}） */
export async function getRoleOptions(): Promise<Option[]> {
  const res: any = await requestClient.get('/system/role/select');
  return Array.isArray(res) ? res : (res?.items ?? []);
}

export interface TreeOption {
  value: string;
  title: string;
  children?: TreeOption[];
}

/** 部门树（用于 TreeSelect） */
export async function getDeptTree(): Promise<TreeOption[]> {
  const res: any = await requestClient.get('/system/dept');
  const list = Array.isArray(res) ? res : (res?.items ?? []);
  const map = (nodes: any[]): TreeOption[] =>
    (nodes || []).map((n) => ({
      value: n.id,
      title: n.name,
      children: n.children?.length ? map(n.children) : undefined,
    }));
  return map(list);
}

/** 把部门 ID 映射成名称（用于回显已选） */
export function flattenDeptNames(tree: TreeOption[]): Record<string, string> {
  const out: Record<string, string> = {};
  const walk = (nodes: TreeOption[]) => {
    nodes.forEach((n) => {
      out[n.value] = n.title;
      if (n.children) walk(n.children);
    });
  };
  walk(tree);
  return out;
}
