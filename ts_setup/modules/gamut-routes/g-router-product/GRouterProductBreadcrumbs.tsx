import React from 'react';
import { Link, Typography, Breadcrumbs } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';


import { useNavigate } from '@tanstack/react-router';
import { useIntl } from 'react-intl';

import { useLocale } from '@dxs-ts/gamut-api';

import { useUtilityClasses } from './useUtilityClasses';
import { GRouterProductOwnerState } from './g-router-product-types'



export const GRouterProductBreadcrumbs: React.FC<GRouterProductOwnerState> = (props) => {
  const { ownerState } = props;

  return (
    ownerState.isAnon ? 
      <AnonBreadcrumbs ownerState={ownerState} /> : 
      <ProductBreadcrumbs ownerState={ownerState} />
  );
}



const ProductBreadcrumbs: React.FC<GRouterProductOwnerState> = (props) => {
  const { topic, topicLink } = props.ownerState;
  const intl = useIntl();
  const nav = useNavigate();
  const classes = useUtilityClasses();

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
    <Breadcrumbs className={classes.productBreadcrumbs}>
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
    </Breadcrumbs>)
}

const AnonBreadcrumbs: React.FC<GRouterProductOwnerState> = (props) => {
  const { topic, topicLink } = props.ownerState;
  const intl = useIntl();
  const nav = useNavigate();
  const { locale } = useLocale();
  const classes = useUtilityClasses();

  function handleHomePage(locale: string) {
    nav({
      from: '/public/$locale',
      params: { locale },
      to: '/public/$locale',
    })
  }
  return (
    <Breadcrumbs className={classes.anonBreadcrumbs}>
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
    </Breadcrumbs>)
}