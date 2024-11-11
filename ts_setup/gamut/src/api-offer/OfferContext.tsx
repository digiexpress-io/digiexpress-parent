import React from 'react'

import { OfferApi } from './offer-types';
import { usePopulateContext } from './usePopulateContext';



export const OfferContext = React.createContext<OfferApi.OfferContextType>({} as any);


export const OfferProvider: React.FC<{
  children: React.ReactNode;
  options: { staleTime: number, queryKey: string };
  createOffer: OfferApi.CreateOfferFetchPOST;
  getOffers: OfferApi.GetOffersFetchGET;
  cancelOffer: OfferApi.CancelOfferFetchDELETE;
}> = (props) => {
  const data = usePopulateContext(props);


  return React.useMemo(() => {

    const contextValue: OfferApi.OfferContextType = {
      offers: data.offers,
      isPending: data.isPending,
      getOffer: (id) => data.offers.find((offer) => offer.id === id),
      refresh: data.refresh,
      createOffer: data.createOffer,
      cancelOffer: data.cancelOffer
    };

    return (<OfferContext.Provider value={contextValue}>{props.children}</OfferContext.Provider>);
  }, [data, props]);
}


export function useOffers(): OfferApi.OfferContextType {
  const result: OfferApi.OfferContextType = React.useContext(OfferContext);
  return result
}
