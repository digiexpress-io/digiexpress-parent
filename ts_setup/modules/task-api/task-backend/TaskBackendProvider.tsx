
import React from 'react';
import { TaskApi } from '../task-types';

export interface TaskBackendContextType {
  deps: any[];
  currentUser: {
    name: string;
    email: string;
  },
  roles: TaskApi.Role[];
  permissions: {
    isCreateTaskAllowed: boolean;
    isReopenTaskAllowed: boolean;
    isDeleteTaskAllowed: boolean;
    isRetransferAllowed: boolean;
  };
  features: {
    isAuditTaskEnabled: boolean;
  },
  navigate: {
    findAllTasks: () => void;
    createOneTask: () => void;
    openOneTask: (taskIdOrRef: string) => void;
  };
  persistence: {
    findAllUsers: (groups: string[]) => Promise<TaskApi.User[]>;
    findAllAttachments: (taskId: string) => Promise<TaskApi.Attachment[]>;

    getOneAttachmentLink: (taskId: string, attachment: TaskApi.Attachment) => Promise<string>
    deleteOneAttachment: (taskId: string, attachment: TaskApi.Attachment) => Promise<unknown>;
    createManyAttachments: (taskId: string, files: FileList) => Promise<unknown>; 
    createOnTaskTransfer: (task: TaskApi.Task, command: TaskApi.TransferTaskCommand) => Promise<TaskApi.Task>;

    findAllTaskFormAssignments: (taskId: string) => Promise<TaskApi.FormAssignment[]>;
    findAllTasks: () => Promise<TaskApi.Task[]>;
    findAllUnreadTasks: () => Promise<string[]>;
    getOneTask: (taskId: string) => Promise<TaskApi.Task>;
    getOneTaskPdf: (pdfRequest: TaskApi.TaskPdfRequest) => Promise<Blob>;
    getOneTaskAudit: (taskId: string) => Promise<TaskApi.TaskAuditLog>;
    modifyOneTask: (newData: TaskApi.Task) => Promise<TaskApi.Task>;
    deleteOneTask: (taskId: string) => Promise<unknown>;
    deleteManyCustomerAssignment: (taskId: string, assignmentId: string[]) => Promise<TaskApi.Task>; 
    createManyTaskCustomerAssignments: (taskId: string, request: TaskApi.CreateTaskCustomerAssignmentCommand[]) => Promise<TaskApi.Task>;
    createOneTask: (request: Partial<TaskApi.Task>) => Promise<TaskApi.Task>;
    createOneComment: (commentText: string, replyToId: number | undefined, task: TaskApi.Task, isExternalThread: boolean | undefined) => Promise<TaskApi.Comment>
  },
  slots: {
    DialobReview: React.ElementType<{ task: { id: string, questionnaireId?: string | undefined }; onClose: () => void }>;
    DialobReviewButton: React.ElementType<{ onClick: () => void; }>;

    DateTimeFormatter: React.ElementType<{ value: string | Date | undefined, variant?: 'text' }>;
    DateTimePicker: React.ElementType<{ 
      label?: string | React.ReactNode,
      readonly?: boolean,
      fullWidth?: boolean,
      value: string | Date | undefined | null;
      onChange?: (newValue: Date | null) => void;
      onKeyDown?: (event: React.KeyboardEvent<HTMLInputElement>) => void;
    }>
  }
}



export const TaskBackendContext = React.createContext<TaskBackendContextType>({} as any);

export interface TaskBackendProviderProps {
  deps: any[];
  children: React.ReactNode;
  currentUser: TaskBackendContextType['currentUser'];
  roles: TaskBackendContextType['roles'];
  navigate: TaskBackendContextType['navigate'];
  slots: TaskBackendContextType['slots'];
  persistence: TaskBackendContextType['persistence'];
  permissions: TaskBackendContextType['permissions'];
  features: TaskBackendContextType['features'];
}

export const TaskBackendProvider: React.FC<TaskBackendProviderProps> = (props) => {
  const { navigate, persistence, permissions, currentUser, roles, slots, features, deps } = props;

  const contextValue: TaskBackendContextType = React.useMemo(() => {
    return { navigate, persistence, permissions, currentUser, roles, slots, features, deps };
  }, [roles, currentUser, deps]);

  return (<TaskBackendContext.Provider value={contextValue}>{props.children}</TaskBackendContext.Provider>);
}

export function useTaskBackend() {
  const result: TaskBackendContextType = React.useContext(TaskBackendContext);
  return result;
}
