import { Outlet, createFileRoute } from '@tanstack/react-router'



export const Route = createFileRoute('/public/$locale/pages/$pageId/products/$productId')({
  component: Outlet
})