import React from 'react';
import { Container, Divider, Drawer, useThemeProps } from '@mui/material';
import { useNavigate } from '@tanstack/react-router';

import {
  GLayout,
  GFooter,
  GUserOverviewMenuView,
  GAppBar,
  GUserOverviewMenu,
  GOffers
} from '@dxs-ts/gamut-primitives';
import { GShell, GShellClassName, } from '@dxs-ts/gamut-shell';

import { GRouterUnfinishedFormsRoot, MUI_NAME, UnfinishedFormsBreadcrumbs, UnfinishedFormsTitle, useUtilityClasses } from './useUtilityClasses';
import { useRouteToOffer } from '../g-router-offer';



export interface GRouterUnfinishedFormsProps {
  locale: string;
  viewId: GUserOverviewMenuView;
}

export const GRouterUnfinishedForms: React.FC<GRouterUnfinishedFormsProps> = (props) => {
  const nav = useNavigate();
  const classes = useUtilityClasses();
  const { onOpenOffer } = useRouteToOffer();

  const ownerState = useThemeProps({
    props: props,
    name: MUI_NAME,
  });

  function handleLocale(locale: string) {
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { locale },

      to: '/secured/$locale/views/$viewId',
    })
  }
  function handleNav(viewId: GUserOverviewMenuView | undefined) {
    if (!viewId) { // i.e. --> login/logout buttons
      return;
    }
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { viewId },
      to: '/secured/$locale/views/$viewId',
    })
  }



  const breadcrumbs = React.useCallback(() => <UnfinishedFormsBreadcrumbs onClick={() => handleNav('user-overview')} />, []);
  const topTitle = React.useCallback(() => <UnfinishedFormsTitle />, []);
  const left = () => (
    <>
      <Divider />
      <GOffers slotProps={{ item: { onOpen: onOpenOffer } }} />
    </>
  );


  return (
    <GShell>
      <GAppBar locale={ownerState.locale} onLocale={handleLocale} onLogoClick={() => handleNav('user-overview')} viewId={props.viewId} />
      <Drawer variant='permanent' open={false} className={GShellClassName}>
        <GUserOverviewMenu onClick={handleNav} defaultView='requests-in-progress' />
      </Drawer>
      <main role='main'>
        <Container>
          <GRouterUnfinishedFormsRoot className={classes.root} ownerState={ownerState}>
            <GLayout variant='secured-1-row-1-column' slots={{ breadcrumbs, topTitle, left }} />
          </GRouterUnfinishedFormsRoot>
        </Container>
      </main>
      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell>
  );
}

