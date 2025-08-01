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
  GContracts,
} from '@dxs-ts/gamut-primitives';
import { GRouterFormsWithDecisionRoot, MUI_NAME, useUtilityClasses, WithDecisionBreadcrumbs, WithDecisionTitle } from './useUtilityClasses';



export interface GRouterFormsWithDecisionProps {
  locale: string;
  viewId: GUserOverviewMenuView;
  color?: string | undefined;
}

export const GRouterFormsWithDecision: React.FC<GRouterFormsWithDecisionProps> = (initProps) => {
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

  function handleContractItemClick(exchangeId: string) {
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { subjectId: exchangeId },
      to: '/secured/$locale/views/$viewId/$subjectId',
    })
  }

  const breadcrumbs = React.useCallback(() => <WithDecisionBreadcrumbs />, []);
  const topTitle = React.useCallback(() => <WithDecisionTitle />, [])

  return (
    <GShell>
      <GAppBar locale={props.locale} onLocale={handleLocale} onLogoClick={() => handleClick('user-overview')} viewId={props.viewId} />
      <Drawer variant='permanent' open={false} className={GShellClassName}>
        <GUserOverviewMenu onClick={handleClick} defaultView='with-decision' />
      </Drawer>
      <main role='main'>
        <Container>
          <GRouterFormsWithDecisionRoot className={classes.root}>
            <GLayout variant='secured-1-row-1-column'
              slots={{
                breadcrumbs,
                topTitle,
                left: () => (<>
                  <Divider />
                  <GContracts
                    filter={(contract => contract.status === 'COMPLETED' || contract.status === 'REJECTED')}
                    slotProps={{ item: { onClick: handleContractItemClick } }}
                  />
                </>
                ),
              }}
            />
          </GRouterFormsWithDecisionRoot>
        </Container>
      </main>
      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell>
  );
}
