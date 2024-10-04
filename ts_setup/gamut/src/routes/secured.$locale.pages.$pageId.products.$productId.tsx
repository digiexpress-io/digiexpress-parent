import { createFileRoute, Outlet } from '@tanstack/react-router'


export const Route = createFileRoute('/secured/$locale/pages/$pageId/products/$productId')({
  component: Outlet,
})
