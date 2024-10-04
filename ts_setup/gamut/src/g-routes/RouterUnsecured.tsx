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


const Internal: React.FC<{}> = () => {
  const nav = useNavigate();
  const theme = useTheme();
  const { locale } = useLocale();
  const { views } = useSite();
  const [topic, setTopic] = React.useState<SiteApi.TopicView>();

  const landingTopic = Object.values(views).find(a => a.id === '000_index');

  function handleTopicChange(topic: SiteApi.TopicView) {
    setTopic(topic);
  }

  function handleLocale(locale: string) {
    nav({
      from: '/public/$locale',
      params: { locale },

      to: '/public/$locale',
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

          <Box
            sx={{
              [theme.breakpoints.down('md')]: {
                justifyContent: 'center',
              },
              position: 'relative',
              backgroundImage: `url(${city1})`,
              backgroundSize: 'cover',
              alignContent: 'space-evenly',
              display: 'flex',
              flexWrap: 'wrap',
              padding: 1,
              height: 400,
              zIndex: 1,
            }}
          >

            <GPopoverTopics onTopic={handleTopicChange} />
            <GPopoverSearch onTopic={handleTopicChange} />
          </Box>

          {/*<GForm variant="general-message">1dfe0a3eef10f0306171f85b37a15209</GForm> */}
          {topic === undefined ? <GArticle>{landingTopic}</GArticle> : <GArticle>{topic}</GArticle>}
        </Container>
      </main>

      <footer role='footer'>
        <GFooter />
      </footer>

    </>
  );
}



export const RouterUnsecured: React.FC<{}> = () => {
  return (
    <GShell drawerOpen={false}>
      <Internal />
    </GShell>
  );
}