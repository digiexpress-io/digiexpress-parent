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
  GOffers,
  OfferApi,
} from '../';
import { GRouterUnfinishedFormsRoot, MUI_NAME, UnfinishedFormsBreadcrumbs, UnfinishedFormsTitle, useUtilityClasses } from './useUtilityClasses';



export interface GRouterUnfinishedFormsProps {
  locale: string;
  viewId: GUserOverviewMenuView;
}

export const GRouterUnfinishedForms: React.FC<GRouterUnfinishedFormsProps> = (props) => {
  const nav = useNavigate();
  const classes = useUtilityClasses();

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

  function handleOnOpenOffer(offer: OfferApi.Offer) {

    const offerId = offer.id;
    const pageId = offer.pageId;
    const productId = offer.productId;


    if(!!productId) {
      nav({
        from: '/secured/$locale/views/$viewId',
        params: { offerId, pageId, productId },
        to: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId',
      })
    } else if(offer.otherLocales.length > 0) {
      nav({
        from: '/secured/$locale/views/$viewId',
        params: { offerId, pageId, productId: offer.otherLocales[0].productId, locale: offer.otherLocales[0].locale },
        to: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId',
      })
    } else {
      // TODO: polite error msgs
      alert('Form not available!');
    }
  }

  const breadcrumbs = React.useCallback(() => <UnfinishedFormsBreadcrumbs onClick={() => handleNav('user-overview')} />, []);
  const topTitle = React.useCallback(() => <UnfinishedFormsTitle />, []);
  const left = () => (
    <>
      <Divider />
      <GOffers slotProps={{ item: { onOpen: handleOnOpenOffer } }} />
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

