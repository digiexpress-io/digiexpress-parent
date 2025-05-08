import React from 'react';
import { Link, Typography } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';
import { useNavigate } from '@tanstack/react-router';

import { useIntl } from 'react-intl';

import {
  GUserOverviewMenuView,
} from '../';
import { GRouterSecuredServicesBreadcrumbs, OwnerState } from './useUtilityClasses';




export interface GRouterSecuredServicesProps {
  locale: string;
  viewId: GUserOverviewMenuView;
}

export const Nav: React.FC<{ ownerState: OwnerState }> = ({ ownerState }) => {
  const intl = useIntl();
  const { topic } = ownerState;

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


  return (
    <GRouterSecuredServicesBreadcrumbs>
      <Link onClick={() => handleNav('user-overview')}>
        <HomeIcon />
        {intl.formatMessage({ id: 'gamut.userOverview.home' })}
      </Link>
      <Typography>
        {intl.formatMessage({ id: 'gamut.services' })}
      </Typography>
      <Typography>
        {topic?.name}
      </Typography>
    </GRouterSecuredServicesBreadcrumbs>);
}

