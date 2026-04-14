import React from 'react'

import { OfferApi } from './offer-types';
import { usePopulateContext } from './usePopulateContext';



export const OfferContext = React.createContext<OfferApi.OfferContextType>({} as any);


export const OfferProvider: React.FC<{
  children: React.ReactNode;
  options: { staleTime: number, queryKey: string };
  createOffer: OfferApi.CreateOfferFetchPOST;
  getAllOffers: OfferApi.GetOffersFetchGET;
  getOneOffer: OfferApi.GetOfferFetchGET;
  cancelOffer: OfferApi.CancelOfferFetchDELETE;
}> = (props) => {
  const data = usePopulateContext(props);

  const [sortOrder, setSortOrder] = React.useState<OfferApi.OfferSortOrder>('DESC');
  const sortedByDate = data.offers
    .filter((c) => !!c.updated)
    .sort((a, b) => {
      const dateA = a.updated ? a.updated.toMillis() : 0;
      const dateB = b.updated ? b.updated.toMillis() : 0;
      return sortOrder === 'ASC' ? dateA - dateB : dateB - dateA;
    });

  function toggleOfferSortOrder() {
    setSortOrder((prev) => (prev === 'ASC' ? 'DESC' : 'ASC'));
  };

  return React.useMemo(() => {

    const contextValue: OfferApi.OfferContextType = {
      offers: sortedByDate,
      isPending: data.isPending,
      
      getOffer: (id) => {
        const found = data.offers.find((offer) => offer.id === id);
        if(!found) {
          console.error('Can\'t find offer by id', { id, fromData: data.offers });
        }
        return found;
      },
      refresh: data.refresh,
      createOffer: data.createOffer,
      cancelOffer: data.cancelOffer,
      fetchOffer: data.fetchOffer,
      getLocalisedOfferName: data.getLocalisedOfferName,
      sortOrder,
      toggleOfferSortOrder
    };

    if(data.isPending) {
      return (<></>);
    }

    return (<OfferContext.Provider value={contextValue}>{props.children}</OfferContext.Provider>);
  }, [data, props]);
}


export function useOffers(): OfferApi.OfferContextType {
  const result: OfferApi.OfferContextType = React.useContext(OfferContext);
  return result
}
