
import RadioGroupPopover from './RadioGroupPopover';
import { TaskApi } from '@/api-task'

interface Props {
  label: string
  readonly?:boolean
  value: TaskApi.TaskPriority | undefined
  handleCallback: (newValue: TaskApi.TaskPriority) => void;
}

export const Priority = ({ label, readonly, value, handleCallback }: Props) => {
  return (
    <RadioGroupPopover 
      label={label}
      readonly={readonly}
      messages={TaskApi.task_priority_messages}
      colorMap={TaskApi.task_priority_colors}
      value={value}
      handleCallback={newValue => handleCallback(newValue as TaskApi.TaskPriority)}
    />
  );
}
