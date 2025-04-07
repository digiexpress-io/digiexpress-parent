
import RadioGroupPopover from './RadioGroupPopover';
import { TaskApi } from '@/api-task';


interface Props {
  label: string
  readonly?: boolean
  value: TaskApi.TaskStatus | undefined
  handleCallback: (newValue: TaskApi.TaskStatus) => void;
}

export const StatusComponent =({label, readonly, handleCallback, value}:Props) =>{
 return (
  <RadioGroupPopover 
    label={label}
    readonly={readonly}
    messages={TaskApi.task_status_messages}
    colorMap={TaskApi.task_status_colors}
    handleCallback={newValue => handleCallback(newValue as TaskApi.TaskStatus)}
    value={value}
  />
 );
}

