
import React from 'react';
import { TaskApi } from '../task-types';

export interface TaskBackendContextType {
  currentUser: {
    name: string;
    email: string;
  },
  roles: TaskApi.Role[];
  permissions: {
    isCreateTaskAllowed: boolean;
    isReopenTaskAllowed: boolean;
    isDeleteTaskAllowed: boolean;
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

    
    paginateTasks: (queryProps: string) => Promise<{
      data: TaskApi.Task[], // array of data
      page: number, // current page we are on, starts with 0 = first page
      totalCount: number // total entries on all the pages combined
    }>;

    findAllTasks: () => Promise<TaskApi.Task[]>;
    findAllUnreadTasks: () => Promise<string[]>;
    getOneTask: (taskId: string) => Promise<TaskApi.Task>;
    getOneTaskPdfLink: (questionnaireId: string, taskId: string) => Promise<string>;
    getOneTaskAudit: (taskId: string) => Promise<TaskApi.TaskAuditLog>;
    modifyOneTask: (newData: TaskApi.Task) => Promise<TaskApi.Task>;
    deleteOneTask: (taskId: string) => Promise<unknown>;
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
  const { navigate, persistence, permissions, currentUser, roles, slots, features } = props;

  const contextValue: TaskBackendContextType = React.useMemo(() => {
    return { navigate, persistence, permissions, currentUser, roles, slots, features };
  }, [roles, currentUser]);

  return (<TaskBackendContext.Provider value={contextValue}>{props.children}</TaskBackendContext.Provider>);
}

export function useTaskBackend() {
  const result: TaskBackendContextType = React.useContext(TaskBackendContext);
  return result;
}
