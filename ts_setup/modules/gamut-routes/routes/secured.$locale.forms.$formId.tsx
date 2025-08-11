import { Outlet, createFileRoute } from '@tanstack/react-router'


export const Route = createFileRoute('/secured/$locale/forms/$formId')({
  component: Outlet,
})



