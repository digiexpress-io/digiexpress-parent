import React from 'react';
import { Container, Drawer } from '@mui/material';
import { useNavigate } from '@tanstack/react-router';

import { Bread, Left, Top, useOwnerState } from './useUtilityClasses';
import { GAppBar, GFooter, GLayout, GShell, GShellClassName, GUserOverviewMenu, GUserOverviewMenuView } from '../../';


export interface RouterInboxSubjectProps {
  locale: string;
  viewId: GUserOverviewMenuView;
  subjectId: string;
}

export const RouterInboxSubject: React.FC<RouterInboxSubjectProps> = ({ locale, subjectId, viewId }) => {

  const nav = useNavigate();
  const ownerState = useOwnerState(subjectId);
  const slots = React.useMemo(() => (
    ownerState.isPending ?
      {} :
      {
        breadcrumbs: () => <Bread ownerState={ownerState} />,
        topTitle: () => <Top ownerState={ownerState} />,
        left: () => <Left ownerState={ownerState} />
      }
  ), [ownerState]);

  function handleLocale(locale: string) {
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { locale },
      to: '/secured/$locale/views/$viewId',
    })
  }

  return (
    <GShell>
      <GAppBar locale={locale} onLocale={handleLocale} onLogoClick={() => ownerState.onNav('user-overview')} viewId={viewId} />
      <Drawer variant='permanent' open={false} className={GShellClassName}>
        <GUserOverviewMenu onClick={ownerState.onNav} defaultView='inbox' />
      </Drawer>
      <main role='main'>
        <Container><GLayout variant='secured-1-row-1-column' slots={slots} /></Container>
      </main>
      <footer role='footer'><GFooter /></footer>
    </GShell>
  );
}



