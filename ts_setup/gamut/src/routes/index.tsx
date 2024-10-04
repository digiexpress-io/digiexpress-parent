import { createFileRoute, redirect } from '@tanstack/react-router'

// @ts-ignore
export const Route = createFileRoute('/')({
  loader: () => redirect({
    from: '/',
    to: '/secured/$locale/views/$viewId',
    params: { viewId: 'user-overview', locale: 'en' }
  }),

})