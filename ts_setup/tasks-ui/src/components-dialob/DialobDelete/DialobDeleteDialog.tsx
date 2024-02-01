import React from 'react';
import { Dialog, DialogContent, DialogTitle, Box, DialogActions, IconButton, Typography, alpha, Grid, CircularProgress } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { FormattedMessage } from 'react-intl';
import { TenantEntryDescriptor } from 'descriptor-dialob';
import Burger from 'components-burger';
import Context from 'context';
import { DialobSession } from 'client';
import { sambucus, wash_me } from 'components-colors';


const DialobDeleteDialog: React.FC<{
  open: boolean,
  onClose: () => void,
  entry: TenantEntryDescriptor
}> = (props) => {
  const backend = Context.useBackend();
  const tenants = Context.useDialobTenant();
  const [sessions, setSessions] = React.useState<DialobSession[]>();
  const [loading, setLoading] = React.useState(false);
  const [deleting, setDeleting] = React.useState(false);

  React.useEffect(() => {
    if (props.entry?.formName) {
      setLoading(true);
      backend.tenant.getDialobForm(props.entry.formName).then((form) => {
        backend.tenant.getDialobSessions({ formId: form._id, technicalName: props.entry.formName, tenantId: props.entry.tenantId }).then(sessions => {
          setSessions(sessions);
          setLoading(false);
        })
      });
    }
  }, [props.entry]);

  if (!props.open) {
    return null;
  }

  const handleDelete = async () => {
    setDeleting(true);
    backend.tenant.deleteDialobForm(props.entry.formName, tenants.state.activeTenant).then((response) => {
      tenants.reload().then(() => {
        setDeleting(false);
        props.onClose();
      });
    });
  }

  return (

    <Dialog open={true} fullWidth maxWidth='md'>
      <DialogTitle sx={{
        backgroundColor: wash_me,
        borderBottom: `1px solid ${alpha(sambucus, 0.3)}`,
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
            {loading ? <CircularProgress size='10pt' /> : <Typography>{sessions?.length}</Typography>}
          </Grid>


        </Grid>
      </DialogContent>
      <DialogActions>
        <Burger.SecondaryButton label='buttons.cancel' onClick={props.onClose} />
        {deleting ? <CircularProgress size='16pt' /> : <Burger.PrimaryButton label='buttons.delete' onClick={handleDelete} />}
      </DialogActions>
    </Dialog>
  );
}

export default DialobDeleteDialog;