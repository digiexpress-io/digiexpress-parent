import React from 'react';
import { Dialog, useTheme, useMediaQuery, DialogContent, DialogActions, Button, LinearProgress, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { MaterialDialobReview } from '@resys/dialob-review-material';

export type DialobDialogProps = {
  closeDialog: ()=>void
  form: any
  session: any
  error?: Error
}

export const DialobReviewDialog: React.FC<DialobDialogProps> = ({closeDialog, form, session, error}) => {
  
  const theme = useTheme();
  const fullScreen = useMediaQuery(theme.breakpoints.down('sm'));

  const loaded = form && session;
  return (
    <Dialog open={true} fullScreen={fullScreen} scroll='paper' maxWidth='lg'>
      <DialogContent dividers>
        {loaded && 
          <MaterialDialobReview formData={form} sessionData={session} />
        }
        {!loaded && !error && <LinearProgress/>}
        {error && 
          <Typography variant='subtitle1' color='error'>
            <FormattedMessage id='application.error' />
          </Typography>
        }
      </DialogContent>
      <DialogActions>
        <Button onClick={closeDialog} variant='contained'><FormattedMessage id={'button.close'} /></Button>
      </DialogActions>
    </Dialog>
  )
}