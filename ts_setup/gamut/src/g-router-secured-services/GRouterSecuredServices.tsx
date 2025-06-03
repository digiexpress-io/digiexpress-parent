import React, { ReactElement } from 'react';
import { Container, Drawer, useTheme, useMediaQuery, useThemeProps } from '@mui/material';
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
import { GRouterSecuredServicesRoot, useUtilityClasses, OwnerState, MUI_NAME } from './useUtilityClasses';
import { SearchApi } from '../api-search';
import { SearchFilters } from './SearchFilter';
import { SearchResults } from './SearchResults';
import { Nav } from './Nav';


export interface GRouterSecuredServicesProps {
  locale: string;
  viewId: GUserOverviewMenuView;
  defaultViewId?: string;

  activeTopicId?: string | undefined;
}

export const GRouterSecuredServices: React.FC<GRouterSecuredServicesProps> = (initProps) => {
  const { views } = useSite();
  const intl = useIntl();
  const nav = useNavigate();
  const classes = useUtilityClasses();
  const theme = useTheme();
  const withDrawer = !useMediaQuery(theme.breakpoints.down("md"));


  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const defaultViewId = props.activeTopicId ?? props.defaultViewId ?? '000_index';
  const topic = Object.values(views).find((view: SiteApi.TopicView) => view.id === defaultViewId);


  function setTopic(topic: SiteApi.TopicView) {
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { viewId: 'services' },
      search: { topicId: topic.id },
      to: '/secured/$locale/views/$viewId',
    })
  }

  const ownerState: OwnerState = {
    defaultViewId,
    locale: props.locale,
    viewId: props.viewId,
    topic,
    withDrawer: withDrawer,
    onForm,
    onTopic: setTopic,
  }

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
      params: { viewId: ownerState.viewId },
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


  const breadcrumbs = React.useCallback(() => <Nav ownerState={ownerState} />, [ownerState]);

  const leftContents = (<>
    {!topic && !ownerState.withDrawer && (
      <>
        <SearchFilters ownerState={ownerState} />
        <SearchResults ownerState={ownerState} />
      </>
    )}
    {!!topic && <GArticle>{topic}</GArticle>}
  </>);


  return (
    <SearchApi.SearchProvider>
      <GShell>
        <GRouterSecuredServicesRoot className={classes.root}>
          <GAppBar locale={ownerState.locale} onLocale={handleLocale} onLogoClick={() => handleNav('user-overview')} viewId={ownerState.viewId} />

          {ownerState.withDrawer && (
            <Drawer variant='permanent' open={false} className={GShellClassName}>
              <SearchFilters ownerState={ownerState} />
              <SearchResults ownerState={ownerState} />
            </Drawer>)
          }

          <main role='main'><Container><GLayout variant='secured-1-row-1-column' left={leftContents} slots={{ breadcrumbs, left: Left }} /></Container></main>
          <footer role='footer'><GFooter /></footer>
        </GRouterSecuredServicesRoot>
      </GShell>
    </SearchApi.SearchProvider>
  );
}


const Left: React.FC<{ children?: React.ReactNode }> = ({ children }) => {

  return (<>{children}</>)
}

