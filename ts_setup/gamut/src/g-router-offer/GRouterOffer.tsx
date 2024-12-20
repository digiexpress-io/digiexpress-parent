import React from 'react';

import { Container, Toolbar, useThemeProps } from '@mui/material';

import {
  GShell,
  GFooter,
  GShellClassName,
  GForm,
  GLogo,
  GLayout,
  useIam
} from '../';
import { useNavigate } from '@tanstack/react-router';
import { GRouterOfferRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';


export interface GRouterOfferProps {
  offerId: string;
  formId: string;
  productId: string;
}


export const GRouterOffer: React.FC<GRouterOfferProps> = (initProps) => {
  const nav = useNavigate();
  const anon = useIam();
  const anonymousUser = anon.authType === 'ANON';


  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const { formId, productId } = props;
  const classes = useUtilityClasses();

  function onAfterComplete() {
    if (anonymousUser) {
      // TODO::: dont know where 
      alert('TODO::: route somewhere');
    }
    else {
      nav({
        from: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId',
        to: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary',
      })
    }
  }


  return (
    <GShell drawerOpen={false}>
      <GRouterOfferRoot className={classes.root}>
        <Toolbar className={GShellClassName}>
          <GLayout variant='toolbar-n-rows-2-columns'>
            <GLogo variant='black_lg' />
          </GLayout>
        </Toolbar>

        <main role='main'>
          <Container>
            <GForm variant={productId} onAfterComplete={onAfterComplete}>{formId}</GForm>
          </Container>
        </main>

        <footer role='footer'>
          <GFooter />
        </footer>
      </GRouterOfferRoot>
    </GShell>

  );
}