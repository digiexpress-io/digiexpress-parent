import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import {
  RouterUnfinishedForms, RouterFormsAwaitingDecision, RouterFormsWithDecision,
  RouterUserOverview,
  RouterBookings
} from '../g-routes';
import { GRouterSecuredServices } from '../g-router-secured-services';
import { GRouterSecuredServicesSm } from '../g-router-secured-services-sm';

import { GRouterInbox } from '../g-router-inbox';

import { GUserOverviewMenuView } from '../g-user-overview-menu';
import { useLocale } from '../api-locale';

export const Route = createFileRoute('/secured/$locale/views/$viewId/')({
  component: Component,
})

function Component() {
  const { viewId, locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useEffect(() => setLocale(locale), [locale])


  return React.useMemo(() => (<ChooseComponent viewId={viewId as any} locale={locale} />), [viewId, locale])
}


function ChooseComponent(props: { viewId: GUserOverviewMenuView, locale: string }) {
  const { viewId, locale = 'en' } = props;


  if (viewId === 'services') {
    return <GRouterSecuredServices locale={locale} viewId={viewId} />
  } else if (viewId === 'service-select-sm') {
    return <GRouterSecuredServicesSm locale={locale} viewId={viewId} />
  } else if (viewId === 'requests-in-progress') {
    return <RouterUnfinishedForms locale={locale} viewId={viewId} />
  } else if (viewId === 'user-overview') {
    return <RouterUserOverview locale={locale} viewId={viewId} />
  } else if (viewId === 'awaiting-decision') {
    return <RouterFormsAwaitingDecision locale={locale} viewId={viewId} />
  } else if (viewId === 'with-decision') {
    return <RouterFormsWithDecision locale={locale} viewId={viewId} />
  } else if (viewId === 'inbox') {
    return <GRouterInbox locale={locale} viewId={viewId} />
  } else if (viewId === 'bookings') {
    return <RouterBookings locale={locale} viewId={viewId} />
  } 
  return (<>No view defined</>)
}
