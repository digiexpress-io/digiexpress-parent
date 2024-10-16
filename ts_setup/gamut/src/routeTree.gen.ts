/* prettier-ignore-start */

/* eslint-disable */

// @ts-nocheck

// noinspection JSUnusedGlobalSymbols

// This file is auto-generated by TanStack Router

// Import Routes

import { Route as rootRoute } from './routes/__root'
import { Route as IndexImport } from './routes/index'
import { Route as PublicLocaleImport } from './routes/public.$locale'
import { Route as PublicLocaleIndexImport } from './routes/public.$locale.index'
import { Route as SecuredLocaleViewsViewIdImport } from './routes/secured.$locale.views.$viewId'
import { Route as SecuredLocaleViewsViewIdIndexImport } from './routes/secured.$locale.views.$viewId.index'
import { Route as SecuredLocaleViewsViewIdSubjectIdImport } from './routes/secured.$locale.views.$viewId.$subjectId'
import { Route as SecuredLocalePagesPageIdProductsProductIdImport } from './routes/secured.$locale.pages.$pageId.products.$productId'
import { Route as PublicLocalePagesPageIdProductsProductIdImport } from './routes/public.$locale.pages.$pageId.products.$productId'
import { Route as SecuredLocalePagesPageIdProductsProductIdIndexImport } from './routes/secured.$locale.pages.$pageId.products.$productId.index'
import { Route as SecuredLocalePagesPageIdProductsProductIdOffersOfferIdImport } from './routes/secured.$locale.pages.$pageId.products.$productId.offers.$offerId'
import { Route as PublicLocalePagesPageIdProductsProductIdOffersOfferIdImport } from './routes/public.$locale.pages.$pageId.products.$productId.offers.$offerId'
import { Route as SecuredLocalePagesPageIdProductsProductIdOffersOfferIdIndexImport } from './routes/secured.$locale.pages.$pageId.products.$productId.offers.$offerId.index'
import { Route as SecuredLocalePagesPageIdProductsProductIdOffersOfferIdSummaryImport } from './routes/secured.$locale.pages.$pageId.products.$productId.offers.$offerId.summary'

// Create/Update Routes

const IndexRoute = IndexImport.update({
  path: '/',
  getParentRoute: () => rootRoute,
} as any)

const PublicLocaleRoute = PublicLocaleImport.update({
  path: '/public/$locale',
  getParentRoute: () => rootRoute,
} as any)

const PublicLocaleIndexRoute = PublicLocaleIndexImport.update({
  path: '/',
  getParentRoute: () => PublicLocaleRoute,
} as any)

const SecuredLocaleViewsViewIdRoute = SecuredLocaleViewsViewIdImport.update({
  path: '/secured/$locale/views/$viewId',
  getParentRoute: () => rootRoute,
} as any)

const SecuredLocaleViewsViewIdIndexRoute =
  SecuredLocaleViewsViewIdIndexImport.update({
    path: '/',
    getParentRoute: () => SecuredLocaleViewsViewIdRoute,
  } as any)

const SecuredLocaleViewsViewIdSubjectIdRoute =
  SecuredLocaleViewsViewIdSubjectIdImport.update({
    path: '/$subjectId',
    getParentRoute: () => SecuredLocaleViewsViewIdRoute,
  } as any)

const SecuredLocalePagesPageIdProductsProductIdRoute =
  SecuredLocalePagesPageIdProductsProductIdImport.update({
    path: '/secured/$locale/pages/$pageId/products/$productId',
    getParentRoute: () => rootRoute,
  } as any)

const PublicLocalePagesPageIdProductsProductIdRoute =
  PublicLocalePagesPageIdProductsProductIdImport.update({
    path: '/pages/$pageId/products/$productId',
    getParentRoute: () => PublicLocaleRoute,
  } as any)

const SecuredLocalePagesPageIdProductsProductIdIndexRoute =
  SecuredLocalePagesPageIdProductsProductIdIndexImport.update({
    path: '/',
    getParentRoute: () => SecuredLocalePagesPageIdProductsProductIdRoute,
  } as any)

