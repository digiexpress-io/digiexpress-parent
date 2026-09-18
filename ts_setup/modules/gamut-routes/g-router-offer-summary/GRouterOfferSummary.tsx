import React from 'react';
import { Container, Toolbar, useThemeProps } from '@mui/material';

import { GFooter, GLayout, GLogo } from '@dxs-ts/gamut-primitives';
import { GShell, GShellClassName } from '@dxs-ts/gamut-shell';
import { GRouterOfferSummaryRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { FormFillSummaryGreeter } from './FormFillSummaryGreeter';


export interface GRouterOfferSummaryProps {
  offerId: string;
  productId: string;
  pageId: string;
  locale: string;
}

export const GRouterOfferSummary: React.FC<GRouterOfferSummaryProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const { locale } = props;
  const classes = useUtilityClasses();

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
            <FormFillSummaryGreeter locale={locale} pageId={props.pageId} productId={props.productId} />
          </GRouterOfferSummaryRoot>
        </Container>
      </main>

      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell >

  );
}