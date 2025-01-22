import React from 'react';
import { Container, Drawer } from '@mui/material';
import { useNavigate } from '@tanstack/react-router';

import {
  GShell,
  GLayout,
  GFooter,
  GShellClassName,
  GUserOverviewMenuView,
  GAppBar,
  GUserOverview,
  GUserOverviewMenu,
  useContracts,
  useOffers,
  useComms,
  useSite,
  CommsApi,
  useIam,
} from '../';
import { GRouterUserOverviewRoot, UnfinishedFormsTitle, useUtilityClasses } from './useUtilityClasses';


export interface GRouterUserOverviewProps {
  locale: string;
  viewId: GUserOverviewMenuView;
}

export const GRouterUserOverview: React.FC<GRouterUserOverviewProps> = ({ locale, viewId }) => {
  const { contractStats } = useContracts();
  const { offers } = useOffers();
  const { subjects } = useComms();
  const { views } = useSite();
  const iam = useIam();
  const nav = useNavigate();
  const classes = useUtilityClasses();
  const topics = Object.values(views);
  const topicCount = topics.length;

  let unreadMessages: CommsApi.Subject[] = [];
  subjects.forEach((subject): number => {
    if (!subject.isViewed) {
      unreadMessages.push(subject);
    }
    return 0;
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

  const topTitle = React.useCallback(() => <UnfinishedFormsTitle />, []);

  return (
    <GShell>
      <GAppBar locale={locale} onLocale={handleLocale} onLogoClick={() => handleClick('user-overview')} viewId={viewId} />
      <Drawer variant='permanent' open={false} className={GShellClassName}>
        <GUserOverviewMenu onClick={handleClick} defaultView='user-overview' />
      </Drawer>
      <main role='main'>
        <Container>
          <GRouterUserOverviewRoot className={classes.root}>
            <GLayout variant='secured-1-row-1-column'
              slots={{
                topTitle,
                left: () => (
                  <GUserOverview
                    topicCount={topicCount}
                    startedForms={offers.length}
                    waitingForms={contractStats.awaitingDecision}
                    decidedForms={contractStats.decided}
                    newMessages={unreadMessages.length}
                    bookings={0}
                    userName={[iam.user?.firstName, iam.user?.lastName].join(' ')}
                    userAddress={iam.user?.contact.address?.street || ''}
                    userCityAndCountry={[iam.user?.contact.address?.locality, iam.user?.contact.address?.country].join(',')}
                    userZipcode={iam.user?.contact.address?.postalCode || ''}
                  />
                )
              }} />
          </GRouterUserOverviewRoot>
        </Container>
      </main>
      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell>
  );
}

