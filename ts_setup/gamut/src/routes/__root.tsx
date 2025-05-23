import React from 'react';

import { createRootRoute, Outlet } from '@tanstack/react-router'
import { useIntl } from 'react-intl';

import { GErrorNotFound } from '../g-error-not-found';
import { GError } from '../g-error';

export const Route = createRootRoute({
  component: RouteComponent,
  notFoundComponent: GErrorNotFound,
  errorComponent: GError,
})

function RouteComponent() {
  const intl = useIntl();
  const title = intl.formatMessage({ id: 'document.title' });

  React.useEffect(() => {
    document.title = title;
  }, [title])

  return <Outlet />
}



