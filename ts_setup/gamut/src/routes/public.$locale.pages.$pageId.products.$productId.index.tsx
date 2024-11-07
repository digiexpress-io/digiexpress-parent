import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { RouterProduct } from '../g-routes'

export const Route = createFileRoute(
  '/public/$locale/pages/$pageId/products/$productId/',
)({
  component: Component,
})

function Component() {
  const { locale, productId, pageId } = Route.useParams()
  return React.useMemo(
    () => (
      <ChooseComponent locale={locale} productId={productId} pageId={pageId} />
    ),
    [locale, productId, pageId],
  )
}

function ChooseComponent(props: {
  locale: string
  productId: string
  pageId: string
}) {
  return (
    <RouterProduct
      productId={props.productId}
      pageId={props.pageId}
      locale={props.locale}
    />
  )
}