const SecuredLocalePagesPageIdProductsProductIdOffersOfferIdRoute =
  SecuredLocalePagesPageIdProductsProductIdOffersOfferIdImport.update({
    path: '/offers/$offerId',
    getParentRoute: () => SecuredLocalePagesPageIdProductsProductIdRoute,
  } as any)

const PublicLocalePagesPageIdProductsProductIdOffersOfferIdRoute =
  PublicLocalePagesPageIdProductsProductIdOffersOfferIdImport.update({
    path: '/offers/$offerId',
    getParentRoute: () => PublicLocalePagesPageIdProductsProductIdRoute,
  } as any)

const SecuredLocalePagesPageIdProductsProductIdOffersOfferIdIndexRoute =
  SecuredLocalePagesPageIdProductsProductIdOffersOfferIdIndexImport.update({
    path: '/',
    getParentRoute: () =>
      SecuredLocalePagesPageIdProductsProductIdOffersOfferIdRoute,
  } as any)

const SecuredLocalePagesPageIdProductsProductIdOffersOfferIdSummaryRoute =
  SecuredLocalePagesPageIdProductsProductIdOffersOfferIdSummaryImport.update({
    path: '/summary',
    getParentRoute: () =>
      SecuredLocalePagesPageIdProductsProductIdOffersOfferIdRoute,
  } as any)

// Populate the FileRoutesByPath interface

declare module '@tanstack/react-router' {
  interface FileRoutesByPath {
    '/': {
      id: '/'
      path: '/'
      fullPath: '/'
      preLoaderRoute: typeof IndexImport
      parentRoute: typeof rootRoute
    }
    '/public/$locale': {
      id: '/public/$locale'
      path: '/public/$locale'
      fullPath: '/public/$locale'
      preLoaderRoute: typeof PublicLocaleImport
      parentRoute: typeof rootRoute
    }
    '/public/$locale/': {
      id: '/public/$locale/'
      path: '/'
      fullPath: '/public/$locale/'
      preLoaderRoute: typeof PublicLocaleIndexImport
      parentRoute: typeof PublicLocaleImport
    }
    '/secured/$locale/views/$viewId': {
      id: '/secured/$locale/views/$viewId'
      path: '/secured/$locale/views/$viewId'
      fullPath: '/secured/$locale/views/$viewId'
      preLoaderRoute: typeof SecuredLocaleViewsViewIdImport
      parentRoute: typeof rootRoute
    }
    '/secured/$locale/views/$viewId/$subjectId': {
      id: '/secured/$locale/views/$viewId/$subjectId'
      path: '/$subjectId'
      fullPath: '/secured/$locale/views/$viewId/$subjectId'
      preLoaderRoute: typeof SecuredLocaleViewsViewIdSubjectIdImport
      parentRoute: typeof SecuredLocaleViewsViewIdImport
    }
    '/secured/$locale/views/$viewId/': {
      id: '/secured/$locale/views/$viewId/'
      path: '/'
      fullPath: '/secured/$locale/views/$viewId/'
      preLoaderRoute: typeof SecuredLocaleViewsViewIdIndexImport
      parentRoute: typeof SecuredLocaleViewsViewIdImport
    }
    '/public/$locale/pages/$pageId/products/$productId': {
      id: '/public/$locale/pages/$pageId/products/$productId'
      path: '/pages/$pageId/products/$productId'
      fullPath: '/public/$locale/pages/$pageId/products/$productId'
      preLoaderRoute: typeof PublicLocalePagesPageIdProductsProductIdImport
      parentRoute: typeof PublicLocaleImport
    }
    '/secured/$locale/pages/$pageId/products/$productId': {
      id: '/secured/$locale/pages/$pageId/products/$productId'
      path: '/secured/$locale/pages/$pageId/products/$productId'
      fullPath: '/secured/$locale/pages/$pageId/products/$productId'
      preLoaderRoute: typeof SecuredLocalePagesPageIdProductsProductIdImport
      parentRoute: typeof rootRoute
    }
    '/secured/$locale/pages/$pageId/products/$productId/': {
      id: '/secured/$locale/pages/$pageId/products/$productId/'
      path: '/'
      fullPath: '/secured/$locale/pages/$pageId/products/$productId/'
      preLoaderRoute: typeof SecuredLocalePagesPageIdProductsProductIdIndexImport
      parentRoute: typeof SecuredLocalePagesPageIdProductsProductIdImport
    }
    '/public/$locale/pages/$pageId/products/$productId/offers/$offerId': {
      id: '/public/$locale/pages/$pageId/products/$productId/offers/$offerId'
      path: '/offers/$offerId'
      fullPath: '/public/$locale/pages/$pageId/products/$productId/offers/$offerId'
      preLoaderRoute: typeof PublicLocalePagesPageIdProductsProductIdOffersOfferIdImport
      parentRoute: typeof PublicLocalePagesPageIdProductsProductIdImport
    }
    '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId': {
      id: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId'
      path: '/offers/$offerId'
      fullPath: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId'
      preLoaderRoute: typeof SecuredLocalePagesPageIdProductsProductIdOffersOfferIdImport
      parentRoute: typeof SecuredLocalePagesPageIdProductsProductIdImport
    }
    '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary': {
      id: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary'
      path: '/summary'
      fullPath: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary'
      preLoaderRoute: typeof SecuredLocalePagesPageIdProductsProductIdOffersOfferIdSummaryImport
      parentRoute: typeof SecuredLocalePagesPageIdProductsProductIdOffersOfferIdImport
    }
    '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/': {
      id: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/'
      path: '/'
      fullPath: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/'
      preLoaderRoute: typeof SecuredLocalePagesPageIdProductsProductIdOffersOfferIdIndexImport
      parentRoute: typeof SecuredLocalePagesPageIdProductsProductIdOffersOfferIdImport
    }
  }
}

