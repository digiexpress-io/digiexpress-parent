import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { RouterOffer } from '../g-routes';
import { useOffers } from '../api-offer';



export const Route = createFileRoute('/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/')({
  component: Component,
  
})

function Component() {
  const { locale, offerId, productId } = Route.useParams();
  const offers = useOffers();
  const offer = offers.getOffer(offerId);
  const formId = offer?.formId;

  return React.useMemo(() => (<ChooseComponent locale={locale} offerId={offerId} productId={productId} formId={formId} />), [locale, offerId, productId, formId])
}

function ChooseComponent(props: { locale: string, offerId: string, productId: string, formId: string | undefined }) {

  if(!props.formId) {
    return (<>Loading....</>);
  }



  return (<RouterOffer 
    offerId={props.offerId} 
    productId={props.productId} 
    formId={props.formId}
  />)
}
