import { DateTime } from "luxon";

import { mapToOffer } from './mappers'
import { SiteApi } from "../api-site";


export namespace OfferApi {
  export const mapper = mapToOffer;
}

export declare namespace OfferApi {
  export type OfferId = string;
  export type OfferSortOrder = 'ASC' | 'DESC';

  export interface Offer {
    id: OfferId;
    name: string;
    created: DateTime;
    updated: DateTime;

    formUri: string;
    formId: string | undefined;

    productId: string; // link id which was clicked to trigger form filling
    pageId: string;
    parentPageId: string | undefined;

    assigned: boolean;
    
    otherLocales: {
      locale: string;
      productId: string;
    }[]
  }


  export interface OfferRequest {
    pageId: string; // article.id = human readable articale name
    parentPageId: string | undefined;

    productId: string; // links technical id, linked to workflow
    locale: string;
  }


  export type CreateOfferFetchPOST = (request: OfferRequest, cockpitId: string | undefined) => Promise<Response>;
  export type GetOffersFetchGET = (cockpitId: string | undefined) => Promise<Response>;
  export type GetOfferFetchGET = (offerId: OfferId) => Promise<Response>;
  export type CancelOfferFetchDELETE = (request: Offer) => Promise<Response>;


  export interface OfferContextType {
    offers: readonly Offer[];
    isPending: boolean;
    createOffer: (request: OfferRequest) => Promise<Offer>;
    cancelOffer: (offerId: OfferId) => Promise<void>;
    fetchOffer: (offerId: OfferId) => Promise<Offer>;

    toggleOfferSortOrder(): void;
    sortOrder: OfferSortOrder;

    getOffer(offerId: OfferId): Offer | undefined;
    getLocalisedOfferName: (site: SiteApi.Site, workflowName: string) => string;
    refresh(): Promise<void>;
  }
}