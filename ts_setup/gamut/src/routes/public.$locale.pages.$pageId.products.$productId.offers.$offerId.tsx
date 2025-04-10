import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { GRouterOffer } from '../g-router-offer';
import { OfferApi, useOffers } from '../api-offer';



export const Route = createFileRoute('/public/$locale/pages/$pageId/products/$productId/offers/$offerId')({
  component: Component,
  
})

function Component() {
  const { locale, offerId, productId } = Route.useParams();
  return React.useMemo(() => (<ChooseComponent locale={locale} offerId={offerId} productId={productId}/>), [locale, productId, offerId])
}

function ChooseComponent(props: { locale: string, offerId: string, productId: string }) {
  return (<GRouterOffer 
    offerId={props.offerId} 
    productId={props.productId} 
  />)
}
