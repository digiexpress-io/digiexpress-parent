import React from 'react';
import { Container, Drawer, useTheme, useMediaQuery } from '@mui/material';
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
  GUserOverviewMenuView,
  GArticle
} from '../';
import { GRouterSecuredServicesRoot, useUtilityClasses, OwnerState } from './useUtilityClasses';
import { SearchApi } from '../api-search';
import { SearchFilters } from './SearchFilter';
import { SearchResults } from './SearchResults';
import { Nav } from './Nav';


export interface GRouterSecuredServicesProps {
  locale: string;
  viewId: GUserOverviewMenuView;
}



export const GRouterSecuredServices: React.FC<GRouterSecuredServicesProps> = ({ locale, viewId }) => {
  const { views } = useSite();
  const intl = useIntl();
  const nav = useNavigate();
  const classes = useUtilityClasses();
  const theme = useTheme();

  const withDrawer = !useMediaQuery(theme.breakpoints.down("md"));
  const [topic, setTopic] = React.useState<SiteApi.TopicView>();

  function onForm(form: SearchApi.LinkToForm) {
    const productId = form.linkToForm.id;
    const pageId = form.topic.id;
    nav({
      params: { productId, pageId, locale: intl.locale },
      to: '/secured/$locale/pages/$pageId/products/$productId',
    })
  }
  const handleNav: (newViewId: GUserOverviewMenuView | undefined) => void = React.useCallback((newViewId) => {
    if (!newViewId) { // i.e. --> login/logout buttons
      return;
    }
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { viewId },
      to: '/secured/$locale/views/$viewId',
    })
  }, []);

  function handleLocale(locale: string) {
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { locale },
      to: '/secured/$locale/views/$viewId',
    })
  }


  const ownerState: OwnerState = {
    viewId,
    topic,
    withDrawer: withDrawer,
    onForm,
    onTopic: setTopic,
    onHome: () => handleNav('user-overview')
  }

  React.useEffect(() => {
    if (!topic && withDrawer) {
      const defaultTopic = Object.values(views).find((view: SiteApi.TopicView) => view.id === "000_index");
      setTopic(defaultTopic);
    }
  }, [topic, views]);

  const left = React.useCallback(() => {
    return (
      <>
        {!topic && !ownerState.withDrawer && (
          <>
            <SearchFilters ownerState={ownerState} />
            <SearchResults ownerState={ownerState} />
          </>
        )}
        {!!topic && <GArticle>{topic}</GArticle>}
      </>
    );
  }, [ownerState]);

  const breadcrumbs = React.useCallback(() => <Nav ownerState={ownerState} />, [ownerState]);

  return (
    <SearchApi.SearchProvider>
      <GShell>
        <GRouterSecuredServicesRoot className={classes.root}>
          <GAppBar locale={locale} onLocale={handleLocale} onLogoClick={() => handleNav('user-overview')} viewId={viewId} />

          {ownerState.withDrawer && (
            <Drawer variant='permanent' open={false} className={GShellClassName}>
              <SearchFilters ownerState={ownerState} />
              <SearchResults ownerState={ownerState} />
            </Drawer>)
          }

          <main role='main'><Container><GLayout variant='secured-1-row-1-column' slots={{ breadcrumbs, left }} /></Container></main>
          <footer role='footer'><GFooter /></footer>
        </GRouterSecuredServicesRoot>
      </GShell>
    </SearchApi.SearchProvider>
  );
}

