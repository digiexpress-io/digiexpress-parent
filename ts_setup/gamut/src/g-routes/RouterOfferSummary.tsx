import React from 'react';

import { Box, Button, Container, List, ListItem, ListItemIcon, ListItemText, Toolbar, Typography } from '@mui/material';
import UpdateIcon from '@mui/icons-material/Update';
import MailOutlineIcon from '@mui/icons-material/MailOutline';
import FilePresentIcon from '@mui/icons-material/FilePresent';
import PhoneEnabledIcon from '@mui/icons-material/PhoneEnabled';
import { useIntl } from 'react-intl';

import {
  GShell,
  GFooter,
  GShellClassName,
  GLayout,
  useSite,
  GLogo,
  useIam
} from '../';
import { useNavigate } from '@tanstack/react-router';



export const RouterOfferSummary: React.FC<{
  offerId: string;
  productId: string;
  pageId: string;
  locale: string;
}> = (props) => {

  const nav = useNavigate();
  const intl = useIntl();
  const anon = useIam();
  const site = useSite();
  const topic = site.views[props.pageId];
  const topicLink = topic.links.find(l => l.id === props.productId)

  console.log(topic.links)

  const anonymousUser = anon.authType === 'ANON';
  const buttonBackToMsg = anonymousUser ? 'gamut.public.forms.summary.button.backToServicesHome' : 'gamut.forms.summary.button.back-to-overview';

  function navBack() {
    if (anonymousUser) {
      nav({
        from: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary',
        params: { locale: props.locale },
        to: '/public/$locale'
      })
    }
    else {
      nav({
        from: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary',
        params: { viewId: 'user-overview' },
        to: '/secured/$locale/views/$viewId',
      })
    }
  }


  return (
    <GShell drawerOpen={false}>
      <Toolbar className={GShellClassName} >
        <GLayout variant='toolbar-n-rows-2-columns'>
          <GLogo variant='black_lg' />
        </GLayout>
      </Toolbar>

      <main role='main'>
        <Container>
          <GLayout variant='fill-session-start-end'
            slots={{
              topTitle: () => (
                <Box display='flex' flexDirection='column'>
                  <Typography variant='h1' textAlign='center' marginBottom='20px'>{intl.formatMessage({ id: 'gamut.forms.filling.summary.thank-you' })}</Typography>

                  <Typography variant='h2'>{intl.formatMessage({ id: 'gamut.forms.filling.summary' })}{intl.formatMessage({ id: 'gamut.textSeparator' })}{topicLink?.name ?? "-"}</Typography>
                  <Typography variant='body1'>{intl.formatMessage({ id: 'gamut.forms.filling.summary.info1' })}</Typography>

                  <div style={{ marginTop: '25px' }} />

                  <Typography variant='h2'>{intl.formatMessage({ id: 'gamut.forms.filling.summary.info2' })}</Typography>
                  <List disablePadding dense>
                    <ListItem dense>
                      <ListItemIcon><UpdateIcon color='primary' /></ListItemIcon>
                      <ListItemText>
                        <Typography variant='body1'>{intl.formatMessage({ id: 'gamut.forms.filling.summary.info3' })}</Typography>
                      </ListItemText>
                    </ListItem>

                    <ListItem>
                      <ListItemIcon><MailOutlineIcon color='primary' /></ListItemIcon>
                      <ListItemText>
                        <Typography variant='body1'>{intl.formatMessage({ id: 'gamut.forms.filling.summary.info4' })}</Typography>
                      </ListItemText>
                    </ListItem>

                    <ListItem>
                      <ListItemIcon><FilePresentIcon color='primary' /></ListItemIcon>
                      <ListItemText>
                        <Typography variant='body1'>{intl.formatMessage({ id: 'gamut.forms.filling.summary.info5' })}</Typography>
                      </ListItemText>
                    </ListItem>

                    <ListItem>
                      <ListItemIcon><PhoneEnabledIcon color='primary' /></ListItemIcon>
                      <ListItemText>
                        <Typography variant='body1'>{intl.formatMessage({ id: 'gamut.forms.filling.summary.info6' })}</Typography>
                      </ListItemText>
                    </ListItem>
                  </List>

                </Box>
              ),
              center: () => (<Button variant='contained' onClick={navBack}>{intl.formatMessage({ id: buttonBackToMsg })}</Button>)
            }}

          />


        </Container>
      </main>

      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell >

  );
}