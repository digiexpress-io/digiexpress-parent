import { TaskApi } from '@dxs-ts/task-api';

// Map TaskApi.Colors enum -> hex (aligned with composer v2)
export const ApiColorHex: Record<TaskApi.Colors, string> = {
  [TaskApi.Colors.RED]:    '#f44336',
  [TaskApi.Colors.BLUE]:   '#2196f3',
  [TaskApi.Colors.GREEN]:  '#4caf50',
  [TaskApi.Colors.YELLOW]: '#ffeb3b',
  [TaskApi.Colors.GREY]:   '#9e9e9e',
};

// Priority value -> hex using the API’s color enum
export const PriorityHex = Object.fromEntries(
  Object.entries(TaskApi.task_priority_colors).map(([k, apiColor]) => [
    k, ApiColorHex[apiColor as TaskApi.Colors]
  ])
) as Record<TaskApi.TaskPriority, string>;

// Status value -> hex using the API’s color enum
export const StatusHex = Object.fromEntries(
  Object.entries(TaskApi.task_status_colors).map(([k, apiColor]) => [
    k, ApiColorHex[apiColor as TaskApi.Colors]
  ])
) as Record<TaskApi.TaskStatus, string>;

export const getContrastText = (hex: string): string => {
  const c = hex.replace('#', '');
  const rgb = parseInt(c, 16);
  const r = (rgb >> 16) & 0xff, g = (rgb >> 8) & 0xff, b = rgb & 0xff;
  const luminance = (0.299*r + 0.587*g + 0.114*b) / 255;
  return luminance > 0.6 ? '#000000' : '#ffffff';
};
