import { GRouterOfferSummary } from '../g-router-offer-summary';



export const Route = createFileRoute({
  component: Component,
  
})

function Component() {
  const { locale, offerId, productId, pageId } = Route.useParams();
  return <GRouterOfferSummary locale={locale} offerId={offerId} productId={productId} pageId={pageId} />
}
