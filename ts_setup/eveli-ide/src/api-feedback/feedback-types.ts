
export namespace FeedbackApi {

}

export declare namespace FeedbackApi {

  export type ProcessId = string;
  export type UserId = string;
  export type FeedbackId = string;
  export type SourceId = string;
  export type TaskId = string;
  export type ReplyId = string;
  export type CategoryId = string;
  export type CustomerId = string;



  export interface FeedbackTopic {
    main: FeedbackTopicItem[];
    sub: FeedbackTopicItem[];
  }

  export interface FeedbackTopicItem {
    labelKey: string;
    labelValue: string;
  }

  export interface FeedbackContent {
    origin: string;
    customerTitle: string | undefined;
    labelKey: string;
    subLabelKey: string | undefined;
    labelValue: string;
    subLabelValue: string | undefined;

    locale: string;

    content: {
      title: string;
      main: string | undefined;
      sub: string | undefined;
      question: string | undefined;
    }
  }

  export interface Feedback extends FeedbackContent {
    id: FeedbackId;
    sourceId: SourceId;
    replyText: string;

    updatedBy: string;
    updatedOnDate: string;
    createdBy: string;

    thumbsUpCount: number;
    thumbsDownCount: number;
  }
  export interface FeedbackTemplate extends FeedbackContent {
    processId: ProcessId;
    taskId: TaskId;
    userId: UserId;
    replys: string[];
    questionnaire: {
      metadata: {
        label: string;
        completed: string;
      }
    };
  }



  export interface FeedbackRating {
    id: string;
    replyId: ReplyId | undefined;
    categoryId: CategoryId;
    customerId: string; //obscure id for customer, should not be able to identify the person
    rating: number; // score 1-5
  }


  export interface CreateFeedbackCommand {
    processId: ProcessId;
    taskId: TaskId;
    userId: UserId;

    origin: string;
    content: {
      title: string;
      main: string | undefined;
      sub: string | undefined;
      question: string | undefined;
    };
    reply: string;
    question: string;
    locale: string;

    labelKey: string;
    labelValue: string;
    subLabelKey?: string | undefined;
    subLabelValue?: string | undefined;
    customerTitle: string | undefined;
 
  }

  export interface ModifyOneFeedbackCommand { }
  export interface ModifyOneFeedbackReplyCommand extends ModifyOneFeedbackCommand {
    id: string;
    commandType: 'MODIFY_ONE_FEEDBACK_REPLY';
    reply: string;
    question: string;

    labelKey: string;
    labelValue: string;
    subLabelKey?: string | undefined;
    subLabelValue?: string | undefined;

    customerTitle: string;
  }

  export interface UpsertFeedbackRankingCommand extends ModifyOneFeedbackCommand {
    replyIdOrCategoryId: string;
    rating: number | undefined; // undefined = remove vote
  }


}
