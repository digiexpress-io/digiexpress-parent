import { defineMessages } from "react-intl";

export namespace TaskApi {

  export enum Colors {
    RED = 1,
    BLUE,
    GREEN,
    YELLOW,
    GREY
  }

  export type ColorMap = {
    [status: string]: Colors
  }

  export enum CommentSource {
    FRONTDESK = "FRONTDESK",
    PORTAL = "PORTAL"
  }


  export enum TaskPriority {
    LOW = 'LOW',
    NORMAL = 'NORMAL',
    HIGH = 'HIGH',
  }
  export const task_priority_messages = defineMessages({
    LOW: {
      id: 'task.priority.low',
      defaultMessage: 'Low',
    },
    NORMAL: {
      id: 'task.priority.normal',
      defaultMessage: 'Normal',
    },
    HIGH: {
      id: 'task.priority.high',
      defaultMessage: 'High',
    },
  });

  export const task_priority_colors: ColorMap = {
    LOW: Colors.GREEN,
    NORMAL: Colors.BLUE,
    HIGH: Colors.RED,
  };




  export enum TaskStatus {
    NEW = 'NEW',
    OPEN = 'OPEN',
    COMPLETED = 'COMPLETED',
    REJECTED = 'REJECTED'
  }
  export const task_status_messages = defineMessages({
    NEW: {
      id: 'task.status.new',
      defaultMessage: 'New',
    },
    OPEN: {
      id: 'task.status.open',
      defaultMessage: 'Open',
    },
    COMPLETED: {
      id: 'task.status.completed',
      defaultMessage: 'Completed',
    },
    REJECTED: {
      id: 'task.status.rejected',
      defaultMessage: 'Rejected',
    },
  });

  export const task_status_colors: ColorMap = {
    NEW: Colors.YELLOW,
    OPEN: Colors.BLUE,
    COMPLETED: Colors.GREEN,
    REJECTED: Colors.GREY,
  };
  

}

export declare namespace TaskApi {

  export interface Attachment {
    name: string;
    status: 'OK'|'QUARANTINED'|'UPLOADED';
    created: Date;
    updated: Date;
    size: number;
  }
  
  export interface AttachmentUploadResponse {
    putRequestUrl: string
  }


  export interface Comment {
    id: number
    userName: string
    created: string
    commentText: string
    replyToId?: number|null
    // added in UI for hierarchical display
    __parent?: Comment
    __children?: Comment[]
    external?: boolean
    source?: CommentSource
  }

  export type TaskLink = {
    id?: number
    linkKey: string
    linkAddress: string
  }


  export interface Task {
    id?: string;
    taskRef?: string;
    version?: number;
    created?: Date;
    updated?: Date;
    completed?: Date;
    assignedUser?: string;
    assignedUserEmail?: string;
    updaterId?: string;
    dueDate?: Date;
    status?: TaskStatus;
    subject?: string;
    description?: string;
    priority?: TaskPriority;
    keyWords?: string[];
    taskLinks?: TaskLink[];
    // For UI purposes
    statusCode?: number;
    priorityCode?: number;
    clientIdentificator?: string|null;
    assignedRoles?: string[]|null;

    questionnaireId?: string | undefined;
  }


  export type TaskPriorityStatistics = {
    count: number
    priority: TaskApi.TaskPriority
  }
  
  export type TaskStatusStatistics = {
    count: number
    status: TaskApi.TaskStatus
  }
  
  export type OverdueByGroupStatistics = {
    count: number
    assignedId: string
  }
  
  export type TaskStatusTimelineStatistics = {
    statusDate: Date
    new: number
    open: number
    completed: number
    rejected: number
  }
}