// Create and export the route tree

interface PublicLocalePagesPageIdProductsProductIdRouteChildren {
  PublicLocalePagesPageIdProductsProductIdOffersOfferIdRoute: typeof PublicLocalePagesPageIdProductsProductIdOffersOfferIdRoute
}

const PublicLocalePagesPageIdProductsProductIdRouteChildren: PublicLocalePagesPageIdProductsProductIdRouteChildren =
  {
    PublicLocalePagesPageIdProductsProductIdOffersOfferIdRoute:
      PublicLocalePagesPageIdProductsProductIdOffersOfferIdRoute,
  }

const PublicLocalePagesPageIdProductsProductIdRouteWithChildren =
  PublicLocalePagesPageIdProductsProductIdRoute._addFileChildren(
    PublicLocalePagesPageIdProductsProductIdRouteChildren,
  )

interface PublicLocaleRouteChildren {
  PublicLocaleIndexRoute: typeof PublicLocaleIndexRoute
  PublicLocalePagesPageIdProductsProductIdRoute: typeof PublicLocalePagesPageIdProductsProductIdRouteWithChildren
}

const PublicLocaleRouteChildren: PublicLocaleRouteChildren = {
  PublicLocaleIndexRoute: PublicLocaleIndexRoute,
  PublicLocalePagesPageIdProductsProductIdRoute:
    PublicLocalePagesPageIdProductsProductIdRouteWithChildren,
}

const PublicLocaleRouteWithChildren = PublicLocaleRoute._addFileChildren(
  PublicLocaleRouteChildren,
)

interface SecuredLocaleViewsViewIdRouteChildren {
  SecuredLocaleViewsViewIdSubjectIdRoute: typeof SecuredLocaleViewsViewIdSubjectIdRoute
  SecuredLocaleViewsViewIdIndexRoute: typeof SecuredLocaleViewsViewIdIndexRoute
}

const SecuredLocaleViewsViewIdRouteChildren: SecuredLocaleViewsViewIdRouteChildren =
  {
    SecuredLocaleViewsViewIdSubjectIdRoute:
      SecuredLocaleViewsViewIdSubjectIdRoute,
    SecuredLocaleViewsViewIdIndexRoute: SecuredLocaleViewsViewIdIndexRoute,
  }

const SecuredLocaleViewsViewIdRouteWithChildren =
  SecuredLocaleViewsViewIdRoute._addFileChildren(
    SecuredLocaleViewsViewIdRouteChildren,
  )

