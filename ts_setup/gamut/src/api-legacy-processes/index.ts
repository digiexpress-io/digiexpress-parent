export namespace LegacyProcessApi {

  export interface Process {
    id: string;
    name: string;
    created: string;  // "2023-05-15T08:51:40.380479"
    updated: string;  // "2023-05-15T08:51:40.380479"

    inputContextId: string;
    inputParentContextId: string;
    taskId?: string | undefined;
    // process status
    status: (
      'CREATED'  // When a new process is created. Process contains information about wrench workflow, status, info about task and dialob questionnaire id
      | 'ANSWERED' // When dialob sends a completion message, then process' status is set to answered.  
      | "COMPLETED" // When the task connected to the process is set to completed or rejected status.
      | "REJECTED");

    // task status
    taskStatus?: string
    | 'NEW' // task worker has not started working on the task
    | 'OPEN' // task worker has started working on the task
    | "COMPLETED"  // task worker has completed work
    | "REJECTED"; // task worker has completed work and rejected the task
    taskCreated?: string;
    taskUpdated?: string;
    reviewUri: string;
    formId: string;
    formUri: string;
    formInProgress: boolean;
    viewed: boolean;
    messages: ProcessComment[];
    attachments: ProcessAttachment[];
    messagesUri: string;
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