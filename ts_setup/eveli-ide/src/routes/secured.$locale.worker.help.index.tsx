import React from 'react'
import { Box, Container, Paper, Typography } from '@mui/material'
import { FormattedMessage } from 'react-intl'
import { useLocale } from '@/api-locale'


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

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
