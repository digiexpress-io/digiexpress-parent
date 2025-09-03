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
    REJECTED = 'REJECTED',
    TRANSFERRED = 'TRANSFERRED',
    DELEGATED = 'DELEGATED',
    WAITING = 'WAITING'
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
    TRANSFERRED: {
      id: 'task.status.transferred',
      defaultMessage: 'Transferred',
    },    
    DELEGATED: {
      id: 'task.status.delegated',
      defaultMessage: 'Delegated',
    },
    WAITING: {
      id: 'task.status.waiting',
      defaultMessage: 'Waiting',
    },
  });

  export const task_status_colors: ColorMap = {
    NEW: Colors.YELLOW,
    OPEN: Colors.BLUE,
    COMPLETED: Colors.GREEN,
    REJECTED: Colors.RED,
    TRANSFERRED: Colors.GREY,
    DELEGATED: Colors.GREY,
    WAITING: Colors.GREY
  };
}

export declare namespace TaskApi {

  export interface Attachment {
    name: string;
    status: 'OK' | 'QUARANTINED' | 'UPLOADED';
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
    replyToId?: number | null
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
    id: string;
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
    additionalInfo?: string;
    transferredId?: string | undefined;
    transferredProps?: object | undefined;

    features?: TaskFeatureType[];
    keyWords?: string[];
    taskLinks?: TaskLink[];
    // For UI purposes
    statusCode?: number;
    priorityCode?: number;
    clientIdentificator?: string | null;
    assignedRoles?: string[] | null;

    comments: Comment[];
    questionnaireId?: string | undefined;
  }

  export type TaskFeatureType = 'feedback' | 'transfer' | 'anon';

  export interface TransferTaskCommand {
    transferTitle: string;
  }

  export interface TaskDasboard {
    events: GrimMissionAttributeEvent[];
  }

  export interface GrimMissionAttributeEvent {
    eventDate: string | undefined;
    eventCount: number;
    eventType: GrimMissionAttributeEventType;
    eventSubType: string | undefined;
    attributeValue: string;
  }

  export interface User {
    userName: string;
    userEmail: string;
  }

  export interface Role {
    id: string;
    groupName: string;
  }

  export type TaskAuditEntryType = 'DIFF' | 'MQ' | 'FLOW' | 'VIEWER'

  export interface TaskViewer {
    id: string
    updatedAt: string
    usedBy: string         // userId-- could be human readable id or not
  }

  export interface TaskCommit {
    commitId: string;
    createdAt: string;
    commitAuthor: string;
    commitMessage: string;
  }

  export interface TaskAuditLog {
    id: string;
    flow: TaskAuditEntryProcess | undefined;
    mq: TaskAuditEntryMq | undefined;
    access: TaskAuditEntryAccess;
  }

  export interface TaskAuditEntryProcess {
    processInstance: any;                            //ProcessClient.ProcessInstance;
    processFlowLog: object | undefined;
    processFormLog: object | undefined;
    type: 'FLOW';
  }

  export interface TaskAuditEntryMq {
    deliveries: Record<string, any>;                 //Map<String, Delivery> getDeliveries();
    bindings: Record<string, any>;                   //Map<String, Binding> getBindings();
    messages: Record<string, any>;                   //Map<String, QueueMessage> getQueueMessages();
    queues: Record<string, any>;                     //Map<String, Queue> getQueues();
    queueConsumers: Record<string, any>;             //Map<String, QueueConsumer> getQueueConsumers();
    channels: Record<string, any>;                   //Map<String, Channel> getChannels();
    type: 'MQ'
  }

  export interface TaskAuditEntryDiff {
    value: Record<string, any>                       //Map<String, TaskDiff> getValue();
    type: 'DIFF'
  }

  export interface TaskAuditEntryAccess {
    value: TaskViewer[];                              //List<GrimCommitViewer> getValue();
    commits: Record<string, TaskCommit>;              //Map<String, GrimCommit> getCommits();
    commitTrees: Record<string, any>;                 //Map<String, GrimCommitTree> getCommitTrees();
    type: 'VIEWER'
  }

  export type GrimMissionAttributeEventType = ('STATUS' | 'PRIORITY' | 'STATUS_DATE' | 'OVERDUE' | 'ROLE' | 'QUESTIONNAIRE');
}
