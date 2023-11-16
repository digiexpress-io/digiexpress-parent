import React from 'react';
import { Dialog, DialogContent, DialogTitle, Stack, Box, DialogActions, IconButton, Typography, useTheme, alpha, Alert } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { FormattedMessage, useIntl } from 'react-intl';

import DialobCreateActions from './DialobCreateActions';
import Fields from './DialobCreateFields';
import Burger from 'components-burger';


const DialobCreateDialog: React.FC<{ open: boolean, onClose: () => void }> = (props) => {
  const theme = useTheme();
  const intl = useIntl();
  const [formName, setFormName] = React.useState<string>('');
  const initialFormTitle = intl.formatMessage({ id: 'dialob.form.create.dialog.initial.title' });
  const [formTitle, setFormTitle] = React.useState<string>(initialFormTitle);
  const [errorMessage, setErrorMessage] = React.useState<string>('');

  React.useEffect(() => {
    if (!formName.length) {
      setErrorMessage('dialob.form.name.required');
    } else if (!/^[a-zA-Z0-9_-]*$/.test(formName)) {
      setErrorMessage('dialob.form.name.invalid');
    } else {
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
        <DialobCreateActions onClose={props.onClose} disabled={errorMessage.length > 0} />
      </DialogActions>
    </Dialog>
  );
}

export default DialobCreateDialog;