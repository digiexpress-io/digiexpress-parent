
export namespace TaskApi {

  export enum CommentSource {
    FRONTDESK = "FRONTDESK",
    PORTAL = "PORTAL"
  }
  export enum TaskPriority {
    LOW = 'LOW',
    NORMAL = 'NORMAL',
    HIGH = 'HIGH',
  }

  export enum TaskStatus {
    NEW = 'NEW',
    OPEN = 'OPEN',
    COMPLETED = 'COMPLETED',
    REJECTED = 'REJECTED'
  }
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
}