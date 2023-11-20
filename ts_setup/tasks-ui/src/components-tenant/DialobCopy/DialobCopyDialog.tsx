import React from 'react';
import { Dialog, DialogContent, DialogTitle, Box, DialogActions, IconButton, Typography, useTheme, alpha, Stack, Alert, CircularProgress } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { FormattedMessage, useIntl } from 'react-intl';
import { TenantEntryDescriptor } from 'descriptor-tenant';
import Burger from 'components-burger';
import Fields from 'components-tenant/DialobCreate/DialobCreateFields';
import Context from 'context';


const DialobCopyDialog: React.FC<{
  open: boolean,
  onClose: () => void,
  setActiveDialob: (task?: TenantEntryDescriptor) => void,
  entry: TenantEntryDescriptor
}> = (props) => {
  const theme = useTheme();
  const intl = useIntl();
  const backend = Context.useBackend();
  const tenants = Context.useTenants();
  const [formName, setFormName] = React.useState<string>('');
  const prefix = intl.formatMessage({ id: 'dialob.form.copy.dialog.prefix' });
  const [formTitle, setFormTitle] = React.useState<string>(prefix + props.entry.formTitle);
  const [errorMessage, setErrorMessage] = React.useState<string>('');
  const [loading, setLoading] = React.useState(false);

  React.useEffect(() => {
    if (!formName.length) {
      setErrorMessage('dialob.form.technicalName.required');
    } else if (!/^[_\-a-zA-Z\d]*$/g.test(formName)) {
      setErrorMessage('dialob.form.technicalName.invalid');
    } else {
      setErrorMessage('');
    }
  }, [formName]);

  if (!props.open) {
    return null;
  }

  const handleCopy = async () => {
    setLoading(true);
    await backend.tenant.copyDialobForm(props.entry.formName, formName, formTitle, tenants.state.activeTenant).then((response) => {
      if (response.status === 'OK') {
        tenants.reload().then(() => {
          setErrorMessage('');
          setLoading(false);
          props.onClose();
          backend.tenant.getTenantEntries(tenants.state.activeTenant!).then(data => {
            const found = data.records.find(entry => entry.id === formName);
            if (found) {
              props.setActiveDialob({
                tenantId: tenants.state.activeTenant!,
                source: found,
                formName: formName,
                formTitle: formTitle,
                created: new Date(),
                lastSaved: new Date()
              });
            }
          });
        });
      } else {
        setLoading(false);
        setErrorMessage(response.error?.message!);
      }
    });
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
        <Stack overflow='auto' spacing={1} direction='column'>

          <Burger.Section required>
            <Typography fontWeight='bold'><FormattedMessage id='dialob.form.create.dialog.dialogName' /></Typography>
            <Fields.TechnicalName
              value={formName}
              onChange={setFormName}
            />
          </Burger.Section>

          <Burger.Section>
            <Typography fontWeight='bold'><FormattedMessage id='dialob.form.create.dialog.technicalName' /></Typography>
            <Fields.DialogName
              value={formTitle}
              onChange={setFormTitle}
            />
          </Burger.Section>

          {errorMessage && <Alert severity='error'>
            <FormattedMessage id={errorMessage} />
          </Alert>}
        </Stack>
      </DialogContent>

      <DialogActions>
        <Burger.SecondaryButton label='buttons.cancel' onClick={props.onClose} />
        {loading ? <CircularProgress size='16pt' /> : <Burger.PrimaryButton label='buttons.create' onClick={handleCopy} disabled={errorMessage.length > 0} />}
      </DialogActions>
    </Dialog>
  );
}

export default DialobCopyDialog;