import React from 'react';

import {
  DialobProvider,
  SiteBackendProvider,
  IamBackendProvider,
  OfferProvider,
  ContractProvider,
  CommsProvider,
  BookingProvider,
  LocaleProvider,
} from '@dxs-ts/gamut';

import { QueryClientProvider, QueryClient } from '@tanstack/react-query'

import { DemoTheme } from './theme';

import {
  createBookingFetch,
  createContractFetch,
  createDialobFetch,
  createIamFetch,
  createOfferFetch,
  createSiteFetch,
  createSubjectFetch
} from './fetch';


export const DemoApp: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const queryClient = new QueryClient()
  const dialobFetch = createDialobFetch();
  const iamFetch = createIamFetch();
  const siteFetch = createSiteFetch();
  const offerFetch = createOfferFetch();
  const contractFetch = createContractFetch();
  const subjectFetch = createSubjectFetch();
  const bookingFetch = createBookingFetch();

  const liveness = 60000;
  const staleTime = 5 * 1000;
  const processesQueryKey = 'legacy-processes';

  function handleExpire() {
    alert("SESSION EXPIRED");
  }

  return (
    <QueryClientProvider client={queryClient}>
      <LocaleProvider>
        <DemoTheme>
          <IamBackendProvider liveness={liveness} onExpire={handleExpire}
            fetchUserGET={iamFetch.fetchUserGET}
            fetchUserLivenessGET={iamFetch.fetchUserLivenessGET}
            fetchUserProductsGET={iamFetch.fetchUserProductsGET}
            fetchUserRolesGET={iamFetch.fetchUserRolesGET}>

            <DialobProvider fetchGet={dialobFetch.fetchGet} fetchPost={dialobFetch.fetchPost}>
              <SiteBackendProvider fetchGet={siteFetch.fetchGet}>
                <OfferProvider cancelOffer={offerFetch.fetchDelete} createOffer={offerFetch.fetchPost} getOffers={offerFetch.fetchGet} options={{ staleTime, queryKey: processesQueryKey }}>
                  <ContractProvider appendContractAttachment={contractFetch.appendContractAttachment} getContracts={contractFetch.fetchGet} options={{ staleTime, queryKey: processesQueryKey }}>
                    <CommsProvider getSubjects={subjectFetch.fetchGet} options={{ staleTime, queryKey: processesQueryKey }}>
                      <BookingProvider getBookings={bookingFetch.fetchGet} cancelBooking={bookingFetch.fetchPost} options={{ staleTime, queryKey: 'bookings' }}>
                        {children}
                      </BookingProvider>
                    </CommsProvider>
                  </ContractProvider>
                </OfferProvider>
              </SiteBackendProvider>
            </DialobProvider>
          </IamBackendProvider>
        </DemoTheme>
      </LocaleProvider>
    </QueryClientProvider>);
}



