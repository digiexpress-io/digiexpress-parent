import React from 'react';
import { Dialog, DialogContent, DialogTitle, Box, DialogActions, IconButton, Typography, useTheme, alpha, Grid } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { FormattedMessage, useIntl } from 'react-intl';
import { TenantEntryDescriptor } from 'descriptor-tenant';
import Burger from 'components-burger';

const FormNameInput: React.FC<{ value: string, onChange: (value: string) => void }> = ({ value, onChange }) => {
  const [error, setError] = React.useState<boolean>(false);
  const [errorMessage, setErrorMessage] = React.useState<string>('');

  React.useEffect(() => {
    if (!value.length) {
      setError(true);
      setErrorMessage('dialob.form.technicalName.required');
    } else if (!/^[a-zA-Z0-9_-]*$/.test(value)) {
      setError(true);
      setErrorMessage('dialob.form.technicalName.invalid');
    } else {
      setError(false);
      setErrorMessage('');
    }
  }, [value]);

  return (
    <Burger.TextField
      label='dialob.form.technicalName'
      value={value}
      onChange={onChange}
      error={error}
      errorMessage={errorMessage}
      required
    />
  );
}


const DialobCopyDialog: React.FC<{
  open: boolean,
  onClose: () => void,
  entry: TenantEntryDescriptor
}> = (props) => {
  const theme = useTheme();
  const intl = useIntl();
  const [formName, setFormName] = React.useState<string>('');
  const prefix = intl.formatMessage({ id: 'dialob.form.copy.dialog.prefix' });
  const [formTitle, setFormTitle] = React.useState<string>(prefix + props.entry.formTitle);

  const [error, setError] = React.useState<boolean>(false);
  const [errorMessage, setErrorMessage] = React.useState<string>('');

  React.useEffect(() => {
    if (!formName.length) {
      setError(true);
      setErrorMessage('dialob.form.technicalName.required');
    } else if (!/^[a-zA-Z0-9_-]*$/.test(formName)) {
      setError(true);
      setErrorMessage('dialob.form.technicalName.invalid');
    } else {
      setError(false);
      setErrorMessage('');
    }
  }, [formName]);

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
          <Typography variant='h4' fontWeight='bolder'><FormattedMessage id='dialob.form.copy.dialog.title' /></Typography>
          <Box flexGrow={1} />
          <IconButton onClick={props.onClose}>
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>

      <DialogContent>
        <Grid container spacing={1} direction='row'>

          <Grid item md={9} lg={9} xl={9}>
            <Burger.TextField
              label='dialob.form.technicalName'
              value={formName}
              onChange={setFormName}
              error={error}
              errorMessage={errorMessage}
              required
            />
          </Grid>

          <Grid item md={9} lg={9} xl={9}>
            <Burger.TextField value={formTitle} label='dialob.form.title' onChange={setFormTitle} />
          </Grid>

        </Grid>
      </DialogContent>

      <DialogActions>
        <Burger.SecondaryButton label='buttons.cancel' onClick={props.onClose} />
        <Burger.PrimaryButton label='buttons.create' onClick={props.onClose} disabled={error} />
      </DialogActions>
    </Dialog>
  );
}

export default DialobCopyDialog;