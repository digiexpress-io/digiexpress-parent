import { EveliLogin } from '@/eveli-login'
import { Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@mui/material'

import { FormattedMessage } from 'react-intl'

export const Route = createFileRoute({
  component: RouteComponent,
})

function RouteComponent() {
  return (
    <Dialog open={true}>
      <DialogTitle>
        <FormattedMessage id='login.dialog.title' defaultMessage='Use DigiExpress services'/>
      </DialogTitle>
      <DialogContent>
        <DialogContentText>
          <FormattedMessage id='login.dialog.message' defaultMessage='Please login to proceed'/>
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <EveliLogin />
      </DialogActions>
    </Dialog>)
}
