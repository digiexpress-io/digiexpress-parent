import React from 'react'
import { Outlet, createFileRoute } from '@tanstack/react-router'
import { GRouterProduct } from '../g-router-product';



export const Route = createFileRoute('/secured/$locale/pages/$pageId/products/$productId/')({
  component: Component,
})

function Component() {
  const { locale, productId, pageId } = Route.useParams();
  return React.useMemo(() => (<ChooseComponent locale={locale} productId={productId} pageId={pageId}/>), [locale, productId, pageId])
}

function ChooseComponent(props: { locale: string, productId: string, pageId: string }) {
  return (<GRouterProduct productId={props.productId} locale={props.locale} pageId={props.pageId} />)
}
