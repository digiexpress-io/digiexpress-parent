import { createFileRoute, Outlet } from '@tanstack/react-router'


export const Route = createFileRoute('/secured/$locale/forms/$formId')({
  component: Outlet,
})



