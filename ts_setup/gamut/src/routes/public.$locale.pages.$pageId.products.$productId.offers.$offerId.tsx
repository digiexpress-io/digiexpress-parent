import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { GRouterOffer } from '../g-router-offer';
import { OfferApi, useOffers } from '../api-offer';



export const Route = createFileRoute('/public/$locale/pages/$pageId/products/$productId/offers/$offerId')({
  component: Component,
  
})

function Component() {
  const { locale, offerId, productId } = Route.useParams();
  const offers = useOffers();
  const [offer, setOffer] = React.useState<OfferApi.Offer>();

  React.useEffect(() => {
    offers.fetchOffer(offerId).then(setOffer);
  }, [offerId]);

  const formId = offer?.formId;
  return React.useMemo(() => (<ChooseComponent locale={locale} offerId={offerId} productId={productId} formId={formId}/>), [locale, productId, offerId, formId])
}

function ChooseComponent(props: { locale: string, offerId: string, productId: string, formId?: string }) {

  if(!props.formId) {
    return (<>Loading offer...</>);
  }
  return (<GRouterOffer 
    formId={props.formId}
    offerId={props.offerId} 
    productId={props.productId} 
  />)
}
