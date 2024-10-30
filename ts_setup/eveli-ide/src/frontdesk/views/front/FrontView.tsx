import { Container, Divider, Grid2, Typography } from '@mui/material';
import React from 'react';
import { FormattedMessage } from 'react-intl';


export const FrontView: React.FC = () => {


  return (
    <Container maxWidth='lg'>
      <Grid2 container spacing={3}>
        <Grid2 size={{ xs: 12, sm: 12 }}>
          <Typography variant='h2' component='h1'>
            <FormattedMessage id='front.intro.title' />
          </Typography>
          <Divider sx={{ mb: 3 }} />
        </Grid2>
      </Grid2>
    </Container>
  );
}
