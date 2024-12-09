import React from 'react';
import { Container, Stack, Toolbar, Box, useTheme } from '@mui/material';
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

import city1 from './city1.jpg'


const FlexSpacerRow: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (<Stack spacing={1} direction='row'>{children}</Stack>)
}


const Internal: React.FC<{ pageId: string }> = ({ pageId }) => {
  const nav = useNavigate();
  const theme = useTheme();
  const { locale } = useLocale();
  const { views } = useSite();
  
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
          <Box sx={{
              [theme.breakpoints.down('md')]: {
                justifyContent: 'center',
                gap: theme.spacing(1),
              },
              position: 'relative',
              backgroundImage: `url(${city1})`,
              backgroundSize: 'cover',
              alignContent: 'center',
              display: 'flex',
              flexWrap: 'wrap',
              padding: 1,
              height: 400,
              zIndex: 1,
            }}
          >

            <GPopoverTopics onTopic={handleTopicChange} />
            <GPopoverSearch onTopic={handleTopicChange} pageId={pageId} onFormLink={({ pageId, productId }) => handleUnSecureLink(pageId, productId)} />
          </Box>

          <GArticle>{landingTopic}</GArticle>
        </Container>
      </main>

      <footer role='footer'>
        <GFooter />
      </footer>

    </>
  );
}



export const RouterUnsecured: React.FC<{pageId: string}> = ({ pageId }) => {
  return (
    <GShell drawerOpen={false}>
      <Internal pageId={pageId}/>
    </GShell>
  );
}