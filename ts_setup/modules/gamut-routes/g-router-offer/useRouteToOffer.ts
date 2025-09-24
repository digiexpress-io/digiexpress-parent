import { OfferApi, useLocale } from "@dxs-ts/gamut-api";
import { useNavigate } from "@tanstack/react-router";



export function useRouteToOffer() {
  const nav = useNavigate();
  const defaultLocale = useLocale();

  function onOpenOffer(offer: OfferApi.Offer) {
    const offerId = offer.id;
    const pageId = offer.pageId;
    const productId = offer.productId;


    if(offer.assigned) {
      const params = {offerId, pageId, productId: productId ? productId : '__search_topic', locale: defaultLocale.locale };
      nav({
        params: params,
        to: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId',
      })
    } else if(!!productId) {
      nav({
        from: '/secured/$locale/views/$viewId',
        params: { offerId, pageId, productId },
        to: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId',
      })
    } else if(offer.otherLocales.length > 0 && offer.otherLocales[0].productId) {
      nav({
        from: '/secured/$locale/views/$viewId',
        params: { offerId, pageId, productId: offer.otherLocales[0].productId, locale: offer.otherLocales[0].locale },
        to: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId',
      })
    } else {
      // TODO: polite error msgs
      alert('Form not available!');
    }
  }

  return { onOpenOffer }
}