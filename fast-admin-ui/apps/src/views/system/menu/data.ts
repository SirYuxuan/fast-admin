/* eslint-disable @typescript-eslint/ban-ts-comment */
// @ts-ignore
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemMenuApi } from '#/api/system/menu';

export function getMenuTypeOptions() {
  return [
    {
      color: 'processing',
      label: '目录',
      value: 'catalog',
    },
    { color: 'default', label: '菜单', value: 'menu' },
    { color: 'error', label: '按钮', value: 'button' },
    {
      color: 'success',
      label: '内嵌',
      value: 'embedded',
    },
    { color: 'warning', label: '外链', value: 'link' },
  ];
}

export function useColumns(
  onActionClick: OnActionClickFn<SystemMenuApi.SystemMenu>,
): VxeTableGridOptions<SystemMenuApi.SystemMenu>['columns'] {
  return [
    {
      align: 'left',
      field: 'meta.title',
      fixed: 'left',
      slots: { default: 'title' },
      title: '标题',
      treeNode: true,
      width: 250,
    },
    {
      align: 'center',
      cellRender: { name: 'CellTag', options: getMenuTypeOptions() },
      field: 'type',
      title: '类型',
      width: 100,
    },
    {
      field: 'authCode',
      title: '权限标识',
      width: 200,
    },
    {
      align: 'left',
      field: 'path',
      title: '路由地址',
      width: 200,
    },

    {
      align: 'left',
      field: 'component',
      formatter: ({ row }) => {
        switch (row.type) {
          case 'catalog':
          case 'menu': {
            return row.component ?? '';
          }
          case 'embedded': {
            return row.meta?.iframeSrc ?? '';
          }
          case 'link': {
            return row.meta?.link ?? '';
          }
        }
        return '';
      },
      minWidth: 200,
      title: '页面组件',
    },
    {
      cellRender: { name: 'CellTag' },
      field: 'status',
      title: '状态',
      width: 100,
    },

    {
      align: 'right',
      cellRender: {
        attrs: {
          nameField: 'name',
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [
          {
            code: 'append',
            text: '新增下级',
            authCode: 'system:menu:add',
          },
          {
            code: 'edit', // 默认的编辑按钮
            authCode: 'system:menu:edit',
            disabled: (row: SystemMenuApi.SystemMenu) => {
              return row.meta?.title === '工作台';
            },
          },
          {
            code: 'delete', // 默认的删除按钮
            authCode: 'system:menu:delete',
            disabled: (row: SystemMenuApi.SystemMenu) => {
              return (
                !!(row.children && row.children.length > 0) ||
                row.meta?.title === '工作台'
              );
            },
          },
        ],
      },
      field: 'operation',
      fixed: 'right',
      headerAlign: 'center',
      showOverflow: false,
      title: '操作',
      width: 200,
    },
  ];
}
