import React from 'react'
import { GUserOverviewMenuView } from '../g-user-overview-menu';
import { GRouterInboxSubject } from '../g-router-inbox-subject';

export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { locale, subjectId, viewId } = Route.useParams();
  return React.useMemo(() => (<ChooseComponent viewId={viewId as any} locale={locale} subjectId={subjectId} />), [locale])
}

function ChooseComponent(props: { locale: string, viewId: GUserOverviewMenuView, subjectId: string }) {
  return (<GRouterInboxSubject locale={props.locale} viewId={props.viewId} subjectId={props.subjectId} />)
}
