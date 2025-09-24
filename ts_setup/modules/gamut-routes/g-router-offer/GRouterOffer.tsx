import React from 'react';

import { Container, Toolbar, useThemeProps } from '@mui/material';

import { useIam, useLocale } from '@dxs-ts/gamut-api';
import {
  GFooter,
  GLogo,
  GLayout,
  GFormUnavailable
} from '@dxs-ts/gamut-primitives';
import { GShell, GShellClassName, } from '@dxs-ts/gamut-shell';

import { useNavigate } from '@tanstack/react-router';
import { GRouterOfferRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { GForm } from '@dxs-ts/gamut-form';

export interface GRouterOfferProps {
  offerId: string;
  productId: string;
  slots?: {
    appbar?: {
      right?: React.ElementType<GRouterOfferSlotProps>
    }
  }
}

export type GRouterOfferSlotProps = Omit<GRouterOfferProps, 'slots'>;

const EMPTY_SLOT = () => <></>;

export const GRouterOffer: React.FC<GRouterOfferProps> = (initProps) => {
  const nav = useNavigate();
  const anon = useIam();
  const { locale } = useLocale();
  const anonymousUser = anon.authType === 'ANON';


  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const { productId } = props;
  const classes = useUtilityClasses();

  function onAfterComplete() {
    if (anonymousUser) {
      nav({
        from: '/public/$locale/pages/$pageId/products/$productId/offers/$offerId',
        to: '/public/$locale/pages/$pageId/products/$productId/offers/$offerId/summary',
      })
    }
    else {
      nav({
        from: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId',
        to: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary',
      })
    }
  }

  


  const onCancel = React.useCallback(() => {
    if (anonymousUser) {
      nav({
        from: '/public/$locale/pages/$pageId/products/$productId',
        params: { locale },
        to: '/public/$locale'
      })
    } else {
      nav({
        from: '/secured/$locale/pages/$pageId/products/$productId',
        params: { viewId: 'user-overview' },
        to: '/secured/$locale/views/$viewId'
      })
    }
  }, [anonymousUser, locale]);

  const RightSlot: React.ElementType<GRouterOfferSlotProps> = props.slots?.appbar?.right ? props.slots?.appbar?.right : EMPTY_SLOT;

  return (
    <GShell drawerOpen={false}>
      <GRouterOfferRoot className={classes.root}>
        <Toolbar className={GShellClassName}>
          <GLayout variant='toolbar-n-rows-2-columns'>
            <GLogo variant='black_lg' />
            <RightSlot {...props} />
          </GLayout>
        </Toolbar>

        <main role='main'>
          <Container>
            <GForm 
              executionId={props.offerId} variant={productId} 
              formUnavailable={GFormUnavailable}
              onAfterComplete={onAfterComplete}
              onCancel={onCancel}
            />
          </Container>
        </main>

        <footer role='footer'>
          <GFooter />
        </footer>
      </GRouterOfferRoot>
    </GShell>

  );
}