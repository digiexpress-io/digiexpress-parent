import React from 'react';
import { Container, Breadcrumbs, Link, Typography, Button, Box, List, ListItem, ListItemIcon, ListItemText, Toolbar } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';
import SaveIcon from '@mui/icons-material/Save';
import {
  GShell,
  GFooter,
  GAppBar,
  GLayout,
  useSite,
  useOffers,
  useIam,
  useLocale,
  GLogo,
  GShellClassName,
} from '../';
import { useNavigate } from '@tanstack/react-router';
import { useIntl } from 'react-intl';
import { SiteApi } from '../api-site';


export interface RouterProductProps {
  productId: string, pageId: string, locale: string
}

export const RouterProduct: React.FC<RouterProductProps> = (props) => {
  const nav = useNavigate();
  const anon = useIam();
  const site = useSite();


  const topic = site.views[props.pageId];
  const topicLink = topic.links.find(l => l.id === props.productId)!
  const anonymousUser = anon.authType === 'ANON';

  const ownerState = {
    topic,
    topicLink,
    anonymousUser,
    locale: props.locale
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
              breadcrumbs: () => (anonymousUser ? <AnonBreadcrumbs ownerState={ownerState} /> : <ProductBreadcrumbs ownerState={ownerState} />),
              topTitle: () => <TopTitle ownerState={ownerState} />,
              center: () => <Center ownerState={ownerState} />
            }}>

          </GLayout>
        </Container>
      </main>

      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell>
  );
}


interface RouterProductOwnerState {
  ownerState: {
    topic: SiteApi.TopicView;
    topicLink: SiteApi.TopicLink;
    anonymousUser: boolean;
    locale: string;
  }
}

const Center: React.FC<RouterProductOwnerState> = (props) => {
  const nav = useNavigate();
  const offers = useOffers();
  const intl = useIntl();
  const anon = useIam();

  const { topicLink, topic, locale } = props.ownerState;
  const productId = topicLink.id;
  const anonymousUser = anon.authType === 'ANON';

  // article links
  const parentPageId = topic.parent?.id ?? undefined;
  const pageId = topic.id;


  function handleCancelOffer() {
    if (anonymousUser) {
      nav({
        from: '/public/$locale/pages/$pageId/products/$productId',
        params: { locale },
        to: '/public/$locale'
      })
    }
    nav({
      from: '/public/$locale/pages/$pageId/products/$productId',
      params: { viewId: 'user-overview' },
      to: '/secured/$locale/views/$viewId',
    })
  }

  function handleCreateOffer() {
    offers.createOffer({ locale, productId, parentPageId, pageId }).then((offer) => {
      nav({
        params: { locale, pageId, productId, offerId: offer.id },
        to: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId',
      })
    })
  }

  return (<>
    <Button variant='outlined' onClick={handleCancelOffer}>{intl.formatMessage({ id: 'gamut.forms.filling.cancel.button' })}</Button>
    <Button variant='contained' onClick={handleCreateOffer}>{intl.formatMessage({ id: 'gamut.forms.filling.start.button' })}</Button>
  </>)
}

const TopTitle: React.FC<RouterProductOwnerState> = (props) => {
  const { topicLink } = props.ownerState;
  const intl = useIntl();
  return (
    <Box display='flex' flexDirection='column'>
      <Typography variant='h1' textAlign='center' marginBottom='20px'>{intl.formatMessage({ id: 'gamut.forms.filling.welcome' })}</Typography>
      <Typography variant='h3'>{intl.formatMessage({ id: 'gamut.forms.filling.start' })}{intl.formatMessage({ id: 'gamut.textSeparator' })}{topicLink.name}</Typography>

      <List disablePadding dense>
        <ListItem dense>
          <ListItemIcon><SaveIcon color='primary' /></ListItemIcon>
          <ListItemText>
            <Typography variant='body1'>{intl.formatMessage({ id: 'gamut.forms.filling.start.info1' })}</Typography>
          </ListItemText>
        </ListItem>
      </List>
    </Box>)
}


const ProductBreadcrumbs: React.FC<RouterProductOwnerState> = (props) => {
  const { topic, topicLink } = props.ownerState;
  const intl = useIntl();
  const nav = useNavigate();

  function handleUserOverview() {
    nav({
      from: '/public/$locale/pages/$pageId/products/$productId',
      params: { viewId: 'user-overview' },
      to: '/secured/$locale/views/$viewId',
    })
  }
  function handleServicesClick() {
    nav({
      from: '/public/$locale/pages/$pageId/products/$productId',
      params: { viewId: 'services' },
      to: '/secured/$locale/views/$viewId',
    });
  }

  return (
    <Breadcrumbs>
      <Link onClick={handleUserOverview}>
        <HomeIcon />
        {intl.formatMessage({ id: 'gamut.userOverview.home' })}
      </Link>
      <Link onClick={handleServicesClick}>
        {intl.formatMessage({ id: 'gamut.services' })}
      </Link>
      <Typography>
        {topic.name}
      </Typography>
      <Typography>
        {topicLink.name}
      </Typography>
    </Breadcrumbs>)
}

const AnonBreadcrumbs: React.FC<RouterProductOwnerState> = (props) => {
  const { topic, topicLink } = props.ownerState;
  const intl = useIntl();
  const nav = useNavigate();
  const { locale } = useLocale();

  function handleHomePage(locale: string) {
    nav({
      from: '/public/$locale',
      params: { locale },

      to: '/public/$locale',
    })
  }
  return (
    <Breadcrumbs>
      <Link onClick={() => handleHomePage(locale)}>
        <HomeIcon />
        {intl.formatMessage({ id: 'gamut.public.servicesHome' })}
      </Link>
      <Typography>
        {topic.name}
      </Typography>
      <Typography>
        {topicLink.name}
      </Typography>
    </Breadcrumbs>)
}