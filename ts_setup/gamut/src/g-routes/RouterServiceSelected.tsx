import React from 'react';
import { Breadcrumbs, Container, Link, Typography } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';

import { useNavigate } from '@tanstack/react-router';
import { useIntl } from 'react-intl';

import {
  GShell,
  GLayout,
  GFooter,
  GAppBar,
  useSite,
  GMarkdown,
  GUserOverviewMenuView,
  GLinksPage
} from '../';


export interface RouterServiceSelectedProps {
  locale: string;
  viewId: GUserOverviewMenuView;
  serviceId: string
}

export const RouterServiceSelected: React.FC<RouterServiceSelectedProps> = ({ locale, viewId, serviceId }) => {
  const intl = useIntl();

  const { views } = useSite();
  const topics = Object.values(views);
  const topic = topics.find((a) => a.topic.id === serviceId);

  const nav = useNavigate();
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
      <main role='main'>
        <Container>
          <GLayout variant='secured-1-row-1-column-small' slots={{
            breadcrumbs: () => (
              <Breadcrumbs>
                <Link onClick={() => handleClick('user-overview')}>
                  <HomeIcon />
                  {intl.formatMessage({ id: 'gamut.userOverview.home' })}
                </Link>
                <Link onClick={() => handleClick('service-select')}>
                  {intl.formatMessage({ id: 'gamut.services' })}
                </Link>
                <Typography>
                  {topic?.name}
                </Typography>
              </Breadcrumbs>
            ),
            left: () => (
              <>
                <GMarkdown children={topic?.blob?.value} />
                <GLinksPage children={topic} />
              </>
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

