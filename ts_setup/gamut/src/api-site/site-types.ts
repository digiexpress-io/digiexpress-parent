export namespace SiteApi {

}

export declare namespace SiteApi {
  export type LocaleCode = string;
  export type TopicId = string;
  export type FeedbackId = string;
  export type TopicLinkId = string;
  export type BlobId = string;
  export type TopicLinkType = "phone" | "dialob" | "internal" | "external" | "workflow" | string;

  export type FetchSiteGET = (locale: LocaleCode) => Promise<Response>;
  export type FetchFeedbackGET = (locale: LocaleCode) => Promise<Response>;
  export type FetchFeedbackRatingPUT = (command: UpsertFeedbackRankingCommand) => Promise<Response>;

  // internal stencil data structure - needs conversion
  export interface Site {
    id: string;
    images: string;
    locale: LocaleCode;

    topics: Record<string, Topic>;
    blobs: Record<string, Blob>;
    links: Record<string, TopicLink>;
  }

  export interface Blob {
    id: BlobId;
    value: string;
  }

  export interface Topic {
    id: TopicId;
    name: string;
    links: string[];
    headings: TopicHeading[];
    parent?: string | null;
    blob?: string;
  }

  export interface TopicLink {
    id: TopicLinkId;
    type: TopicLinkType;
    name: string;
    value: string;
    global?: boolean;
    workflow?: boolean;
    secured?: boolean;
    path?: string;
  }

  export interface TopicHeading {
    id: string;
    name: string;
    order: number;
    level: number;
  }

  // to be converted
  /**
    Webstore model
    Product Group
      - 1-1 Product Description = Topic 
      - 0-n Product notes = phones/links
      - 0-n Product = Link(workflow) 
      - 0-* Related products (Product Group)
  */
  export interface TopicGroup {
    column: number;
    topics: TopicView[];
    next: boolean; // are there more items after this one
  }
  export interface TopicView {
    id: TopicId;
    name: string;
    topic: Topic;
    blob?: Blob
    parent?: Topic;
    children: Topic[];

    links: TopicLink[];
    internalExternal: TopicLink[];
    phones: TopicLink[];
    workflows: TopicLink[];
  }

  export interface CustomerFeedback {
    feedback: Feedback;
    rating: FeedbackRating | undefined;
  }
  export interface FeedbackRating {
    id: string;
    replyId: string | undefined;
    categoryId: string;
    customerId: string;
    rating: number; // score 1-5
  }
  export interface Feedback {
    id: FeedbackId;
      
    labelKey: string;
    labelValue: string;
    
    subLabelKey: string | undefined;
    subLabelValue: string | undefined;

    updatedOnDate: string;

    content: string;
    replyText: string;
    locale: string;
    
    thumbsUpCount: number; // round rating to thumbs up
    thumbsDownCount: number; // round rating to thumbs down
  }

  export interface UpsertFeedbackRankingCommand {
    replyIdOrCategoryId: string;
    rating: 1 | 5 | undefined;
  }

}