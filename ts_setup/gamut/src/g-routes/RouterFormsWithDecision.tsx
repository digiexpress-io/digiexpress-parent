import React from 'react';
import { Avatar, Box, Breadcrumbs, Container, Divider, Drawer, Link, Typography, useTheme } from '@mui/material';
import HourglassBottomIcon from '@mui/icons-material/HourglassBottom';
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
  GContracts,
} from '../';



export interface RouterFormsWithDecisionProps {
  locale: string;
  viewId: GUserOverviewMenuView;
  color?: string | undefined;
}

export const RouterFormsWithDecision: React.FC<RouterFormsWithDecisionProps> = (props) => {
  const intl = useIntl();
  const nav = useNavigate();
  const theme = useTheme();
  const { locale, color = theme.palette.info.main } = props;


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

  function handleContractItemClick(exchangeId: string) {
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { subjectId: exchangeId },
      to: '/secured/$locale/views/$viewId/$subjectId',
    })
  }

  return (
    <GShell>
      <GAppBar locale={locale} onLocale={handleLocale} onLogoClick={() => handleClick('user-overview')} viewId={props.viewId} />
      <Drawer variant='permanent' open={false} className={GShellClassName}>
        <GUserOverviewMenu onClick={handleClick} defaultView='with-decision' />
      </Drawer>
      <main role='main'>
        <Container>
          <GLayout variant='secured-1-row-1-column'
            slots={{
              breadcrumbs: () => (
                <Breadcrumbs>
                  <Link onClick={() => handleClick('user-overview')}>
                    <HomeIcon />
                    {intl.formatMessage({ id: 'gamut.userOverview.home' })}
                  </Link>
                </Breadcrumbs>
              ),
              topTitle: () => (
                <>
                  <Box display='flex' flexDirection='row' alignItems='center'>
                    <Avatar sx={{ height: '50px', width: '50px', alignContent: 'center', mr: 1, backgroundColor: color }}>
                      <HourglassBottomIcon fontSize='large' />
                    </Avatar>
                    <Typography variant='h1'>{intl.formatMessage({ id: 'gamut.forms.withDecision.title' })}</Typography>
                  </Box>
                  <Typography variant='body1' my={theme.spacing(1)}>{intl.formatMessage({ id: 'gamut.forms.withDecision.subtitle' })}</Typography>
                </>
              ),
              left: () => (<>
                <Divider />
                <GContracts
                  filter={(contract => contract.status === 'COMPLETED' || contract.status === 'REJECTED')}
                  slotProps={{ item: { color, onClick: handleContractItemClick } }}
                />
              </>
              ),
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
