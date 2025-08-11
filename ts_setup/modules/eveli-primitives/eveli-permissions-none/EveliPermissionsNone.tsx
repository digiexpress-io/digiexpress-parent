import React from 'react';
import { Box, Typography } from '@mui/material';
import { EveliPermissionsNoneRoot, useUtilityClasses } from './useUtilityClasses';
import { useIntl } from 'react-intl';

import { EveliLogo } from '../eveli-logo';



export const EveliPermissionsNone: React.FC = () => {
  const classes = useUtilityClasses();
  const intl = useIntl();

  return (
    <EveliPermissionsNoneRoot className={classes.root}>
      <Box className={classes.logoBox}>
        <EveliLogo variant='black_lg'/>
      </Box>
      <Box>
        <Typography variant='h1'>{intl.formatMessage({id: 'eveli.permissions.none.title'})}</Typography>
        <Typography variant='body1'>{intl.formatMessage({id: 'eveli.permissions.none.desc1'})}</Typography>
        <Typography variant='body1'>{intl.formatMessage({id: 'eveli.permissions.none.desc2'})}</Typography>
      </Box>
    </EveliPermissionsNoneRoot>)
}