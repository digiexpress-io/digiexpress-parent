import { FieldInputProps } from 'formik';
import RadioGroupPopover from './RadioGroupPopover';
import { TaskApi } from '@/api-task'

interface Props extends FieldInputProps<""> {
  label: string
  readonly?:boolean
}

export const Priority = ({ label, readonly, ...props }: Props) => {
  return (
    <RadioGroupPopover 
      label={label}
      readonly={readonly}
      messages={TaskApi.task_priority_messages}
      colorMap={TaskApi.task_priority_colors}
      {...props}
    />
  );
}
