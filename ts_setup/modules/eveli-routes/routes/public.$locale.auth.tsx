import { createFileRoute } from '@tanstack/react-router'
import { Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@mui/material'
import { FormattedMessage } from 'react-intl'

import { EveliLogin } from '@dxs-ts/eveli-primitives'

export const Route = createFileRoute('/public/$locale/auth')({
  component: RouteComponent,
})

function RouteComponent() {
  return (
    <Dialog open={true}>
      <DialogTitle>
        <FormattedMessage id='login.dialog.title' />
      </DialogTitle>
      <DialogContent>
        <DialogContentText>
          <FormattedMessage id='login.dialog.message' />
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <EveliLogin />
      </DialogActions>
    </Dialog>)
}
