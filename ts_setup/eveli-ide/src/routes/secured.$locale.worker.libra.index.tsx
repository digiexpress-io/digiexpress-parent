import { XfsBreadcrumbs, XfsFolder, XfsProvider, XfsTree } from '@/eveli-xfile-system'
import { Box, Container, Paper } from '@mui/material'


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  return (
    <Container maxWidth='lg'>
      <Paper variant="outlined">
        <Box padding={1}>
          <XfsProvider>
            <XfsBreadcrumbs />
            <Box display='flex'>
              <Box width="50%">
                <XfsTree />
              </Box>
              <Box>
                <XfsFolder />
              </Box>
            </Box>
          </XfsProvider>
        </Box>
      </Paper>
    </Container>
  )
}
