import React from 'react';
import { createRootRoute, Outlet, useNavigate } from '@tanstack/react-router'
import { useIntl } from 'react-intl';

import { GErrorNotFound, GError } from '@dxs-ts/gamut-primitives';

import { 
  useConfig, 
  BookingProvider, ContractProvider, OfferProvider, CommsProvider,
  DialobProvider, SiteBackendProvider,
  IamBackendProvider, useIam 
} from '@dxs-ts/gamut-api';

export const Route = createRootRoute({
  component: RouteComponent,
  notFoundComponent: GErrorNotFound,
  errorComponent: GError,
})

function RouteComponent() {
  const intl = useIntl();
  const nav = useNavigate();
  const { iamFetch, iamLiveness, handleExpire } = useConfig();
  const title = intl.formatMessage({ id: 'document.title' });

  React.useEffect(() => {
    document.title = title;
  }, [title])

  const onAuthorizationFail = React.useCallback((error: Error, locale: string) => {
    nav({
      params: { locale },
      to: '/public/$locale/login'
    });
  }, [nav]);

  return (<IamBackendProvider
      liveness={iamLiveness}
      onAuthorizationFail={onAuthorizationFail}
      onExpire={handleExpire}
      fetchUserGET={iamFetch.fetchUserGET}
      fetchUserLivenessGET={iamFetch.fetchUserLivenessGET}
      fetchUserProductsGET={iamFetch.fetchUserProductsGET}
      fetchUserRolesGET={iamFetch.fetchUserRolesGET}>

      <AuthSetup>{<Outlet />}</AuthSetup>
    </IamBackendProvider>)
}


const SecuredSetup: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { siteFetch, authFeedbackFetch, dialobFetch, offerFetch, staleTime, processesQueryKey, contractFetch, subjectFetch, bookingFetch } = useConfig();
  return (
    <SiteBackendProvider
      fetchSiteGet={siteFetch.fetchSiteGet}
      fetchFeedbackGet={authFeedbackFetch.fetchFeedbackGet}
      fetchFeedbackRatingPut={authFeedbackFetch.fetchFeedbackRatingPut} >

      <DialobProvider 
        fetchActionGet={dialobFetch.fetchActionGet} 
        fetchActionPost={dialobFetch.fetchActionPost} 
        fetchReviewGet={dialobFetch.fetchReviewGet}
        fetchAttachmentPost={dialobFetch.fetchAttachmentPost}
        >
        
        <OfferProvider 
          cancelOffer={offerFetch.fetchDelete}
          createOffer={offerFetch.fetchPost} 
          getOneOffer={offerFetch.fetchOneGet} 
          getAllOffers={offerFetch.fetchAllGet} 
          options={{ staleTime, queryKey: processesQueryKey }}>
          
          <ContractProvider 
            appendContractAttachment={contractFetch.appendContractAttachment} 
            getContracts={contractFetch.fetchGet} 
            getContractAttachment={contractFetch.fetchContractAttachment}
            options={{ staleTime, queryKey: processesQueryKey }}>

            <CommsProvider markViewed={subjectFetch.fetchPut} getSubjects={subjectFetch.fetchGet} replyTo={subjectFetch.fetchPost} options={{ staleTime, queryKey: processesQueryKey }}>
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
    const { siteFetch, authFeedbackFetch, dialobFetch, staleTime, processesQueryKey, dialobPublicFetch, publicOfferFetch } = useConfig();
  return (
    <SiteBackendProvider
      fetchSiteGet={siteFetch.fetchSiteGet}
      fetchFeedbackGet={siteFetch.fetchFeedbackGet}
      fetchFeedbackRatingPut={authFeedbackFetch.fetchFeedbackRatingPut}>
      <DialobProvider 
        fetchActionGet={dialobPublicFetch.fetchActionGet} 
        fetchActionPost={dialobPublicFetch.fetchActionPost} 
        fetchReviewGet={dialobPublicFetch.fetchReviewGet}
        fetchAttachmentPost={dialobFetch.fetchAttachmentPost}>
        
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