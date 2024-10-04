import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { GUserOverviewMenuView } from '../g-user-overview-menu';
import { RouterInboxSubject, RouterServiceSelected } from '../g-routes';


export const Route = createFileRoute('/secured/$locale/views/$viewId/$subjectId')({
  component: Component,
})

function Component() {
  const { locale, subjectId, viewId } = Route.useParams();
  return React.useMemo(() => (<ChooseComponent viewId={viewId as any} locale={locale} subjectId={subjectId} />), [locale])
}

function ChooseComponent(props: { locale: string, viewId: GUserOverviewMenuView, subjectId: string }) {


  if (props.viewId === 'service-select') {
    return (<RouterServiceSelected locale={props.locale} viewId={props.viewId} serviceId={props.subjectId} />)
  } 
  return (<RouterInboxSubject locale={props.locale} viewId={props.viewId} subjectId={props.subjectId} />)
}
