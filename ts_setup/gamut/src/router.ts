import { createRouter } from '@tanstack/react-router'
import { routeTree } from './routeTree.gen'


export { Route as rootRoute } from './routes/__root'
export { Route as IndexImport } from './routes/index'
export { Route as PublicLocaleImport } from './routes/public.$locale'
export { Route as PublicLocaleIndexImport } from './routes/public.$locale.index'
export { Route as SecuredLocaleViewsViewIdImport } from './routes/secured.$locale.views.$viewId'
export { Route as SecuredLocaleViewsViewIdIndexImport } from './routes/secured.$locale.views.$viewId.index'
export { Route as SecuredLocaleViewsViewIdSubjectIdImport } from './routes/secured.$locale.views.$viewId.$subjectId'
export { Route as SecuredLocalePagesPageIdProductsProductIdImport } from './routes/secured.$locale.pages.$pageId.products.$productId'
export { Route as PublicLocalePagesPageIdProductsProductIdImport } from './routes/public.$locale.pages.$pageId.products.$productId'
export { Route as SecuredLocalePagesPageIdProductsProductIdIndexImport } from './routes/secured.$locale.pages.$pageId.products.$productId.index'
export { Route as SecuredLocalePagesPageIdProductsProductIdOffersOfferIdImport } from './routes/secured.$locale.pages.$pageId.products.$productId.offers.$offerId'
export { Route as PublicLocalePagesPageIdProductsProductIdOffersOfferIdImport } from './routes/public.$locale.pages.$pageId.products.$productId.offers.$offerId'
export { Route as SecuredLocalePagesPageIdProductsProductIdOffersOfferIdIndexImport } from './routes/secured.$locale.pages.$pageId.products.$productId.offers.$offerId.index'
export { Route as SecuredLocalePagesPageIdProductsProductIdOffersOfferIdSummaryImport } from './routes/secured.$locale.pages.$pageId.products.$productId.offers.$offerId.summary'


export { routeTree };
export const router = createRouter({ routeTree })
