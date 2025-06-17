import React from 'react';
import { Container, useThemeProps } from '@mui/material';
import { useNavigate } from '@tanstack/react-router';

import {
  GShell,
  GPopoverSearch,
  GFooter,
  GArticle,
  GPopoverTopics,
  SiteApi,
  useLocale,
  useSite,
  GAppBar,
} from '../';

import { GRouterUnsecuredRoot, MUI_NAME } from './useUtilityClasses';
import { useUtilityClasses } from './useUtilityClasses';


export interface GRouterUnsecuredProps {
  pageId?: string;
  defaultPageId?: string;
  backgroundImage?: string;
  height?: string | number
}

const Internal: React.FC<GRouterUnsecuredProps> = (initProps) => {
  
  const nav = useNavigate();
  const { locale } = useLocale();
  const { views } = useSite();
  const classes = useUtilityClasses();
  const ownerState = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const pageId = ownerState.pageId ?? (ownerState.defaultPageId ?? '000_index');

  const landingTopic = Object.values(views).find(a => a.id === pageId);

  function handleTopicChange(topic: SiteApi.TopicView) {
    nav({
      from: '/public/$locale',
      params: { locale, pageId: topic.id },
      to: '/public/$locale/pages/$pageId',
    })
  }

  function handleLocale(locale: string) {
    nav({
      from: '/public/$locale',
      params: { locale, pageId },
      to: '/public/$locale/pages/$pageId',
    })
  }

  function handleUnSecureLink(pageId: string, productId: string) {
    nav({
      params: { productId, pageId, locale },
      to: '/public/$locale/pages/$pageId/products/$productId',
    })
  }

  return (
    <>
      <GAppBar locale={locale} onLocale={handleLocale} onLogoClick={() => { }} />
      <main role='main'>
        <Container>
          <GRouterUnsecuredRoot className={classes.root} ownerState={ownerState}>
            <div className={classes.menuButtonContainer}>
              <GPopoverTopics onTopic={handleTopicChange} hideChildren={true} />
              <GPopoverSearch onTopic={handleTopicChange} pageId={pageId} onFormLink={({ pageId, productId }) => handleUnSecureLink(pageId, productId)} />
            </div>
            <GArticle>{landingTopic}</GArticle>
          </GRouterUnsecuredRoot>
        </Container>
      </main>

      <footer role='footer'>
        <GFooter />
      </footer>
    </>
  );
}



export const GRouterUnsecured: React.FC<GRouterUnsecuredProps> = (props) => {
  return (
    <GShell drawerOpen={false}>
      <Internal {...props} />
    </GShell>
  );
}