interface SecuredLocalePagesPageIdProductsProductIdOffersOfferIdRouteChildren {
  SecuredLocalePagesPageIdProductsProductIdOffersOfferIdSummaryRoute: typeof SecuredLocalePagesPageIdProductsProductIdOffersOfferIdSummaryRoute
  SecuredLocalePagesPageIdProductsProductIdOffersOfferIdIndexRoute: typeof SecuredLocalePagesPageIdProductsProductIdOffersOfferIdIndexRoute
}

const SecuredLocalePagesPageIdProductsProductIdOffersOfferIdRouteChildren: SecuredLocalePagesPageIdProductsProductIdOffersOfferIdRouteChildren =
  {
    SecuredLocalePagesPageIdProductsProductIdOffersOfferIdSummaryRoute:
      SecuredLocalePagesPageIdProductsProductIdOffersOfferIdSummaryRoute,
    SecuredLocalePagesPageIdProductsProductIdOffersOfferIdIndexRoute:
      SecuredLocalePagesPageIdProductsProductIdOffersOfferIdIndexRoute,
  }

const SecuredLocalePagesPageIdProductsProductIdOffersOfferIdRouteWithChildren =
  SecuredLocalePagesPageIdProductsProductIdOffersOfferIdRoute._addFileChildren(
    SecuredLocalePagesPageIdProductsProductIdOffersOfferIdRouteChildren,
  )

interface SecuredLocalePagesPageIdProductsProductIdRouteChildren {
  SecuredLocalePagesPageIdProductsProductIdIndexRoute: typeof SecuredLocalePagesPageIdProductsProductIdIndexRoute
  SecuredLocalePagesPageIdProductsProductIdOffersOfferIdRoute: typeof SecuredLocalePagesPageIdProductsProductIdOffersOfferIdRouteWithChildren
}

const SecuredLocalePagesPageIdProductsProductIdRouteChildren: SecuredLocalePagesPageIdProductsProductIdRouteChildren =
  {
    SecuredLocalePagesPageIdProductsProductIdIndexRoute:
      SecuredLocalePagesPageIdProductsProductIdIndexRoute,
    SecuredLocalePagesPageIdProductsProductIdOffersOfferIdRoute:
      SecuredLocalePagesPageIdProductsProductIdOffersOfferIdRouteWithChildren,
  }

const SecuredLocalePagesPageIdProductsProductIdRouteWithChildren =
  SecuredLocalePagesPageIdProductsProductIdRoute._addFileChildren(
    SecuredLocalePagesPageIdProductsProductIdRouteChildren,
  )

export interface FileRoutesByFullPath {
  '/': typeof IndexRoute
  '/public/$locale': typeof PublicLocaleRouteWithChildren
  '/public/$locale/': typeof PublicLocaleIndexRoute
  '/secured/$locale/views/$viewId': typeof SecuredLocaleViewsViewIdRouteWithChildren
  '/secured/$locale/views/$viewId/$subjectId': typeof SecuredLocaleViewsViewIdSubjectIdRoute
  '/secured/$locale/views/$viewId/': typeof SecuredLocaleViewsViewIdIndexRoute
  '/public/$locale/pages/$pageId/products/$productId': typeof PublicLocalePagesPageIdProductsProductIdRouteWithChildren
  '/secured/$locale/pages/$pageId/products/$productId': typeof SecuredLocalePagesPageIdProductsProductIdRouteWithChildren
  '/secured/$locale/pages/$pageId/products/$productId/': typeof SecuredLocalePagesPageIdProductsProductIdIndexRoute
  '/public/$locale/pages/$pageId/products/$productId/offers/$offerId': typeof PublicLocalePagesPageIdProductsProductIdOffersOfferIdRoute
  '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId': typeof SecuredLocalePagesPageIdProductsProductIdOffersOfferIdRouteWithChildren
  '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary': typeof SecuredLocalePagesPageIdProductsProductIdOffersOfferIdSummaryRoute
  '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/': typeof SecuredLocalePagesPageIdProductsProductIdOffersOfferIdIndexRoute
}

