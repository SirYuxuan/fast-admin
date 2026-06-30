import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';

import type { SysJobApi } from '#/api/system/job';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'jobName', label: '任务名称' },
    { component: 'Input', fieldName: 'jobGroup', label: '任务分组' },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: '启动中', value: 1 },
          { label: '暂停', value: 0 },
        ],
      },
      fieldName: 'status',
      label: '状态',
    },
  ];
}

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'jobName',
      label: '任务名称',
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'jobGroup',
      label: '任务分组',
      defaultValue: 'DEFAULT',
    },
    {
      component: 'Input',
      fieldName: 'beanName',
      label: 'Bean 名',
      rules: 'required',
      help: 'Spring 容器中 Bean 的名字，例如 demoJob',
    },
    {
      component: 'Input',
      fieldName: 'methodName',
      label: '方法名',
      defaultValue: 'execute',
      help: '无参方法 或 接收单个 String 参数的方法',
    },
    {
      component: 'Input',
      fieldName: 'methodParams',
      label: '方法参数',
      help: '可选；填了会作为 String 参数传给方法',
    },
    {
      component: 'Input',
      fieldName: 'cronExpression',
      label: 'Cron 表达式',
      rules: 'required',
      help: '示例：0/30 * * * * ? 表示每 30 秒执行一次',
    },
    {
      component: 'Select',
      componentProps: {
        options: [
          { label: '立即执行', value: 1 },
          { label: '忽略', value: 2 },
          { label: '触发一次', value: 3 },
        ],
      },
      defaultValue: 2,
      fieldName: 'misfirePolicy',
      label: '错过策略',
    },
    {
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: '不允许', value: 0 },
          { label: '允许', value: 1 },
        ],
      },
      defaultValue: 0,
      fieldName: 'concurrent',
      label: '并发执行',
    },
    {
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: '暂停', value: 0 },
          { label: '启动', value: 1 },
        ],
      },
      defaultValue: 0,
      fieldName: 'status',
      label: '初始状态',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 2 },
      fieldName: 'remark',
      label: '备注',
    },
  ];
}

export function useColumns<T = SysJobApi.Job>(
  onActionClick: OnActionClickFn<T>,
  onStatusChange?: (newStatus: any, row: T) => PromiseLike<boolean | undefined>,
): VxeTableGridOptions['columns'] {
  return [
    { field: 'jobName', title: '任务名称', minWidth: 160 },
    { field: 'jobGroup', title: '分组', width: 110 },
    { field: 'beanName', title: 'Bean', minWidth: 140 },
    { field: 'methodName', title: '方法', width: 110 },
    { field: 'cronExpression', title: 'Cron', minWidth: 160 },
    {
      field: 'status',
      title: '状态',
      width: 90,
      cellRender: {
        attrs: { beforeChange: onStatusChange },
        name: onStatusChange ? 'CellSwitch' : 'CellTag',
        options: [
          { label: '运行中', value: 1, color: 'success' },
          { label: '暂停', value: 0, color: 'default' },
        ],
      },
    },
    {
      field: 'concurrent',
      title: '并发',
      width: 80,
      formatter: ({ cellValue }) => (cellValue === 1 ? '是' : '否'),
    },
    { field: 'createdAt', title: '创建时间', width: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [
          {
            code: 'run',
            disabled: (row: SysJobApi.Job) => row.__running,
            loading: (row: SysJobApi.Job) => row.__running,
            text: '执行',
          },
          { code: 'log', text: '日志' },
          { code: 'edit' },
          { code: 'delete' },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 280,
    },
  ];
}
