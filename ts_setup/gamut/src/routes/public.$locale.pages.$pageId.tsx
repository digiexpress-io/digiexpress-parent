import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '../api-locale';
import { RouterUnsecured } from '../g-routes';

export const Route = createFileRoute(
  '/public/$locale/pages/$pageId',
)({
  component: Component,
})

function Component() {
  const { pageId } = Route.useParams()
  return React.useMemo(() => (<ChooseComponent pageId={pageId} /> ), [pageId])
}

function ChooseComponent(props: {pageId: string}) {
  return (<RouterUnsecured pageId={props.pageId}/>)
}
