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
  GInbox,
} from '../';
import { Bread, GRouterInboxRoot, MUI_NAME, Top, useUtilityClasses } from './useUtilityClasses';




export interface GRouterInboxProps {
  locale: string;
  viewId: GUserOverviewMenuView;
}

export const GRouterInbox: React.FC<GRouterInboxProps> = (initProps) => {
  const nav = useNavigate();

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const { locale, viewId } = props;
  const classes = useUtilityClasses();


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
  function handleLocale(locale: string) {
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { locale },

      to: '/secured/$locale/views/$viewId',
    })
  }
  function handleSubjectClick(subjectId: string) {
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { subjectId },
      to: '/secured/$locale/views/$viewId/$subjectId',
    })
  }

  function handleAttachmentClick(subjectId: string, attachmentId: string) {

  }

  return (
    <GShell>
      <GRouterInboxRoot className={classes.root}>
        <GAppBar locale={locale} onLocale={handleLocale} onLogoClick={() => handleNav('user-overview')} viewId={viewId} />
        <Drawer variant='permanent' open={false} className={GShellClassName}>
          <GUserOverviewMenu onClick={handleNav} defaultView='inbox' />
        </Drawer>
        <main role='main'>
          <Container>
            <GLayout variant='secured-1-row-1-column'
              slots={{
                breadcrumbs: () => <Bread />,
                topTitle: () => <Top />,
                left: () => (<>
                  <Divider />
                  <GInbox slotProps={{
                    attachment: { onClick: handleAttachmentClick },
                    item: { onClick: handleSubjectClick },
                    formReview: {}
                  }}
                  />
                </>
                )
              }} />
          </Container>
        </main>
        <footer role='footer'>
          <GFooter />
        </footer>
      </GRouterInboxRoot>
    </GShell >
  );
}

