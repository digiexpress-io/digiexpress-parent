import React from 'react';
import { Box, CircularProgress, Typography } from '@mui/material';
import {useIntl} from 'react-intl';


export const EveliSpinner: React.FC = () => {

  const intl = useIntl();

    return (
      <Box sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        height: '70vh',
      }}
      >
        <Typography sx={{ mr: 1 }}>{intl.formatMessage({ id: 'eveli.loading', defaultMessage: 'Loading...' })}</Typography>
        <CircularProgress />
      </Box >
    )
  }
