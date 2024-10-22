import { Container, Divider, Grid, Typography } from '@mui/material';
import { Alert } from '@mui/lab';
import React from 'react';
import { FormattedMessage } from 'react-intl';

export const UnauthorizedView: React.FC = () => {

  return (
    <Container maxWidth='lg'>
      <Grid container spacing={3}>
        <Grid item xs={12} sm={12}>
          <Typography variant='h2' component='h1'>
            <FormattedMessage id='error.unauthorized.title' />
          </Typography>
          <Divider sx={{ mb: 3 }} />
          <Alert severity="error">
            <FormattedMessage id='error.unauthorized.text' />
          </Alert>
        </Grid>
      </Grid>
    </Container>
  );
}
