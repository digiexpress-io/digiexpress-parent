import React from 'react';
import { Container, Divider, Drawer, useThemeProps } from '@mui/material';
import { useNavigate } from '@tanstack/react-router';
import { useIntl } from 'react-intl';

import {
  GLayout,
  GFooter,
  GUserOverviewMenuView,
  GAppBar,
  GUserOverviewMenu,
  GContracts,
  GSort,
} from '@dxs-ts/gamut-primitives';

import { useContracts } from '@dxs-ts/gamut-api';
import { GShell, GShellClassName, } from '@dxs-ts/gamut-shell';
import { AwaitingDecisionsBreadcrumbs, AwaitingDecisionTitle, GRouterFormsAwaitingDecisionRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';


export interface GRouterFormsAwaitingDecisionProps {
  locale: string;
  viewId: GUserOverviewMenuView;
  color?: string | undefined;
}

export const GRouterFormsAwaitingDecision: React.FC<GRouterFormsAwaitingDecisionProps> = (initProps) => {
  const nav = useNavigate();
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { toggleContractSortOrder, sortOrder } = useContracts();

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

  const breadcrumbs = React.useCallback(() => <AwaitingDecisionsBreadcrumbs />, []);
  const topTitle = React.useCallback(() => <AwaitingDecisionTitle />, []);

  return (
    <GShell>
      <GAppBar locale={props.locale} onLocale={handleLocale} onLogoClick={() => handleClick('user-overview')} viewId={props.viewId} />
      <Drawer variant='permanent' open={false} className={GShellClassName}>
        <GUserOverviewMenu onClick={handleClick} defaultView='awaiting-decision' />
      </Drawer>
      <main role='main'>
        <Container>
          <GRouterFormsAwaitingDecisionRoot className={classes.root}>
            <GLayout variant='secured-1-row-1-column'
              slots={{
                breadcrumbs,
                topTitle,
                left: () => (<>
                  <Divider />
                  <GSort onClick={toggleContractSortOrder} direction={sortOrder} label={intl.formatMessage({ id: 'gamut.buttons.sort-last-modified' })} />
                  <GContracts
                    filter={(contract => contract.status === 'OPEN' || contract.status === 'NEW' || contract.status === 'WAITING' || contract.status === 'TRANSFERRED')}
                    slotProps={{ item: { onClick: handleContractItemClick } }}
                  />
                </>
                ),
              }}
            />
          </GRouterFormsAwaitingDecisionRoot>
        </Container>
      </main>
      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell>
  );
}