export interface FileRoutesByTo {
  '/': typeof IndexRoute
  '/public/$locale': typeof PublicLocaleIndexRoute
  '/secured/$locale/views/$viewId/$subjectId': typeof SecuredLocaleViewsViewIdSubjectIdRoute
  '/secured/$locale/views/$viewId': typeof SecuredLocaleViewsViewIdIndexRoute
  '/public/$locale/pages/$pageId/products/$productId': typeof PublicLocalePagesPageIdProductsProductIdRouteWithChildren
  '/secured/$locale/pages/$pageId/products/$productId': typeof SecuredLocalePagesPageIdProductsProductIdIndexRoute
  '/public/$locale/pages/$pageId/products/$productId/offers/$offerId': typeof PublicLocalePagesPageIdProductsProductIdOffersOfferIdRoute
  '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary': typeof SecuredLocalePagesPageIdProductsProductIdOffersOfferIdSummaryRoute
  '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId': typeof SecuredLocalePagesPageIdProductsProductIdOffersOfferIdIndexRoute
}

export interface FileRoutesById {
  __root__: typeof rootRoute
  '/': typeof IndexRoute
  '/public/$locale': typeof PublicLocaleRouteWithChildren
  '/public/$locale/': typeof PublicLocaleIndexRoute
  '/secured/$locale/views/$viewId': typeof SecuredLocaleViewsViewIdRouteWithChildren
  '/secured/$locale/views/$viewId/$subjectId': typeof SecuredLocaleViewsViewIdSubjectIdRoute
  '/secured/$locale/views/$viewId/': typeof SecuredLocaleViewsViewIdIndexRoute
  '/public/$locale/pages/$pageId/products/$productId': typeof PublicLocalePagesPageIdProductsProductIdRouteWithChildren
  '/secured/$locale/pages/$pageId/products/$productId': typeof SecuredLocalePagesPageIdProductsProductIdRouteWithChildren
  '/secured/$locale/pages/$pageId/products/$productId/': typeof SecuredLocalePagesPageIdProductsProductIdIndexRoute
  '/public/$locale/pages/$pageId/products/$productId/offers/$offerId': typeof PublicLocalePagesPageIdProductsProductIdOffersOfferIdRoute
  '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId': typeof SecuredLocalePagesPageIdProductsProductIdOffersOfferIdRouteWithChildren
  '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary': typeof SecuredLocalePagesPageIdProductsProductIdOffersOfferIdSummaryRoute
  '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/': typeof SecuredLocalePagesPageIdProductsProductIdOffersOfferIdIndexRoute
}

export interface FileRouteTypes {
  fileRoutesByFullPath: FileRoutesByFullPath
  fullPaths:
    | '/'
    | '/public/$locale'
    | '/public/$locale/'
    | '/secured/$locale/views/$viewId'
    | '/secured/$locale/views/$viewId/$subjectId'
    | '/secured/$locale/views/$viewId/'
    | '/public/$locale/pages/$pageId/products/$productId'
    | '/secured/$locale/pages/$pageId/products/$productId'
    | '/secured/$locale/pages/$pageId/products/$productId/'
    | '/public/$locale/pages/$pageId/products/$productId/offers/$offerId'
    | '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId'
    | '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary'
    | '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/'
  fileRoutesByTo: FileRoutesByTo
  to:
    | '/'
    | '/public/$locale'
    | '/secured/$locale/views/$viewId/$subjectId'
    | '/secured/$locale/views/$viewId'
    | '/public/$locale/pages/$pageId/products/$productId'
    | '/secured/$locale/pages/$pageId/products/$productId'
    | '/public/$locale/pages/$pageId/products/$productId/offers/$offerId'
    | '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary'
    | '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId'
  id:
    | '__root__'
    | '/'
    | '/public/$locale'
    | '/public/$locale/'
    | '/secured/$locale/views/$viewId'
    | '/secured/$locale/views/$viewId/$subjectId'
    | '/secured/$locale/views/$viewId/'
    | '/public/$locale/pages/$pageId/products/$productId'
    | '/secured/$locale/pages/$pageId/products/$productId'
    | '/secured/$locale/pages/$pageId/products/$productId/'
    | '/public/$locale/pages/$pageId/products/$productId/offers/$offerId'
    | '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId'
    | '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary'
    | '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/'
  fileRoutesById: FileRoutesById
}

