import React from 'react'
import { GRouterOffer } from '../g-router-offer';



export const Route = createFileRoute({
  component: Component,
  
})

function Component() {
  const { locale, offerId, productId } = Route.useParams();

  return React.useMemo(() => (<ChooseComponent locale={locale} offerId={offerId} productId={productId} />), 
    [locale, offerId, productId])
}

function ChooseComponent(props: { locale: string, offerId: string, productId: string}) {

  return (<GRouterOffer 
    offerId={props.offerId} 
    productId={props.productId} 
  />)
}
