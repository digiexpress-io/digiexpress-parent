import { createFileRoute, Outlet } from '@tanstack/react-router'


export const Route = createFileRoute('/secured/$locale/pages/$pageId/products/$productId/offers/$offerId')({
  component: Outlet,
})