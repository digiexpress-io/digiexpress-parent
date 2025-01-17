import React from 'react';
import { Link, Typography } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';

import { useIntl } from 'react-intl';

import {
  GUserOverviewMenuView,
} from '../';
import { GRouterSecuredServicesBreadcrumbsRoot, useUtilityClasses, OwnerState } from './useUtilityClasses';




export interface GRouterSecuredServicesProps {
  locale: string;
  viewId: GUserOverviewMenuView;
}

export const Nav: React.FC<{ ownerState: OwnerState }> = ({ ownerState }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const { topic, onHome } = ownerState;

  return (
    <GRouterSecuredServicesBreadcrumbsRoot className={classes.root}>
      <Link onClick={onHome}>
        <HomeIcon />
        {intl.formatMessage({ id: 'gamut.userOverview.home' })}
      </Link>
      <Typography>
        {intl.formatMessage({ id: 'gamut.services' })}
      </Typography>
      <Typography>
        {topic?.name}
      </Typography>
    </GRouterSecuredServicesBreadcrumbsRoot>);
}

