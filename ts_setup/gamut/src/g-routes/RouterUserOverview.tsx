import React from 'react';
import { Container, Divider, Drawer, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
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
} from '../';


export interface RouterUserOverviewProps {
  locale: string;
  viewId: GUserOverviewMenuView;
}

export const RouterUserOverview: React.FC<RouterUserOverviewProps> = ({ locale, viewId }) => {
  const intl = useIntl();
  const { contractStats } = useContracts();
  const { offers } = useOffers();
  const { subjects } = useComms();
  const nav = useNavigate();

  const { views } = useSite();
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



  return (
    <GShell>
      <GAppBar locale={locale} onLocale={handleLocale} onLogoClick={() => handleClick('user-overview')} viewId={viewId} />
      <Drawer variant='permanent' open={false} className={GShellClassName}>
        <GUserOverviewMenu onClick={handleClick} defaultView='user-overview' />
      </Drawer>
      <main role='main'>
        <Container>
          <GLayout variant='secured-1-row-1-column'
            slots={{
              topTitle: () => (<>
                <Typography variant='h1'>{intl.formatMessage({ id: 'gamut.userOverview.welcome.title' })}</Typography>
                <Typography variant='body1'>{intl.formatMessage({ id: 'gamut.userOverview.welcome.desc' })}</Typography>
                <Divider />
              </>
              ),
              left: () => (
                <GUserOverview
                  topicCount={topicCount}
                  startedForms={offers.length}
                  waitingForms={contractStats.awaitingDecision}
                  decidedForms={contractStats.decided}
                  newMessages={unreadMessages.length}
                  bookings={0}
                  userName={'Antero Asiakas'}
                  userAddress={'1234 Pine street'}
                  userCityAndCountry={'Helsinki, Finland'}
                  userZipcode={'7688DF-A'}
                />
              )
            }} />
        </Container>
      </main>
      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell>
  );
}

