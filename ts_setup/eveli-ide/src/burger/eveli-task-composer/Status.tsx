import { FieldInputProps } from 'formik';
import RadioGroupPopover from './RadioGroupPopover';
import { TaskApi } from '@/burger';


interface Props extends FieldInputProps<""> {
  label: string
  readonly?:boolean
  handleCallback?: (newValue: string) => void;
}

export const StatusComponent =({label, readonly, handleCallback,  ...props}:Props) =>{
 return (
  <RadioGroupPopover 
    label={label}
    readonly={readonly}
    messages={TaskApi.task_status_messages}
    colorMap={TaskApi.task_status_colors}
    handleCallback={handleCallback}
    {...props}
  />
 );
}

