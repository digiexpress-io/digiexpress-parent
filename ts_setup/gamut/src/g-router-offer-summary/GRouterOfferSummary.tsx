import React from 'react';
import { Container, Toolbar, useThemeProps } from '@mui/material';
import { useNavigate } from '@tanstack/react-router';

import {
  GShell,
  GFooter,
  GShellClassName,
  GLayout,
  useSite,
  GLogo,
  useIam,
  useOffers
} from '../';

import { GRouterOfferSummaryRoot, MUI_NAME, useUtilityClasses, SummaryBox } from './useUtilityClasses';


export interface GRouterOfferSummaryProps {
  offerId: string;
  productId: string;
  pageId: string;
  locale: string;
}

export const GRouterOfferSummary: React.FC<GRouterOfferSummaryProps> = (initProps) => {
  const { refresh } = useOffers();
  const nav = useNavigate();
  const anon = useIam();
  const site = useSite();

  const anonymousUser = anon.authType === 'ANON';
  const buttonBackToMsg = anonymousUser ? 'gamut.public.forms.summary.button.backToServicesHome' : 'gamut.forms.summary.button.back-to-overview';

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const { locale } = props;

  const topic = site.views[props.pageId];
  const topicLink = topic.links.find(l => l.id === props.productId)

  const classes = useUtilityClasses();


  function navBack() {
    if (anonymousUser) {
      nav({
        from: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary',
        params: { locale },
        to: '/public/$locale'
      })
    }
    else {
      refresh();
      nav({
        from: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary',
        params: { viewId: 'user-overview' },
        to: '/secured/$locale/views/$viewId',
      })
    }
  }


  return (
    <GShell drawerOpen={false}>
      <Toolbar className={GShellClassName} >
        <GLayout variant='toolbar-n-rows-2-columns'>
          <GLogo variant='black_lg' />
        </GLayout>
      </Toolbar>

      <main role='main'>
        <Container>
          <GRouterOfferSummaryRoot className={classes.root}>
            <SummaryBox topicLink={topicLink} buttonBackToMsg={buttonBackToMsg} onNav={navBack} />
          </GRouterOfferSummaryRoot>
        </Container>
      </main>

      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell >

  );
}