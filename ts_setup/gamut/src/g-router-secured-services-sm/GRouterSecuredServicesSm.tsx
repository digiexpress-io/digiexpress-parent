import React from 'react';
import { Box, Breadcrumbs, Container, Divider, Link, Typography } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';

import { useNavigate } from '@tanstack/react-router';
import { useIntl } from 'react-intl';

import {
  GShell,
  GLayout,
  GFooter,
  GAppBar,
  useSite,
  GUserOverviewMenuView,
  SiteApi,
  GSecuredServicesSearch,
  GSecuredServices
} from '../';

// used exclusively for Mobile views

export interface GRouterSecuredServicesSmProps {
  locale: string;
  viewId: GUserOverviewMenuView;
}

export const GRouterSecuredServicesSm: React.FC<GRouterSecuredServicesSmProps> = ({ locale, viewId }) => {
  const { views } = useSite();
  const intl = useIntl();

  const [topic, setTopic] = React.useState<SiteApi.TopicView>(views['000_index']);
  const topics = Object.values(views);


  const nav = useNavigate();
  function handleOnTopic(topic: SiteApi.TopicView) {
    setTopic(topic);
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { subjectId: topic.id },
      to: '/secured/$locale/views/$viewId/$subjectId',
    })
  }

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
          <GLayout variant='secured-1-row-1-column' slots={{
            topTitle: () => (<>
              <Box display='flex' flexDirection='row' alignItems='center'>
                <Typography variant='h1'>{intl.formatMessage({ id: 'gamut.services' })}</Typography>
              </Box>
              <Typography variant='caption'>{intl.formatMessage({ id: 'gamut.services.subtitle' })}</Typography>
            </>
            ),
            breadcrumbs: () => (
              <Breadcrumbs>
                <Link onClick={() => handleClick('user-overview')}>
                  <HomeIcon />
                  {intl.formatMessage({ id: 'gamut.userOverview.home' })}
                </Link>
                <Typography>
                  {intl.formatMessage({ id: 'gamut.services' })}
                </Typography>
              </Breadcrumbs>
            ),
            left: () => <>
              <GSecuredServicesSearch id='gamut.search.placeholder' />
              {topics.map((topic, index) => (<div key={topic.id}>
                <GSecuredServices onClick={(_event) => handleOnTopic(topic)}>{topic.name}</GSecuredServices>
                {index === topics.length - 1 ? <></> : <Divider />}
              </div>
              ))}
            </>,
          }} />
        </Container>
      </main>
      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell>
  );
}

