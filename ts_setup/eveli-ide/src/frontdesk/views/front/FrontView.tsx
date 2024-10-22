import { Container, Divider, Grid, Typography } from '@mui/material';
import React from 'react';
import { FormattedMessage } from 'react-intl';


export const FrontView: React.FC = () => {


  return (
    <Container maxWidth='lg'>
      <Grid container spacing={3}>
        <Grid item xs={12} sm={12}>
          <Typography variant='h2' component='h1'>
            <FormattedMessage id='front.intro.title' />
          </Typography>
          <Divider sx={{ mb: 3 }} />
        </Grid>
      </Grid>
    </Container>
  );
}
