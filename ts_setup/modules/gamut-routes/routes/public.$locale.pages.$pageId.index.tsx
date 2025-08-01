import { createFileRoute } from '@tanstack/react-router'
import React from 'react'
import { GRouterUnsecured } from '../g-router-unsecured'

export const Route = createFileRoute('/public/$locale/pages/$pageId/')({
  component: Component,
})

function Component() {
  const { pageId } = Route.useParams()
  return React.useMemo(() => <ChooseComponent pageId={pageId} />, [pageId])
}

function ChooseComponent(props: { pageId: string }) {
  return <GRouterUnsecured pageId={props.pageId} />
}
