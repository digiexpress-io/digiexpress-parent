import React from 'react';


import { useQuery } from '@tanstack/react-query'
import { OfferApi } from './offer-types';
import { LegacyProcessApi } from '../api-legacy-processes';
import { mapToOffer, mapToOfferData } from './mappers';
import { SiteApi, useSite } from '../api-site';



export interface UsePropulateProps {
  options: { staleTime: number, queryKey: string };
  createOffer: OfferApi.CreateOfferFetchPOST;
  getAllOffers: OfferApi.GetOffersFetchGET;
  getOneOffer: OfferApi.GetOfferFetchGET;
  cancelOffer: OfferApi.CancelOfferFetchDELETE;
  getAllowedOffers: OfferApi.GetAllowedOffersFetchGET;
}

export interface PopulateOfferContext {
  offers: readonly OfferApi.Offer[];
  isPending: boolean;
  allowedOffers: string[];
  createOffer: (request: OfferApi.OfferRequest) => Promise<OfferApi.Offer>;
  cancelOffer: (offerId: string) => Promise<void>;
  fetchOffer: (offerId: string) => Promise<OfferApi.Offer>;
  getLocalisedOfferName: (site: SiteApi.Site, workflowName: string) => string;
  refresh(): Promise<void>;
}

export function usePopulateContext(props: UsePropulateProps): PopulateOfferContext {
  const { site } = useSite();
  const [isInitialLoadDone, setInitialLoadDone] = React.useState(false);
  const { getAllOffers, getOneOffer, getAllowedOffers, options } = props;
  const { staleTime, queryKey } = options;

  // tanstack query config
  const { data, error, refetch, isPending } = useQuery({
    staleTime,
    queryKey: [queryKey],
    queryFn: () =>  Promise.all([
      getAllOffers().then(data => data.json()).then((data: LegacyProcessApi.Process[]) => data),
      getAllowedOffers().then(data => data.json()).then((data: string[]) => data),
    ])
  });

  // Get the offer (form) name based on the topic link
  const getLocalisedOfferName = (site: SiteApi.Site, workflowName: string): string => {
    const linkName: string = Object.values(site.links).find(link => link.value === workflowName)?.name!;
    return linkName;
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


  const processes = data?.[0];
  const allowedOffers = data?.[1];

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
      allowedOffers: allowedOffers ?? [], 
      offers: offerData?.offers ?? [], 
      isPending: !isContextLoaded, 
      createOffer, refresh, cancelOffer, getLocalisedOfferName, fetchOffer };
  }, [offerData?.hash, allowedOffers, isContextLoaded, createOffer, refresh, cancelOffer, getLocalisedOfferName, fetchOffer]);
}
