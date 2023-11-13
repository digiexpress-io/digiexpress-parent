import React from 'react';
import { Dialog, DialogContent, DialogTitle, Stack, Box, DialogActions, IconButton, Typography, useTheme, alpha, Grid, CircularProgress } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { FormattedMessage } from 'react-intl';

import Burger from 'components-burger';
import { TenantEntryDescriptor } from 'descriptor-tenant';
import { DialobForm, DialobSession } from 'client'
import { SessionItem } from './SessionItem';

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
        backgroundColor: theme.palette.mainContent.main,
        borderBottom: `1px solid ${alpha(theme.palette.mainContent.dark, 0.3)}`,
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
        <Stack overflow='auto' spacing={1} direction='column'>
          <Grid container spacing={1} direction='row'>
            <Grid item md={3} lg={3} xl={3}>
              <Typography fontWeight='bold'><FormattedMessage id='dialob.form.title' /></Typography>
            </Grid>
            <Grid item md={9} lg={9} xl={9}>
              <Typography>{props.entry.formTitle}</Typography>
            </Grid>

            <Grid item md={3} lg={3} xl={3}>
              <Typography fontWeight='bold'><FormattedMessage id='dialob.form.technicalName' /></Typography>
            </Grid>
            <Grid item md={9} lg={9} xl={9}>
              <Typography>{props.entry.formName}</Typography>
            </Grid>

            <Grid item md={3} lg={3} xl={3}>
              <Typography fontWeight='bold'><FormattedMessage id='dialob.form.lastSaved' /></Typography>
            </Grid>
            <Grid item md={9} lg={9} xl={9}>
              <Typography><Burger.DateTimeFormatter type='dateTime' value={props.entry.lastSaved} /></Typography>
            </Grid>

            <Grid item md={12} lg={12} xl={12}>
              <Typography fontWeight='bold'><FormattedMessage id='dialob.form.sessions' /></Typography>
            </Grid>

            <Grid item md={12} lg={12} xl={12}>
              {sessions.map((session) => <SessionItem entry={entry} form={form} session={session} />)}

            </Grid>
          </Grid>
        </Stack>

      </DialogContent>
      <DialogActions>
      </DialogActions>
    </Dialog>
  );
}

export default DialobSessionsDialog;