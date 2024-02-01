import React from 'react';
import { Dialog, DialogContent, DialogTitle, Box, DialogActions, IconButton, Typography, useTheme, alpha, CircularProgress } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { FormattedMessage } from 'react-intl';

import { TenantEntryDescriptor } from 'descriptor-dialob';
import { DialobForm, DialobSession } from 'client'
import { SessionList } from './SessionList';
import { sambucus, wash_me } from 'components-colors';

const DialobSessionsDialog: React.FC<{
  onClose: () => void,
  entry: TenantEntryDescriptor,
  form: DialobForm | undefined,
  sessions: DialobSession[] | undefined
}> = (props) => {

  const { form, entry, sessions } = props;
  const theme = useTheme();

  if (!form || !sessions) {
    return (<Dialog open={true} fullWidth maxWidth='md'>
      <DialogContent>
        <CircularProgress />
      </DialogContent>
    </Dialog>);
  }

  return (
    <Dialog open={true} fullWidth maxWidth='xl'>
      <DialogTitle sx={{
        backgroundColor: wash_me,
        borderBottom: `1px solid ${alpha(sambucus, 0.3)}`,
        mb: 1,
      }}>
        <Box display='flex' alignItems='center'>
          <Typography variant='h4' fontWeight='bolder'><FormattedMessage id='dialob.form.sessions.dialog.title' /></Typography>
          <Box flexGrow={1} />
          <IconButton onClick={props.onClose}>
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>

      <DialogContent>
        <SessionList entry={entry} form={form} sessions={sessions} />
      </DialogContent>

      <DialogActions>
      </DialogActions>
    </Dialog>
  );
}

export default DialobSessionsDialog;