import React from 'react';
import { useIntl, FormattedMessage } from 'react-intl';
import { useBlocker } from '@tanstack/react-router'

import { Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Button } from '@mui/material';
import { CancelButton } from '@/eveli-styles';



export const PageLeavingConfirmation: React.FC<{
  navigationConfirmationRequired: () => boolean;
}> = ({ navigationConfirmationRequired }) => {


  const intl = useIntl();
  const { proceed, reset, status } = useBlocker({
    shouldBlockFn: ({ current, next }) => {
      return navigationConfirmationRequired();
    },
    enableBeforeUnload: false,
    withResolver: true,
  })
  const open = status === 'blocked';
  const text = intl.formatMessage({ id: 'confirm.unsavedChanges' });

  function onClose() {
    if(reset) {
      reset();
    }
  }

  function onAccept() {
    if(proceed) {
      proceed() 
    }
  }
  function onCancel() {
    if(reset) {
      reset();
    }
  }

  return ( 
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'confirm.close.title' })}</DialogTitle>
      <DialogContent>
        <DialogContentText>{text}</DialogContentText>
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onCancel} />
        <Button variant='contained' onClick={onAccept}>
          <FormattedMessage id='button.accept'/>
        </Button>
      </DialogActions>
    </Dialog>
  );
};