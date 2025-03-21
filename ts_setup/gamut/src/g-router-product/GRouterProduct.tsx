import React from 'react';
import { Container, Link, Typography, Button, List, ListItem, ListItemIcon, ListItemText, Toolbar, Alert } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';
import SaveIcon from '@mui/icons-material/Save';
import {
  GShell,
  GFooter,
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

import { GRouterProductAnonBreadcrumbsRoot, GRouterProductBreadcrumbsRoot, GRouterProductRoot, GRouterProductTitleRoot, GRouterProductButtonsRoot, useUtilityClasses } from './useUtilityClasses';
import { GAuthFormStart } from '../g-auth-form-start';

export interface GRouterProductProps {
  productId: string,
  pageId: string,
  locale: string
}

export const GRouterProduct: React.FC<GRouterProductProps> = (props) => {
  const anon = useIam();
  const site = useSite();

  const topic = site.views[props.pageId];
  const topicLink = topic.links.find(l => l.id === props.productId)
  const anonymousUser = anon.authType === 'ANON';
  const anonLink: boolean = anonymousUser && topicLink?.anon === true;
  const allowed: boolean = (!anonymousUser || anonLink) && !!topicLink;
  const ownerState = {
    topic,
    topicLink,
    anonymousUser,
    locale: props.locale,
    allowed
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
          <GRouterProductRoot>
            <GLayout variant='fill-session-start-end'
              slots={{
                breadcrumbs: () => (anonymousUser ? <AnonBreadcrumbs ownerState={ownerState} /> : <ProductBreadcrumbs ownerState={ownerState} />),
                topTitle: () => <ProductTitle ownerState={ownerState} />,
                center: () => <StartProductForm ownerState={ownerState} />
              }}>
            </GLayout>
          </GRouterProductRoot>
        </Container>
      </main>

      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell >
  );
}


interface GRouterProductOwnerState {
  ownerState: {
    topic: SiteApi.TopicView;
    topicLink: SiteApi.TopicLink | undefined;
    anonymousUser: boolean;
    allowed: boolean;
    locale: string;
  }
}

const StartProductForm: React.FC<GRouterProductOwnerState> = (props) => {
  const nav = useNavigate();
  const offers = useOffers();
  const intl = useIntl();

  const { topicLink, topic, locale, anonymousUser, allowed } = props.ownerState;
  const productId = topicLink?.id;
  const classes = useUtilityClasses();


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
      from: '/secured/$locale/pages/$pageId/products/$productId',
      params: { viewId: 'user-overview' },
      to: '/secured/$locale/views/$viewId',
    })
  }

  function handleCreateOffer() {
    if (!productId) {
      return;
    }

    offers.createOffer({ locale, productId, parentPageId, pageId }).then((offer) => {
      if (anonymousUser) {
        nav({
          params: { locale, pageId },
          to: '/public/$locale/pages/$pageId',
        })
      } else {
        nav({
          params: { locale, pageId, productId, offerId: offer.id },
          to: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId',
        })
      }
    })
  }

  function handleAfterLogin() {
    nav({
      from: '/public/$locale/pages/$pageId/products/$productId',
      to: '/secured/$locale/pages/$pageId/products/$productId',
    })
  }

  return (
    <GRouterProductButtonsRoot className={classes.root}>
      <Button variant='outlined' onClick={handleCancelOffer}>{intl.formatMessage({ id: 'gamut.forms.filling.cancel.button' })}</Button>

      {allowed ? (
        <Button variant='contained' onClick={handleCreateOffer}>{intl.formatMessage({ id: 'gamut.forms.filling.start.button' })}</Button>
      ) : (
        <GAuthFormStart forced onSubmit={handleAfterLogin}>
          <Button type='submit' variant='contained'>{intl.formatMessage({ id: 'gamut.forms.filling.login-then-start.button' })}</Button>
        </GAuthFormStart>)

      }

    </GRouterProductButtonsRoot>)
}

const ProductTitle: React.FC<GRouterProductOwnerState> = (props) => {
  const { topicLink, allowed } = props.ownerState;
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <GRouterProductTitleRoot>
      <Typography className={classes.productTitle}>{intl.formatMessage({ id: 'gamut.forms.filling.welcome' })}</Typography>
      <Typography className={classes.productSubTitle}>{intl.formatMessage({ id: 'gamut.forms.filling.start' })}{intl.formatMessage({ id: 'gamut.textSeparator' })}{topicLink?.name ?? "-"}</Typography>

      <List disablePadding dense>
        <ListItem>
          <ListItemIcon><SaveIcon color='primary' /></ListItemIcon>
          <ListItemText>
            <Typography className={classes.productBodyText}>{intl.formatMessage({ id: 'gamut.forms.filling.start.info1' })}</Typography>
          </ListItemText>
        </ListItem>
        {!allowed && (
          <ListItem>
            <ListItemText>
              <Alert severity='error'>
                <Typography className={classes.productBodyTextError}>{intl.formatMessage({ id: 'gamut.forms.filling.must_be_authenticated' })}</Typography>
              </Alert>
            </ListItemText>
          </ListItem>)}
      </List>
    </GRouterProductTitleRoot>
  )
}


const ProductBreadcrumbs: React.FC<GRouterProductOwnerState> = (props) => {
  const { topic, topicLink, anonymousUser } = props.ownerState;
  const intl = useIntl();
  const nav = useNavigate();


  function handleUserOverview() {
    nav({
      from: '/secured/$locale/pages/$pageId/products/$productId',
      params: { viewId: 'user-overview' },
      to: '/secured/$locale/views/$viewId',
    })
  }
  function handleServicesClick() {
    nav({
      from: '/secured/$locale/pages/$pageId/products/$productId',
      params: { viewId: 'services' },
      to: '/secured/$locale/views/$viewId',
    });
  }

  return (
    <GRouterProductBreadcrumbsRoot>
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
        {topicLink?.name ?? "-"}
      </Typography>
    </GRouterProductBreadcrumbsRoot>)
}

const AnonBreadcrumbs: React.FC<GRouterProductOwnerState> = (props) => {
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
    <GRouterProductAnonBreadcrumbsRoot>
      <Link onClick={() => handleHomePage(locale)}>
        <HomeIcon />
        {intl.formatMessage({ id: 'gamut.public.servicesHome' })}
      </Link>
      <Typography>
        {topic.name}
      </Typography>
      <Typography>
        {topicLink?.name ?? "-"}
      </Typography>
    </GRouterProductAnonBreadcrumbsRoot>)
}