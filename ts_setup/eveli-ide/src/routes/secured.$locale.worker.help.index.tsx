import React from 'react'
import { Box, Container, Paper, Typography } from '@mui/material'
import { FormattedMessage } from 'react-intl'


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  return (
    <Container maxWidth='lg'>
      <Typography variant='h6' gutterBottom>
        <FormattedMessage id='help.title' />
      </Typography>
      <Paper variant="outlined">
        <Box padding={1}></Box>
      </Paper>
    </Container>
  );
}
