import { DateTime } from 'luxon';
import { ProductApi } from '../api-product';

export namespace CommsApi {

}

export declare namespace CommsApi {


  export type SubjectId = string;

  export interface Subject {
    id: SubjectId;
    created: DateTime;
    contractId: string; // contract id
    product: ProductApi.Product;
    name: string;
    exchange: readonly Message[]; // all messages, ordered by created asc
    lastExchange?: Message; // THE LAST message chronologically 
    documents: readonly SubjectDocument[];
    isViewed: boolean;
  }

  export interface SubjectDocument {
    id: string;
    name: string;
    created: DateTime;
    size: number
  }

  export interface Message {
    id: string;
    created: DateTime;
    replyToId?: string;
    commentText: string;
    userName: string;
    isMyMessage: boolean;
  }

  export type GetSubjectsFetchGET = () => Promise<Response>;

  export interface CommsContextType {
    subjects: readonly Subject[];
    isPending: boolean;
    subjectStats: { exchanges: number },
    getSubject(contractId: SubjectId): Subject | undefined;
    refresh(): Promise<void>;
  }
}