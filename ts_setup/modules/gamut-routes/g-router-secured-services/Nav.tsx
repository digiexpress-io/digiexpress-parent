import React from 'react';
import { Breadcrumbs, Link, Typography } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';
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


  return (
    <Breadcrumbs className={classes.servicesBreadcrumbs}>
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
    </Breadcrumbs>);
}

