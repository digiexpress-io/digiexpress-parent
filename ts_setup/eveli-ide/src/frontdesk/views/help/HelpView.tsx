import { Box, Container, 
  Paper, Typography } from '@mui/material';
import React from 'react';
import { FormattedMessage } from 'react-intl';

export const HelpView: React.FC = () => {


  return (
    <Container maxWidth='lg'>
      <Typography variant='h6' gutterBottom>
        <FormattedMessage id='help.title' />
      </Typography>
      <Paper variant="outlined">
        <Box padding={1}>
        </Box>
      </Paper>
    </Container>
  );
}
