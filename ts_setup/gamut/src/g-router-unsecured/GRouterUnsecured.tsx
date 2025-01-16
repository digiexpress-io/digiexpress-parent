import React from 'react';
import { Container, Stack, Toolbar } from '@mui/material';
import { useNavigate } from '@tanstack/react-router';

import {
  GShell, GLogo,
  GLocales,
  GLogin,
  GPopoverSearch,
  GLayout,
  GFooter,
  GArticle,
  GShellClassName,
  GPopoverTopics,
  SiteApi,
  useLocale,
  useSite,
} from '../';

import { GRouterUnsecuredRoot } from './useUtilityClasses';
import { useUtilityClasses } from './useUtilityClasses';


const FlexSpacerRow: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (<Stack spacing={1} direction='row'>{children}</Stack>)
}


const Internal: React.FC<{ pageId: string }> = ({ pageId }) => {
  const nav = useNavigate();
  const { locale } = useLocale();
  const { views } = useSite();
  const classes = useUtilityClasses();

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
      params: { locale },

      to: '/public/$locale',
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
      <Toolbar className={GShellClassName} >
        <GLayout variant={'toolbar-n-rows-2-columns'}>
          <GLogo variant='black_lg' />
          <FlexSpacerRow>
            <GLocales value={locale} onClick={handleLocale} />
            <GLogin />
          </FlexSpacerRow>

        </GLayout>
      </Toolbar >

      <main role='main'>
        <Container>
          <GRouterUnsecuredRoot className={classes.root}>
            <div className={classes.menuButtonContainer}>
              <GPopoverTopics onTopic={handleTopicChange} />
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



export const GRouterUnsecured: React.FC<{ pageId: string }> = ({ pageId }) => {
  return (
    <GShell drawerOpen={false}>
      <Internal pageId={pageId} />
    </GShell>
  );
}