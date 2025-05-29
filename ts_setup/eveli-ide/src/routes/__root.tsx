import { createRootRoute, Outlet } from '@tanstack/react-router';
import React from 'react';
import { useIntl } from 'react-intl';

import { EveliError } from '../eveli-error/EveliError';
import { EveliErrorNotFound } from '../eveli-error-not-found/EveliErrorNotFound';

export const Route = createRootRoute({
  component: RouteComponent,
  errorComponent: EveliError,
  notFoundComponent: EveliErrorNotFound,
});

function RouteComponent() {
  const intl = useIntl();
  const title = intl.formatMessage({ id: 'document.title' });

  React.useEffect(() => {
    document.title = title;
  }, [title]);

  return <Outlet />;
}
