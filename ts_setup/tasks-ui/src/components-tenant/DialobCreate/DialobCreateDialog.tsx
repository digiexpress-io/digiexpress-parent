import React from 'react';
import { Dialog, DialogContent, DialogTitle, Stack, Box, DialogActions, IconButton, Typography, useTheme, alpha } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { FormattedMessage } from 'react-intl';

import DialobCreateActions from './DialobCreateActions';
import Fields from './DialobCreateFields';
import Burger from 'components-burger';


const DialobCreateDialog: React.FC<{ open: boolean, onClose: () => void }> = (props) => {
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
          <Typography variant='h4' fontWeight='bolder'><FormattedMessage id='dialob.form.create.dialog.title' /></Typography>
          <Box flexGrow={1} />
          <IconButton onClick={props.onClose}>
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>

      <DialogContent>
        <Stack overflow='auto' spacing={1} direction='column'>
          <Burger.Section>
            <Typography fontWeight='bold'><FormattedMessage id='dialob.form.create.dialog.dialogName' /></Typography>
            <Fields.DialogName />
          </Burger.Section>

          <Burger.Section>
            <Typography fontWeight='bold'><FormattedMessage id='dialob.form.create.dialog.technicalName' /></Typography>
            <Fields.TechnicalName />
          </Burger.Section>

        </Stack>
      </DialogContent>
      <DialogActions>
        <DialobCreateActions onClose={props.onClose} />
      </DialogActions>
    </Dialog>
  );
}

export default DialobCreateDialog;