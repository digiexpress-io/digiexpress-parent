import { Box, Container, 
  Paper, Typography } from '@mui/material';
import React, { useEffect } from 'react';
import { FormattedMessage } from 'react-intl';
import { useUserInfo } from '../../context/UserContext';

export const HelpView: React.FC = () => {
  const userInfo = useUserInfo();

  useEffect(()=> {
  }, [userInfo]);

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
