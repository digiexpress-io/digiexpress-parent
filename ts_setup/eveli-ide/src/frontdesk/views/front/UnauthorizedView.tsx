import { Container, Divider, Grid2, Typography } from '@mui/material';
import { Alert } from '@mui/lab';
import React from 'react';
import { FormattedMessage } from 'react-intl';

export const UnauthorizedView: React.FC = () => {

  return (
    <Container maxWidth='lg'>
      <Grid2 container spacing={3}>
        <Grid2 size={{ xs: 12, sm: 12 }}>
          <Typography variant='h2' component='h1'>
            <FormattedMessage id='error.unauthorized.title' />
          </Typography>
          <Divider sx={{ mb: 3 }} />
          <Alert severity="error">
            <FormattedMessage id='error.unauthorized.text' />
          </Alert>
        </Grid2>
      </Grid2>
    </Container>
  );
}
