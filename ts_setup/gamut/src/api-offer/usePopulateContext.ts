import React from 'react';
import { useQuery } from '@tanstack/react-query'
import { OfferApi } from './offer-types';
import { LegacyProcessApi } from '../api-legacy-processes';
import { mapToOffer, mapToOfferData, toOtherTopicLinkLocales } from './mappers';
import { SiteApi, useSite } from '../api-site';
import { useAssertAuthentication, assertAuthenticatedResponse } from '../api-iam';



export interface UsePropulateProps {
  options: { staleTime: number, queryKey: string };
  createOffer: OfferApi.CreateOfferFetchPOST;
  getAllOffers: OfferApi.GetOffersFetchGET;
  getOneOffer: OfferApi.GetOfferFetchGET;
  cancelOffer: OfferApi.CancelOfferFetchDELETE;
}

export interface PopulateOfferContext {
  offers: readonly OfferApi.Offer[];
  isPending: boolean;
  createOffer: (request: OfferApi.OfferRequest) => Promise<OfferApi.Offer>;
  cancelOffer: (offerId: string) => Promise<void>;
  fetchOffer: (offerId: string) => Promise<OfferApi.Offer>;
  getLocalisedOfferName: (site: SiteApi.Site, workflowName: string | undefined) => string;
  refresh(): Promise<void>;
}

export function usePopulateContext(props: UsePropulateProps): PopulateOfferContext {
  const { site } = useSite();
  const [isInitialLoadDone, setInitialLoadDone] = React.useState(false);
  const { getAllOffers, getOneOffer, options } = props;
  const { staleTime, queryKey } = options;

  // tanstack query config
  const { data: processes, error, refetch, isPending } = useQuery({
    staleTime,
    queryKey: [queryKey],
    queryFn: () => getAllOffers()
      .then(data => {
        assertAuthenticatedResponse(data);
        return data.json();
        
      })
      .then((data: LegacyProcessApi.Process[]) => data)
  });

  useAssertAuthentication(error);
  

  // Get the offer (form) name based on the topic link
  const getLocalisedOfferName = (site: SiteApi.Site, workflowName: string | undefined): string => {
    const link = Object.values(site.links).find(link => link.value === workflowName);
    if(!link && workflowName) { 

      const others = toOtherTopicLinkLocales(site, workflowName);

      // will ontly happen if there is no localization
      if(others.length === 0) {
        return workflowName;
      }

      const [{product}] = others;
      if(product) {
        return product.name;
      }
    }
    return link ? link.name : '-';
  };


  // Create new offer and reload after that
  const createOffer: (request: OfferApi.OfferRequest) => Promise<OfferApi.Offer> = React.useCallback(async (request) => {
    const newOffer: OfferApi.Offer = await props.createOffer(request).then(resp => resp.json()).then(data => mapToOffer(data, site));
    return refetch().then(() => newOffer);
  }, [refetch, props.createOffer, site]);


  // Create new offer and reload after that
  const fetchOffer: (request: OfferApi.OfferId) => Promise<OfferApi.Offer> = React.useCallback(async (request) => {
    const newOffer: OfferApi.Offer = await props.getOneOffer(request).then(resp => resp.json()).then(data => mapToOffer(data, site));
    return newOffer;
  }, [props.getOneOffer, site]);

  // Reload all data
  const refresh: () => Promise<void> = React.useCallback(async () => {
    return refetch().then(() => { });
  }, [refetch]);




  // track initial loading
  React.useEffect(() => {
    if (isInitialLoadDone) {
      return;
    }
    if (processes) {
      setInitialLoadDone(true);
    }
  }, [isInitialLoadDone, processes]);

  const isContextLoaded = (isInitialLoadDone || !isPending);
  const offerData = mapToOfferData(processes ?? [], site);

    // Cancel offer
    const cancelOffer: (offerId: OfferApi.OfferId) => Promise<void> = React.useCallback(async (offerId) => {
      const offer: OfferApi.Offer = offerData.offers.find(o => o.id === offerId)!;
      await props.cancelOffer(offer).then(resp => resp.json());

      return refetch().then(() => {});
    }, [refetch, props.cancelOffer, offerData?.hash]);
  
  

  // cache the end result
  return React.useMemo(() => {
    return { 
      offers: offerData?.offers ?? [], 
      isPending: !isContextLoaded, 
      createOffer, refresh, cancelOffer, getLocalisedOfferName, fetchOffer };
  }, [offerData?.hash, isContextLoaded, createOffer, refresh, cancelOffer, getLocalisedOfferName, fetchOffer]);
}
