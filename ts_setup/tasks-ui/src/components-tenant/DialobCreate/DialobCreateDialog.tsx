import React from 'react';
import { Dialog, DialogContent, DialogTitle, Stack, Box, DialogActions, IconButton, Typography, useTheme, alpha, Alert } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { FormattedMessage, useIntl } from 'react-intl';

import DialobCreateActions from './DialobCreateActions';
import Fields from '../DialobFields/DialobTextFields';
import Burger from 'components-burger';
import { CreateFormRequest } from 'client/tenant-types';
import Context from 'context';
import { TenantEntryDescriptor } from 'descriptor-tenant';
import { sambucus, wash_me } from 'components-colors';

const INIT_FORM: CreateFormRequest = {
  name: "",
  metadata: {
    label: "",
    languages: ['en'],
    labels: []
  },
  data: {
    questionnaire: {
      id: 'questionnaire',
      type: 'questionnaire',
    }
  },
  variables: []
};

const DialobCreateDialog: React.FC<{ open: boolean, onClose: () => void, setActiveDialob: (entry?: TenantEntryDescriptor) => void }> = (props) => {
  const theme = useTheme();
  const intl = useIntl();
  const backend = Context.useBackend();
  const tenants = Context.useTenants();
  const [formName, setFormName] = React.useState<string>('');
  const initialFormTitle = intl.formatMessage({ id: 'dialob.form.create.dialog.initial.title' });
  const [formTitle, setFormTitle] = React.useState<string>(initialFormTitle);
  const [errorMessage, setErrorMessage] = React.useState<string>('');
  const [loading, setLoading] = React.useState(false);

  React.useEffect(() => {
    Fields.validateTehnicalName(formName, setErrorMessage);
  }, [formName]);

  if (!props.open) {
    return null;
  }

  const handleCreate = async (): Promise<void> => {
    const request: CreateFormRequest = {
      ...INIT_FORM,
      name: formName,
      metadata: {
        ...INIT_FORM.metadata,
        label: formTitle
      }
    };
    setLoading(true);
    await backend.tenant.createDialobForm(request, tenants.state.activeTenant).then((response) => {
      if (response.status === 'OK') {
        tenants.reload().then(() => {
          setErrorMessage('');
          setLoading(false);
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
        backgroundColor: wash_me,
        borderBottom: `1px solid ${alpha(sambucus, 0.3)}`,
        mb: 1,
      }}>
        <Box display='flex' alignItems='center'>
          <Typography variant='h4' fontWeight='bolder'><FormattedMessage id='dialob.form.create.dialog.title' /></Typography>
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
        <DialobCreateActions onClose={props.onClose} onCreate={handleCreate} disabled={errorMessage.length > 0} loading={loading} />
      </DialogActions>
    </Dialog>
  );
}

export default DialobCreateDialog;