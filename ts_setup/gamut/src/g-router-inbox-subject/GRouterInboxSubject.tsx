import React from 'react';
import { Container, Drawer, useThemeProps } from '@mui/material';
import { useNavigate } from '@tanstack/react-router';

import { Bread, GRouterInboxSubjectRoot, Left, MUI_NAME, Top, useOwnerState, useUtilityClasses } from './useUtilityClasses';
import { GAppBar, GFooter, GLayout, GShell, GShellClassName, GUserOverviewMenu, GUserOverviewMenuView } from '../';


export interface RouterInboxSubjectProps {
  locale: string;
  viewId: GUserOverviewMenuView;
  subjectId: string;
}

export const GRouterInboxSubject: React.FC<RouterInboxSubjectProps> = (initProps) => {

  const nav = useNavigate();
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const { locale, subjectId, viewId } = props;
  const classes = useUtilityClasses();
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
      <GRouterInboxSubjectRoot className={classes.root}>
        <GAppBar locale={locale} onLocale={handleLocale} onLogoClick={() => ownerState.onNav('user-overview')} viewId={viewId} />
        <Drawer variant='permanent' open={false} className={GShellClassName}>
          <GUserOverviewMenu onClick={ownerState.onNav} defaultView='inbox' />
        </Drawer>
        <main role='main'>
          <Container><GLayout variant='secured-1-row-1-column' slots={slots} /></Container>
        </main>
        <footer role='footer'><GFooter /></footer>
      </GRouterInboxSubjectRoot>
    </GShell>
  );
}



