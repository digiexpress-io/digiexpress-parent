
export namespace FeedbackApi {

}

export declare namespace FeedbackApi {

  export type FetchTemplateGET = (id: TaskId) => Promise<Response>;
  export type FetchFeedbackPOST = (id: TaskId, command: CreateFeedbackCommand) => Promise<Response>;
  export type FetchFeedbackPUT = (id: TaskId, command: ModifyOneFeedbackCommand) => Promise<Response>;
  export type FetchFeedbackGET = (id?: TaskId) => Promise<Response>;
  export type FetchFeedbackDELETE = (id?: TaskId) => Promise<Response>;

  export type ProcessId = string;
  export type UserId = string;
  export type FeedbackId = string;
  export type SourceId = string;
  export type TaskId = string;
  export type ReplyId = string;
  export type CategoryId = string;
  export type CustomerId = string;


  export interface Feedback {
    id: FeedbackId;
    sourceId: SourceId;

    origin: string;
    content: string;
    replyText: string;

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

  interface FeedbackRating {
    id: string;
    replyId: ReplyId | undefined;
    categoryId: CategoryId;
    customerId: string; //obscure id for customer, should not be able to identify the person
    rating: number; // score 1-5
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
    reply: string;
    locale: string;

    labelKey: string;
    labelValue: string;
    subLabelKey?: string | undefined;
    subLabelValue?: string | undefined;
  }

  export interface ModifyOneFeedbackCommand { }
  export interface ModifyOneFeedbackReplyCommand extends ModifyOneFeedbackCommand {
    id: string;
    commandType: 'MODIFY_ONE_FEEDBACK_REPLY';
    reply: string;
  }

  export interface UpsertFeedbackRankingCommand extends ModifyOneFeedbackCommand {
    replyIdOrCategoryId: string;
    rating: number | undefined; // undefined = remove vote
  }


}