export interface RootRouteChildren {
  IndexRoute: typeof IndexRoute
  PublicLocaleRoute: typeof PublicLocaleRouteWithChildren
  SecuredLocaleViewsViewIdRoute: typeof SecuredLocaleViewsViewIdRouteWithChildren
  SecuredLocalePagesPageIdProductsProductIdRoute: typeof SecuredLocalePagesPageIdProductsProductIdRouteWithChildren
}

const rootRouteChildren: RootRouteChildren = {
  IndexRoute: IndexRoute,
  PublicLocaleRoute: PublicLocaleRouteWithChildren,
  SecuredLocaleViewsViewIdRoute: SecuredLocaleViewsViewIdRouteWithChildren,
  SecuredLocalePagesPageIdProductsProductIdRoute:
    SecuredLocalePagesPageIdProductsProductIdRouteWithChildren,
}

export const routeTree = rootRoute
  ._addFileChildren(rootRouteChildren)
  ._addFileTypes<FileRouteTypes>()

/* prettier-ignore-end */

/* ROUTE_MANIFEST_START
{
  "routes": {
    "__root__": {
      "filePath": "__root.tsx",
      "children": [
        "/",
        "/public/$locale",
        "/secured/$locale/views/$viewId",
        "/secured/$locale/pages/$pageId/products/$productId"
      ]
    },
    "/": {
      "filePath": "index.tsx"
    },
    "/public/$locale": {
      "filePath": "public.$locale.tsx",
      "children": [
        "/public/$locale/",
        "/public/$locale/pages/$pageId/products/$productId"
      ]
    },
    "/public/$locale/": {
      "filePath": "public.$locale.index.tsx",
      "parent": "/public/$locale"
    },
    "/secured/$locale/views/$viewId": {
      "filePath": "secured.$locale.views.$viewId.tsx",
      "children": [
        "/secured/$locale/views/$viewId/$subjectId",
        "/secured/$locale/views/$viewId/"
      ]
    },
    "/secured/$locale/views/$viewId/$subjectId": {
      "filePath": "secured.$locale.views.$viewId.$subjectId.tsx",
      "parent": "/secured/$locale/views/$viewId"
    },
    "/secured/$locale/views/$viewId/": {
      "filePath": "secured.$locale.views.$viewId.index.tsx",
      "parent": "/secured/$locale/views/$viewId"
    },
    "/public/$locale/pages/$pageId/products/$productId": {
      "filePath": "public.$locale.pages.$pageId.products.$productId.tsx",
      "parent": "/public/$locale",
      "children": [
        "/public/$locale/pages/$pageId/products/$productId/offers/$offerId"
      ]
    },
    "/secured/$locale/pages/$pageId/products/$productId": {
      "filePath": "secured.$locale.pages.$pageId.products.$productId.tsx",
      "children": [
        "/secured/$locale/pages/$pageId/products/$productId/",
        "/secured/$locale/pages/$pageId/products/$productId/offers/$offerId"
      ]
    },
    "/secured/$locale/pages/$pageId/products/$productId/": {
      "filePath": "secured.$locale.pages.$pageId.products.$productId.index.tsx",
      "parent": "/secured/$locale/pages/$pageId/products/$productId"
    },
    "/public/$locale/pages/$pageId/products/$productId/offers/$offerId": {
      "filePath": "public.$locale.pages.$pageId.products.$productId.offers.$offerId.tsx",
      "parent": "/public/$locale/pages/$pageId/products/$productId"
    },
    "/secured/$locale/pages/$pageId/products/$productId/offers/$offerId": {
      "filePath": "secured.$locale.pages.$pageId.products.$productId.offers.$offerId.tsx",
      "parent": "/secured/$locale/pages/$pageId/products/$productId",
      "children": [
        "/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary",
        "/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/"
      ]
    },
    "/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary": {
      "filePath": "secured.$locale.pages.$pageId.products.$productId.offers.$offerId.summary.tsx",
      "parent": "/secured/$locale/pages/$pageId/products/$productId/offers/$offerId"
    },
    "/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/": {
      "filePath": "secured.$locale.pages.$pageId.products.$productId.offers.$offerId.index.tsx",
      "parent": "/secured/$locale/pages/$pageId/products/$productId/offers/$offerId"
    }
  }
}
ROUTE_MANIFEST_END */
