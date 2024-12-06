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
  useIam,
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


const SecuredSetup: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const staleTime = 5 * 1000;
  const processesQueryKey = 'legacy-processes';

  const dialobFetch = createDialobFetch();
  const offerFetch = createOfferFetch();
  const contractFetch = createContractFetch();
  const subjectFetch = createSubjectFetch();
  const bookingFetch = createBookingFetch();


  return (<DialobProvider fetchActionGet={dialobFetch.fetchActionGet} fetchActionPost={dialobFetch.fetchActionPost} fetchReviewGet={dialobFetch.fetchReviewGet}>

      <OfferProvider cancelOffer={offerFetch.fetchDelete} createOffer={offerFetch.fetchPost} getOffers={offerFetch.fetchGet} options={{ staleTime, queryKey: processesQueryKey }}>
        <ContractProvider appendContractAttachment={contractFetch.appendContractAttachment} getContracts={contractFetch.fetchGet} options={{ staleTime, queryKey: processesQueryKey }}>
        <CommsProvider getSubjects={subjectFetch.fetchGet} replyTo={subjectFetch.fetchPost} options={{ staleTime, queryKey: processesQueryKey }}>
            <BookingProvider getBookings={bookingFetch.fetchGet} cancelBooking={bookingFetch.fetchPost} options={{ staleTime, queryKey: 'bookings' }}>
              {children}
            </BookingProvider>
          </CommsProvider>
        </ContractProvider>
      </OfferProvider>
  </DialobProvider>)
}

const PublicSetup: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (<>{children}</>)
}


const AuthSetup: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const iam = useIam();
  return (iam.authType === 'ANON' ? <PublicSetup>{children}</PublicSetup> : <SecuredSetup>{children}</SecuredSetup>)
}


export const DemoApp: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const queryClient = new QueryClient()
  const iamFetch = createIamFetch();
  const siteFetch = createSiteFetch();

  const liveness = 60000;
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
            <SiteBackendProvider fetchSiteGet={siteFetch.fetchSiteGet} fetchFeedbackGet={siteFetch.fetchFeedbackGet}>
              <AuthSetup>{children}</AuthSetup>
            </SiteBackendProvider>
          </IamBackendProvider>
        </DemoTheme>
      </LocaleProvider>
    </QueryClientProvider>);
}



