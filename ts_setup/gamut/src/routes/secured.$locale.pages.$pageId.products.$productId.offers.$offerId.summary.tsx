import { createFileRoute } from '@tanstack/react-router'
import { RouterOfferSummary } from '../g-routes';



export const Route = createFileRoute('/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary')({
  component: Component,
  
})

function Component() {
  const { locale, offerId, productId, pageId } = Route.useParams();
  return <RouterOfferSummary locale={locale} offerId={offerId} productId={productId} pageId={pageId} />
}
