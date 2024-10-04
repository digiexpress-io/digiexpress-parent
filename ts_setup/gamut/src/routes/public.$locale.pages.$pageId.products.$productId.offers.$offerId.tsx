import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { RouterOffer } from '../g-routes';
import { useOffers } from '../api-offer';



export const Route = createFileRoute('/public/$locale/pages/$pageId/products/$productId/offers/$offerId')({
  component: Component,
  
})

function Component() {
  const { locale, offerId, productId } = Route.useParams();
  const offers = useOffers();
  const offer = offers.getOffer(offerId);
  const formId = offer?.formUri;

  return React.useMemo(() => (<ChooseComponent locale={locale} offerId={offerId} productId={productId} formId={formId}/>), [locale, productId, offerId, formId])
}

function ChooseComponent(props: { locale: string, offerId: string, productId: string, formId?: string }) {

  if(!props.formId) {
    return (<></>);
  }


  return (<RouterOffer 
    formId={props.formId}
    offerId={props.offerId} 
    productId={props.productId} 
  />)
}
