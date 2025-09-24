import { createRootRoute, Outlet } from '@tanstack/react-router';
import React from 'react';
import { useIntl } from 'react-intl';

import { EveliError, EveliErrorNotFound } from '@dxs-ts/eveli-primitives';

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
