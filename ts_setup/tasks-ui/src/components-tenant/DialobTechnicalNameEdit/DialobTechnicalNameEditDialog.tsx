import React from 'react';
import { Dialog, DialogContent, DialogTitle, TextField, Box, DialogActions, IconButton, Typography, useTheme, alpha, Grid } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { FormattedMessage } from 'react-intl';
import { TenantEntryDescriptor } from 'descriptor-tenant';
import Burger from 'components-burger';



const TechnicalNameField: React.FC<{ entry: TenantEntryDescriptor }> = ({ entry }) => {
  const [technicalName, setTechnicalName] = React.useState(entry.formName);

  function handleTechnicalNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setTechnicalName(event.target.value);
  }

  return (<TextField InputProps={{ disableUnderline: true }}
    variant='standard'
    fullWidth
    multiline
    value={technicalName}
    onChange={handleTechnicalNameChange}
  />);
}


const DialobTechnicalNameEditDialog: React.FC<{
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
          <Typography variant='h4' fontWeight='bolder'><FormattedMessage id='dialob.form.technicalNameEdit.dialog.title' /></Typography>
          <Box flexGrow={1} />
          <IconButton onClick={props.onClose}>
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>

      <DialogContent>

        <Grid container spacing={1} direction='row'>


          <Grid item md={12} lg={12} xl={12}>
            <Burger.Section>
              <Typography fontWeight='bold'><FormattedMessage id='dialob.form.technicalName' /></Typography>
              <TechnicalNameField entry={props.entry} />
            </Burger.Section>
          </Grid>

          <Grid item md={3} lg={3} xl={3}>
            <Typography fontWeight='bold'><FormattedMessage id='dialob.form.title' /></Typography>
          </Grid>
          <Grid item md={9} lg={9} xl={9}>
            <Typography>{props.entry.formTitle}</Typography>
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
        <Burger.PrimaryButton label='buttons.accept' onClick={props.onClose} />
      </DialogActions>
    </Dialog>
  );
}

export default DialobTechnicalNameEditDialog;