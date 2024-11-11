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

    productId: string; // link id which was clicked to trigger form filling
    pageId: string;
    parentPageId: string | undefined;
  }


  export interface OfferRequest {
    pageId: string; // article.id = human readable articale name
    parentPageId: string | undefined;

    productId: string; // links technical id, linked to workflow
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