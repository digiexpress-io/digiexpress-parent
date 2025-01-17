import React from 'react';
import { Container, Drawer, Link, Typography } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';

import { useNavigate } from '@tanstack/react-router';
import { useIntl } from 'react-intl';

import {
  GShell,
  GLayout,
  GFooter,
  GShellClassName,
  SiteApi,
  GAppBar,
  useSite,
  GSecuredServices,
  GSecuredServicesSearch,
  GUserOverviewMenuView,
  GArticle
} from '../';
import { GRouterSecuredServicesBreadcrumbsRoot, GRouterSecuredServicesRoot, useUtilityClasses } from './useUtilityClasses';



export interface GRouterSecuredServicesProps {
  locale: string;
  viewId: GUserOverviewMenuView;
}

export const GRouterSecuredServices: React.FC<GRouterSecuredServicesProps> = ({ locale, viewId }) => {
  const { views } = useSite();
  const intl = useIntl();
  const classes = useUtilityClasses();

  const [topic, setTopic] = React.useState<SiteApi.TopicView>(views['000_index']);
  const handleClick: (newViewId: GUserOverviewMenuView | undefined) => void = React.useCallback((newViewId) => {
    if (!newViewId) { // i.e. --> login/logout buttons
      return;
    }
    nav({
      from: '/secured/$locale/views/$newViewId',
      params: { newViewId },
      to: '/secured/$locale/views/$newViewId',
    })
  }, []);

  const Article = React.useCallback(() => (topic ? <GArticle>{topic}</GArticle> : <></>), [topic]);
  const Nav = React.useCallback(() => {
    const classes = useUtilityClasses();
    return (
      <GRouterSecuredServicesBreadcrumbsRoot className={classes.root}>
        <Link onClick={() => handleClick('user-overview')}>
          <HomeIcon />
          {intl.formatMessage({ id: 'gamut.userOverview.home' })}
        </Link>
        <Typography>
          {intl.formatMessage({ id: 'gamut.services' })}
        </Typography>
        <Typography>
          {topic.name}
        </Typography>
      </GRouterSecuredServicesBreadcrumbsRoot>);
  }, [topic, viewId]);


  const topics = Object.values(views);
  function handleOnTopic(topic: SiteApi.TopicView) {
    setTopic(topic)
  }

  const nav = useNavigate();
  function handleLocale(locale: string) {
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { locale },

      to: '/secured/$locale/views/$viewId',
    })
  }

  return (
    <GShell>
      <GAppBar locale={locale} onLocale={handleLocale} onLogoClick={() => handleClick('user-overview')} viewId={viewId} />
      <Drawer variant='permanent' open={false} className={GShellClassName}>
        <GSecuredServicesSearch id='gamut.search.placeholder' />
        {topics.map((topic) => (<GSecuredServices key={topic.id} onClick={(_event) => handleOnTopic(topic)}>{topic.name}</GSecuredServices>))}
      </Drawer>
      <main role='main'>
        <Container>
          <GRouterSecuredServicesRoot className={classes.root}>
            <GLayout variant='secured-1-row-1-column' slots={{ breadcrumbs: Nav, left: Article }} />
          </GRouterSecuredServicesRoot>
        </Container>
      </main>
      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell>
  );
}

