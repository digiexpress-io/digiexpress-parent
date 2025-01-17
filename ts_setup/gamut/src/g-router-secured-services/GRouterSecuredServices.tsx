import React from 'react';
import { Box, Chip, Container, Drawer, Grid2, Link, Typography } from '@mui/material';
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
  GSecuredServicesSearch,
  GUserOverviewMenuView,
  GArticle,
  GLinkFormUnsecured,
  GLinkPhone,
  GLinkHyper,
} from '../';
import { GRouterSecuredServicesBreadcrumbsRoot, GRouterSecuredServicesFilterButtonsRoot, GRouterSecuredServicesRoot, useUtilityClasses } from './useUtilityClasses';
import { SearchApi } from '../api-search';



export interface GRouterSecuredServicesProps {
  locale: string;
  viewId: GUserOverviewMenuView;
}

export const GRouterSecuredServices: React.FC<GRouterSecuredServicesProps> = ({ locale, viewId }) => {
  const { views } = useSite();
  const intl = useIntl();
  const classes = useUtilityClasses();

  const [topic, setTopic] = React.useState<SiteApi.TopicView>(views['000_index']);
  const noValueIndicatorColon = intl.formatMessage({ id: 'gamut.noValueIndicatorColon' });
  const [state, setState] = React.useState(SearchApi.getInstance(views, noValueIndicatorColon));


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


  function handleOnTopic(topic: SiteApi.TopicView) {
    setTopic(topic)
  }
  function handleFilterByType(type: SearchApi.FilterMode) {
    setState(prev => prev.filterMode(prev.searchOptionType === type ? 'ALL' : type));
  }
  function handleSecureLink(productId: string, pageId: string) {
    console.log("pageId", pageId)
    nav({
      params: { productId, pageId, locale: intl.locale },
      to: '/secured/$locale/pages/$pageId/products/$productId',
    })
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
        <GSecuredServicesSearch id='gamut.search.placeholder'
          onChange={({ currentTarget }) => setState(prev => prev.find(currentTarget.value))}
        />
        <GRouterSecuredServicesFilterButtonsRoot className={classes.searchFilterButtons}>
          <Chip
            color={state.searchOptionType === 'PHONE_LINKS' ? 'primary' : undefined}
            label={intl.formatMessage({ id: 'gamut.search.popover.allPhones' })}
            onClick={() => handleFilterByType('PHONE_LINKS')} />
          <Chip
            color={state.searchOptionType === 'TOPICS' ? 'primary' : undefined}
            label={intl.formatMessage({ id: 'gamut.search.popover.allServices' })}
            onClick={() => handleFilterByType('TOPICS')} />
          <Chip
            color={state.searchOptionType === 'FORM_LINKS' ? 'primary' : undefined}
            label={intl.formatMessage({ id: 'gamut.search.popover.allForms' })}
            onClick={() => handleFilterByType('FORM_LINKS')} />
          <Chip
            color={state.searchOptionType === 'LINKS' ? 'primary' : undefined}
            label={intl.formatMessage({ id: 'gamut.search.popover.allLinks' })}
            onClick={() => handleFilterByType('LINKS')} />
        </GRouterSecuredServicesFilterButtonsRoot>
        {state.forms.map((form) => <GLinkFormUnsecured key={form.linkToForm.id} label={form.label}
          value={form.linkToForm.value}
          onClick={() => handleSecureLink(form.linkToForm.id, form.topic.id)} />
        )}
        {state.phones.map((phone) => <GLinkPhone key={phone.id} label={phone.name} value={phone.value} />)}
        {state.topics.map((topic) => <Link key={topic.id} onClick={() => handleOnTopic(topic)}>{topic.name}</Link>)}
        {...state.internal.map((link) => <GLinkHyper label={link.name} value={link.value} key={link.id} />)}
        {...state.external.map((link) => <GLinkHyper label={link.name} value={link.value} key={link.id} />)}

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

