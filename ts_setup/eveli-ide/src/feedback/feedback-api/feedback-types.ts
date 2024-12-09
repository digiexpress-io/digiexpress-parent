
export namespace FeedbackApi {

}

export declare namespace FeedbackApi {

  export type FetchTemplateGET = (id: TaskId) => Promise<Response>;
  export type FetchFeedbackPOST = (id: TaskId, command: CreateFeedbackCommand) => Promise<Response>;
  export type FetchFeedbackGET = (id?: TaskId) => Promise<Response>;

  export type ProcessId = string;
  export type UserId = string;
  export type FeedbackId = string;
  export type SourceId = string;
  export type TaskId = string;


  export interface Feedback {
    id: FeedbackId;
    sourceId: SourceId;

    origin: string;
    content: string;
    locale: string;

    labelKey: string;
    labelValue: string;
    subLabelKey: string | undefined;
    subLabelValue: string | undefined;

    updatedBy: string;
    updatedOnDate: string;
    createdBy: string;

    thumbsUpCount: number;
    thumbsDownCount: number;
  }

  export interface FeedbackTemplate {
    processId: ProcessId;
    userId: UserId;

    origin: string;
    content: string;
    locale: string;

    labelKey: string;
    labelValue: string;
    subLabelKey: string | undefined;
    subLabelValue: string | undefined;

    replys: string[];
    questionnaire: {
      metadata: {
        label: string;
        completed: string;
      }
    };
  }

  export interface CreateFeedbackCommand {
    processId: ProcessId;
    userId: UserId;

    origin: string;
    content: string;
    locale: string;

    labelKey: string;
    labelValue: string;
    subLabelKey?: string | undefined;
    subLabelValue?: string | undefined;
  }
}
