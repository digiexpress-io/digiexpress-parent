import React from 'react';
import { Breadcrumbs, Link, Typography, useMediaQuery, useTheme } from '@mui/material';
import { Home as HomeIcon } from '@mui/icons-material';
import { useNavigate } from '@tanstack/react-router';

import { useIntl } from 'react-intl';

import {
  GUserOverviewMenuView,
} from '@dxs-ts/gamut-primitives';
import { OwnerState } from './useUtilityClasses';
import { useUtilityClasses } from './useUtilityClasses';




export interface GRouterSecuredServicesProps {
  locale: string;
  viewId: GUserOverviewMenuView;
}

export const Nav: React.FC<{ ownerState: OwnerState }> = ({ ownerState }) => {
  const intl = useIntl();
  const theme = useTheme();
  const { topic } = ownerState;
  const classes = useUtilityClasses();
  const nav = useNavigate();

  function handleNav(viewId: GUserOverviewMenuView | undefined) {
    if (!viewId) { // i.e. --> login/logout buttons
      return;
    }
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { viewId },
      to: '/secured/$locale/views/$viewId',
    })
  }

  const isMobile = useMediaQuery(theme.breakpoints.down("md"));
  return (
    <Breadcrumbs className={classes.servicesBreadcrumbs}>
      <Link onClick={() => handleNav('user-overview')}>
        <HomeIcon />
        {intl.formatMessage({ id: 'gamut.userOverview.home' })}
      </Link>
      <Link onClick={() => handleNav('services')}>
        {intl.formatMessage({ id: 'gamut.services' })}
      </Link>
      {(ownerState.activeTopicId && topic?.name || !isMobile) && (<Typography>{topic?.name}</Typography>)}
    </Breadcrumbs>);
}

