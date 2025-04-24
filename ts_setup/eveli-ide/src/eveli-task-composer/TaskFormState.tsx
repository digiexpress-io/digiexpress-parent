import { useIam } from "@/api-iam";
import { TaskApi } from "@/api-task";
import React from 'react';
import { useIntl } from "react-intl";

export interface TaskFormProps {
  clientIdentificator: string | undefined
  priority: TaskApi.TaskPriority  | undefined,
  subject: string  | undefined,
  description: string | undefined,
  dueDate: Date | undefined | null,
  status: TaskApi.TaskStatus | undefined,
  assignedUser:  string | undefined,
  assignedUserEmail:  string | undefined,
  assignedRoles: string[] | undefined,
  additionalInfo: string | undefined,
}


function useSubjectErrors(state: TaskFormProps): undefined | string {
  const intl = useIntl();
  if(!state.subject) {
    return intl.formatMessage({id: 'error.valueRequired'})
  }
  if(state.subject.length < 3) {
    return intl.formatMessage({id: 'error.minTextLength'}, { minLength: 3 })
  }
}

function useAssignedUserErrors(state: TaskFormProps): undefined | string {
  const intl = useIntl();
  if (!state.assignedUser && state.status === 'OPEN') {
    return intl.formatMessage({id: 'error.statusOpenError'})
  }
}

export interface TaskFormDelegateProps {
  currentState: TaskFormProps;
  isSubmitting: boolean;
  isValid: boolean;
  dirty: boolean;
  errors: {
    subject: string | undefined,
    assignedUser: string | undefined
  };
  onSubmit: () => Promise<void>;
  setFieldValue<FieldName extends keyof TaskFormProps>(fieldName: FieldName, fieldValue: TaskFormProps[FieldName]): void;
}

export const TaskFormState: React.FC<{
  task: Partial<TaskApi.Task> | null | undefined;
  children: React.FC<TaskFormDelegateProps>;
  onSubmit: (task: Partial<TaskApi.Task>) => Promise<void>;
}> = (props) => {

  const { user: currentUser } = useIam();  
  const [isSubmitting, setSubmitting] = React.useState<boolean>(false);
  


  const initState = React.useMemo<TaskFormProps>(() => ({
    clientIdentificator: props.task?.clientIdentificator || '',
    additionalInfo: props.task?.additionalInfo || '',
    priority: props.task?.priority,
    subject: props.task?.subject || '',
    description: props.task?.description || '',
    dueDate: props.task?.dueDate,
    status: props.task?.status,
    assignedUser: props.task?.assignedUser || '',
    assignedUserEmail: props.task?.assignedUserEmail || '',
    assignedRoles: props.task?.assignedRoles || []
  }), []);
  
  const [form, setForm] = React.useState<TaskFormProps>(initState);
  const subjectErrors = useSubjectErrors(form);
  const assignedUserErrors = useAssignedUserErrors(form);

  const setFieldValue = React.useCallback(function<FieldName extends keyof TaskFormProps>(fieldName: FieldName, fieldValue: TaskFormProps[FieldName]) {
    setForm(prev => {
      const next = {...prev};
    
      if (fieldName === 'status' && fieldValue === "OPEN" && (prev.status === "NEW" || prev.status === undefined)) {
        next.assignedUser = currentUser.name || "";
        next.assignedUserEmail = currentUser.email || "";
      }

      next[fieldName] = fieldValue;
      return next;
    });
  }, [])


  async function handleSubmitForm() {
    setSubmitting(true)

    const taskFromValues: Partial<TaskApi.Task> = {
      id: props.task?.id,
      version: props.task?.version,
      keyWords: props.task?.keyWords,

      priority: form.priority,
      subject: form.subject,
      description: form.description,
      dueDate: form.dueDate as Date | undefined,
      status: form.status,
      assignedUser: form.assignedUser,
      assignedUserEmail: form.assignedUserEmail,
      clientIdentificator: form.clientIdentificator,
      assignedRoles: form.assignedRoles,
      additionalInfo: form.additionalInfo,
    }

    await props.onSubmit(taskFromValues);
  }

  const errors = {
    subject: subjectErrors,
    assignedUser: assignedUserErrors
  };

  const dirty = JSON.stringify(form) !== JSON.stringify(initState);
  const isValid = Object.values(errors).filter(isInvalid => !!isInvalid).length === 0;
  const Mess: React.FC<TaskFormDelegateProps> = props.children;

  return (<Mess 
    currentState={form} 
    dirty={dirty} 
    isSubmitting={isSubmitting} 
    isValid={isValid} 
    errors={errors}
    setFieldValue={setFieldValue}
    onSubmit={handleSubmitForm} />)
}
