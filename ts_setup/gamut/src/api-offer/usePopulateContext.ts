import React from 'react';


import { useQuery } from '@tanstack/react-query'
import { OfferApi } from './offer-types';
import { LegacyProcessApi } from '../api-legacy-processes';
import { mapToOffer, mapToOfferData } from './mappers';
import { useSite } from '../api-site';



export interface UsePropulateProps {
  options: { staleTime: number, queryKey: string };
  createOffer: OfferApi.CreateOfferFetchPOST;
  getOffers: OfferApi.GetOffersFetchGET;
  cancelOffer: OfferApi.CancelOfferFetchDELETE;
}

export interface PopulateOfferContext {
  offers: readonly OfferApi.Offer[];
  isPending: boolean;
  createOffer: (request: OfferApi.OfferRequest) => Promise<OfferApi.Offer>;
  cancelOffer: (offerId: string) => Promise<void>;
  refresh(): Promise<void>;
}

export function usePopulateContext(props: UsePropulateProps): PopulateOfferContext {
  const { site } = useSite();
  const [isInitialLoadDone, setInitialLoadDone] = React.useState(false);
  const { getOffers, options } = props;
  const { staleTime, queryKey } = options;

  // tanstack query config
  const { data: processes, error, refetch, isPending } = useQuery({
    staleTime,
    queryKey: [queryKey],
    queryFn: () => getOffers()
      .then(data => data.json())
      .then((data: LegacyProcessApi.Process[]) => {
        return data;
      }),
  });



  // Create new offer and reload after that
  const createOffer: (request: OfferApi.OfferRequest) => Promise<OfferApi.Offer> = React.useCallback(async (request) => {
    const newOffer: OfferApi.Offer = await props.createOffer(request).then(resp => resp.json()).then(data => mapToOffer(data, site));
    return refetch().then(() => newOffer);
  }, [refetch, props.createOffer, site]);



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
    return { offers: offerData?.offers ?? [], isPending: !isContextLoaded, createOffer, refresh, cancelOffer };
  }, [offerData?.hash, isContextLoaded, createOffer, refresh, cancelOffer]);
}
