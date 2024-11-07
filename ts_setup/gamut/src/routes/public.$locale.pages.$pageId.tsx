import React from 'react'
import { createFileRoute, Outlet } from '@tanstack/react-router'

export const Route = createFileRoute(
  '/public/$locale/pages/$pageId',
)({
  component: Outlet,
})