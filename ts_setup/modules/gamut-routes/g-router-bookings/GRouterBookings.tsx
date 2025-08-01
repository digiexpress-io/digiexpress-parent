import React from 'react';
import { Container, Divider, Drawer, useThemeProps } from '@mui/material';
import { useNavigate } from '@tanstack/react-router';

import {
  GShell,
  GLayout,
  GFooter,
  GShellClassName,
  GUserOverviewMenuView,
  GAppBar,
  GUserOverviewMenu,
  GBookings,
} from '@dxs-ts/gamut-primitives';
import { BookingsBreadcrumbs, BookingsTitle, GRouterBookingsRoot, MUI_NAME } from './useUtilityClasses';
import { useUtilityClasses } from './useUtilityClasses';



export interface GRouterBookingsProps {
  locale: string;
  viewId: GUserOverviewMenuView;
}

export const GRouterBookings: React.FC<GRouterBookingsProps> = (initProps) => {
  const nav = useNavigate();
  const classes = useUtilityClasses();


  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  function handleLocale(locale: string) {
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { locale },

      to: '/secured/$locale/views/$viewId',
    })
  }
  function handleClick(viewId: GUserOverviewMenuView | undefined) {
    if (!viewId) { // i.e. --> login/logout buttons
      return;
    }
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { viewId },
      to: '/secured/$locale/views/$viewId',
    })
  }

  const breadcrumbs = React.useCallback(() => <BookingsBreadcrumbs />, []);
  const topTitle = React.useCallback(() => <BookingsTitle />, []);

  return (
    <GShell>
      <GAppBar locale={props.locale} onLocale={handleLocale} onLogoClick={() => handleClick('user-overview')} viewId={props.viewId} />
      <Drawer variant='permanent' open={false} className={GShellClassName}>
        <GUserOverviewMenu onClick={handleClick} defaultView='bookings' />
      </Drawer>
      <main role='main'>
        <Container>
          <GRouterBookingsRoot className={classes.root}>
            <GLayout variant='secured-1-row-1-column'
              slots={{
                breadcrumbs,
                topTitle,
                left: () => (
                  <>
                    <Divider />
                    <GBookings slotProps={{ item: { onClick: () => console.log('booking click') } }} />
                  </>
                )
              }}
            />
          </GRouterBookingsRoot>
        </Container>
      </main>
      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell>
  );
}

