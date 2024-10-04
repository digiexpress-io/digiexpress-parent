import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import {
  RouterServices,
  RouterUnfinishedForms, RouterFormsAwaitingDecision, RouterFormsWithDecision,
  RouterUserOverview,
  RouterInbox,
  RouterBookings,
  RouterServiceSelect
} from '../g-routes';

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
    return <RouterServices locale={locale} viewId={viewId} />
  } else if (viewId === 'requests-in-progress') {
    return <RouterUnfinishedForms locale={locale} viewId={viewId} />
  } else if (viewId === 'user-overview') {
    return <RouterUserOverview locale={locale} viewId={viewId} />
  } else if (viewId === 'awaiting-decision') {
    return <RouterFormsAwaitingDecision locale={locale} viewId={viewId} />
  } else if (viewId === 'with-decision') {
    return <RouterFormsWithDecision locale={locale} viewId={viewId} />
  } else if (viewId === 'inbox') {
    return <RouterInbox locale={locale} viewId={viewId} />
  } else if (viewId === 'bookings') {
    return <RouterBookings locale={locale} viewId={viewId} />
  } else if (viewId === 'service-select') {
    return <RouterServiceSelect locale={locale} viewId={viewId} />
  }
  return (<>No view defined</>)
}
