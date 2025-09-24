export namespace LegacyProcessApi {

  export interface Process {
    id: string;
    name: string;
    created: string;  // "2023-05-15T08:51:40.380479"
    updated: string;  // "2023-05-15T08:51:40.380479"


    inputContextId: string;
    inputParentContextId: string;
    taskId?: string | undefined;
    taskRef?: string | undefined;
    // process status
    status: (
      'CREATED'  // When a new process is created. Process contains information about wrench workflow, status, info about task and dialob questionnaire id
      | 'ANSWERED' // When dialob sends a completion message, then process' status is set to answered.  
      | "COMPLETED" // When the task connected to the process is set to completed or rejected status.
      | "REJECTED"
      | "WAITING");

    // task status
    taskStatus?: string
    | 'NEW' // task worker has not started working on the task
    | 'OPEN' // task worker has started working on the task
    | "COMPLETED"  // task worker has completed work
    | "REJECTED" // task worker has completed work and rejected the task
    | "TRANSFERRED"
    | "DELEGATED" // task is delegated to external provider and completed
    | "WAITING";
    taskCreated?: string;
    taskUpdated?: string;
    reviewUri: string;
    formId: string;
    formUri: string;
    formInProgress: boolean;
    viewed: boolean;
    messages: ProcessComment[];
    attachments: ProcessAttachment[];
    subActions: { formInProgress: boolean, id: string, formId: string }[];
    messagesUri: string;
    assigned: boolean;
  }

  export interface ProcessAttachment {
    id: string;
    name: string;
    created: string;
    size: number
  }
  export interface ProcessComment {
    id: string,
    created: string,
    replyToId?: string,
    commentText: string,
    userName: string,
    taskId: string,
  }
}