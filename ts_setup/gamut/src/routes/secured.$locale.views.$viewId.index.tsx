import React from 'react'
import { GRouterSecuredServices } from '../g-router-secured-services';
import { GRouterInbox } from '../g-router-inbox';
import { GRouterUnfinishedForms } from '../g-router-unfinished-forms';
import { GUserOverviewMenuView } from '../g-user-overview-menu';
import { GRouterUserOverview } from '../g-router-user-overview';
import { GRouterBookings } from '../g-router-bookings';
import { GRouterFormsAwaitingDecision } from '../g-router-forms-awaiting-decision';
import { GRouterFormsWithDecision } from '../g-router-forms-with-decision';
import { useLocale } from '../api-locale';




export interface ViewRouteSearchParams {
  topicId?: string | undefined;
}

export function parseViewRouteSearchParams(search: Record<string, unknown>): ViewRouteSearchParams {
  return { topicId: search['topicId'] as string | undefined }
}


export const Route = createFileRoute({
  component: Component,
  validateSearch: (search: Record<string, unknown>): ViewRouteSearchParams => parseViewRouteSearchParams(search)
})

function Component() {
  const { viewId, locale, } = Route.useParams();
  const { topicId } = Route.useSearch();

  const { setLocale } = useLocale();

  React.useEffect(() => setLocale(locale), [locale])


  return React.useMemo(() => (<ChooseComponent viewId={viewId as any} locale={locale} topicId={topicId} />), [viewId, locale, topicId])
}


function ChooseComponent(props: { viewId: GUserOverviewMenuView, locale: string, topicId: string | undefined }) {
  const { viewId, locale = 'en', topicId } = props;

  if (viewId === 'services') {
    return <GRouterSecuredServices locale={locale} viewId={viewId} activeTopicId={topicId} />
  } else if (viewId === 'requests-in-progress') {
    return <GRouterUnfinishedForms locale={locale} viewId={viewId} />
  } else if (viewId === 'user-overview') {
    return <GRouterUserOverview locale={locale} viewId={viewId} />
  } else if (viewId === 'awaiting-decision') {
    return <GRouterFormsAwaitingDecision locale={locale} viewId={viewId} />
  } else if (viewId === 'with-decision') {
    return <GRouterFormsWithDecision locale={locale} viewId={viewId} />
  } else if (viewId === 'inbox') {
    return <GRouterInbox locale={locale} viewId={viewId} />
  } else if (viewId === 'bookings') {
    return <GRouterBookings locale={locale} viewId={viewId} />
  } 
  return (<>No view defined</>)
}
