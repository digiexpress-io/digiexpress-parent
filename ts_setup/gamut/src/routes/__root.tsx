import { createRootRoute, Outlet } from '@tanstack/react-router'
import React from 'react';
import { useIntl } from 'react-intl';


export const Route = createRootRoute({
  component: RouteComponent,
  notFoundComponent: () => {
    return (
      <div>
        <p>Page Not found!</p>
      </div>
    )
  },
})

function RouteComponent() {
  const intl = useIntl();
  const title = intl.formatMessage({ id: 'document.title' });

  React.useEffect(() => {
    document.title = title;
  }, [title])

  return <Outlet />
}



