import { DateTime } from "luxon";


import {
  mapToOffer
} from './mappers'


export namespace OfferApi {
  export const mapper = mapToOffer;
}

export declare namespace OfferApi {
  export type OfferId = string;

  export interface Offer {
    id: OfferId;
    name: string;
    created: DateTime;
    updated: DateTime;

    formUri: string;
    formId: string;

    //productId: string;
    productGroupId: string;
  }


  export interface OfferRequest {
    productGroupId: string;
    productId: string;
    locale: string;
  }


  export type CreateOfferFetchPOST = (request: OfferRequest) => Promise<Response>;
  export type GetOffersFetchGET = () => Promise<Response>;
  export type CancelOfferFetchDELETE = (request: Offer) => Promise<Response>;

  export interface OfferContextType {
    offers: readonly Offer[];
    isPending: boolean;
    createOffer: (request: OfferRequest) => Promise<Offer>;
    cancelOffer: (offerId: OfferId) => Promise<void>;
    getOffer(offerId: OfferId): Offer | undefined;
    refresh(): Promise<void>;
  }
}