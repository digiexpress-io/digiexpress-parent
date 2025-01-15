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
  createSubjectFetch,
  createAuthFeedbackFetch,
  createPublicOfferFetch,
  createPublicDialobFetch
} from './fetch';

const staleTime = 5 * 1000;
const processesQueryKey = 'legacy-processes';


const iamFetch = createIamFetch();
const siteFetch = createSiteFetch();
const dialobFetch = createDialobFetch();
const dialobPublicFetch = createPublicDialobFetch();
const offerFetch = createOfferFetch();
const publicOfferFetch = createPublicOfferFetch();
const contractFetch = createContractFetch();
const subjectFetch = createSubjectFetch();
const bookingFetch = createBookingFetch();
const authFeedbackFetch = createAuthFeedbackFetch();


const SecuredSetup: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <SiteBackendProvider
      fetchSiteGet={siteFetch.fetchSiteGet}
      fetchFeedbackGet={authFeedbackFetch.fetchFeedbackGet}
      fetchFeedbackRatingPut={authFeedbackFetch.fetchFeedbackRatingPut} >

      <DialobProvider fetchActionGet={dialobFetch.fetchActionGet} fetchActionPost={dialobFetch.fetchActionPost} fetchReviewGet={dialobFetch.fetchReviewGet}>
        <OfferProvider 
          cancelOffer={offerFetch.fetchDelete}
          createOffer={offerFetch.fetchPost} 
          getOneOffer={offerFetch.fetchOneGet} 
          getAllOffers={offerFetch.fetchAllGet} 
          options={{ staleTime, queryKey: processesQueryKey }}>
          
          <ContractProvider appendContractAttachment={contractFetch.appendContractAttachment} getContracts={contractFetch.fetchGet} options={{ staleTime, queryKey: processesQueryKey }}>
          <CommsProvider getSubjects={subjectFetch.fetchGet} replyTo={subjectFetch.fetchPost} options={{ staleTime, queryKey: processesQueryKey }}>
            <BookingProvider getBookings={bookingFetch.fetchGet} cancelBooking={bookingFetch.fetchPost} options={{ staleTime, queryKey: 'bookings' }}>
              {children}
            </BookingProvider>
            </CommsProvider>
          </ContractProvider>
        </OfferProvider>
      </DialobProvider>
    </SiteBackendProvider >)
}

const PublicSetup: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <SiteBackendProvider
      fetchSiteGet={siteFetch.fetchSiteGet}
      fetchFeedbackGet={siteFetch.fetchFeedbackGet}
      fetchFeedbackRatingPut={authFeedbackFetch.fetchFeedbackRatingPut}>
      <DialobProvider 
        fetchActionGet={dialobPublicFetch.fetchActionGet} 
        fetchActionPost={dialobPublicFetch.fetchActionPost} 
        fetchReviewGet={dialobPublicFetch.fetchReviewGet}>
        
        <OfferProvider 
          cancelOffer={publicOfferFetch.fetchDelete} 
          createOffer={publicOfferFetch.fetchPost} 
          getOneOffer={publicOfferFetch.fetchOneGet} 
          getAllOffers={publicOfferFetch.fetchAllGet} 
          options={{ staleTime, queryKey: processesQueryKey }}>
          
          {children}
        </OfferProvider>
      </DialobProvider>
    </SiteBackendProvider>
  )
}
const AuthSetup: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const iam = useIam();
  return (iam.authType === 'ANON' ? <PublicSetup>{children}</PublicSetup> : <SecuredSetup>{children}</SecuredSetup>)
}

export const DemoApp: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const queryClient = new QueryClient()

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

            <AuthSetup>{children}</AuthSetup>
          </IamBackendProvider>
        </DemoTheme>
      </LocaleProvider>
    </QueryClientProvider>);
}



