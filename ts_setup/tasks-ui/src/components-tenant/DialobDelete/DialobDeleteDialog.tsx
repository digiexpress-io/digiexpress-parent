import React from 'react';
import { Dialog, DialogContent, DialogTitle, Box, DialogActions, IconButton, Typography, useTheme, alpha, Grid } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { FormattedMessage } from 'react-intl';
import { TenantEntryDescriptor } from 'descriptor-tenant';
import Burger from 'components-burger';


const DialobDeleteDialog: React.FC<{
  open: boolean,
  onClose: () => void,
  entry: TenantEntryDescriptor
}> = (props) => {
  const theme = useTheme();

  if (!props.open) {
    return null;
  }


  return (

    <Dialog open={true} fullWidth maxWidth='md'>
      <DialogTitle sx={{
        backgroundColor: theme.palette.mainContent.main,
        borderBottom: `1px solid ${alpha(theme.palette.mainContent.dark, 0.3)}`,
        mb: 1,
      }}>
        <Box display='flex' alignItems='center'>
          <Typography variant='h4' fontWeight='bolder' color='error'><FormattedMessage id='dialob.form.delete.dialog.title' /></Typography>
          <Box flexGrow={1} />
          <IconButton onClick={props.onClose}>
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>

      <DialogContent>
        <Typography sx={{ mb: 3 }}><FormattedMessage id='dialob.form.delete.dialog.desc' /></Typography>

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

          <Grid item md={3} lg={3} xl={3}>
            <Typography fontWeight='bold'><FormattedMessage id='dialob.form.sessions' /></Typography>
          </Grid>
          <Grid item md={9} lg={9} xl={9}>
            <Typography>SESSIONS</Typography>
          </Grid>


        </Grid>
      </DialogContent>
      <DialogActions>
        <Burger.SecondaryButton label='buttons.cancel' onClick={props.onClose} />
        <Burger.PrimaryButton label='buttons.delete' onClick={props.onClose} />
      </DialogActions>
    </Dialog>
  );
}

export default DialobDeleteDialog;