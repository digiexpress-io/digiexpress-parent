import React from 'react';
import { Container, Toolbar } from '@mui/material';


import {
  GShell,
  GFooter,
  GLayout,
  GLogo,
  GShellClassName,
} from '@dxs-ts/gamut-primitives';

import { GRouterProductRoot } from './useUtilityClasses';

import { GRouterProductProps, useGRouterProducState } from './g-router-product-types'
import { GRouterProductBreadcrumbs } from './GRouterProductBreadcrumbs';
import { GRouterProductContent } from './GRouterProductContent';
import { GRouterProductActions } from './GRouterProductActions';



export const GRouterProduct: React.FC<GRouterProductProps> = (props) => {
  const { ownerState } = useGRouterProducState(props);

  return (
    <GShell drawerOpen={false}>
      <Toolbar className={GShellClassName} >
        <GLayout variant='toolbar-n-rows-2-columns'>
          <GLogo variant='black_lg' />
        </GLayout>
      </Toolbar>

      <main role='main'>
        <Container>
          <GRouterProductRoot>
            <GLayout variant='fill-session-start-end'
              slots={{
                breadcrumbs: () => (<GRouterProductBreadcrumbs ownerState={ownerState} />),
                topTitle: () => <GRouterProductContent ownerState={ownerState} />,
                center: () => <GRouterProductActions ownerState={ownerState} />
              }}>
            </GLayout>
          </GRouterProductRoot>
        </Container>
      </main>

      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell >
  );
}

