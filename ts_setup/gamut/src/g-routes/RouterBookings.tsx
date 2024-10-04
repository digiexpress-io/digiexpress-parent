import React from 'react';
import { Avatar, Box, Breadcrumbs, Container, Divider, Drawer, Link, Typography, useTheme } from '@mui/material';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import HomeIcon from '@mui/icons-material/Home';
import { useNavigate } from '@tanstack/react-router';
import { useIntl } from 'react-intl';

import {
  GShell,
  GLayout,
  GFooter,
  GShellClassName,
  GUserOverviewMenuView,
  GAppBar,
  GUserOverviewMenu,
  GBookings,
} from '../';



export interface RouterBookingsProps {
  locale: string;
  viewId: GUserOverviewMenuView;
  color?: string | undefined;
}

export const RouterBookings: React.FC<RouterBookingsProps> = (props) => {
  const intl = useIntl();
  const theme = useTheme();
  const nav = useNavigate();


  const { locale, color = theme.palette.secondary.main } = props;

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


  return (
    <GShell>
      <GAppBar locale={locale} onLocale={handleLocale} onLogoClick={() => handleClick('user-overview')} viewId={props.viewId} />
      <Drawer variant='permanent' open={false} className={GShellClassName}>
        <GUserOverviewMenu onClick={handleClick} defaultView='bookings' />
      </Drawer>
      <main role='main'>
        <Container>
          <GLayout variant='secured-1-row-1-column'
            slots={{
              breadcrumbs: () => (
                <Breadcrumbs>
                  <Link onClick={() => handleClick('user-overview')}>
                    <HomeIcon />
                    {intl.formatMessage({ id: 'gamut.userOverview.home' })}</Link>
                </Breadcrumbs>
              ),
              topTitle: () => (<>
                <Box display='flex' flexDirection='row' alignItems='center'>
                  <Avatar sx={{ height: '50px', width: '50px', alignContent: 'center', mr: 1, backgroundColor: color }}>
                    <CalendarMonthIcon fontSize='large' />
                  </Avatar>
                  <Typography variant='h1'>{intl.formatMessage({ id: 'gamut.bookings.title' })}</Typography>
                </Box>
                <Typography variant='body1' my={theme.spacing(1)}>{intl.formatMessage({ id: 'gamut.bookings.subtitle' })}</Typography>
              </>
              ),
              left: () => (<>
                <Divider />
                <GBookings
                  slotProps={{ item: { color, onClick: () => console.log('booking click') } }}
                />
              </>
              )
            }}
          />
        </Container>
      </main>
      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell>
  );
}

