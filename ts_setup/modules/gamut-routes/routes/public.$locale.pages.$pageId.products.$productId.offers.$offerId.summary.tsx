import { createFileRoute } from '@tanstack/react-router'
import { GRouterOfferSummary } from '../g-router-offer-summary';



export const Route = createFileRoute(
  '/public/$locale/pages/$pageId/products/$productId/offers/$offerId/summary'
)({
  component: Component,
  
})

function Component() {
  const { locale, offerId, productId, pageId } = Route.useParams();
  return <GRouterOfferSummary locale={locale} offerId={offerId} productId={productId} pageId={pageId} />
}
