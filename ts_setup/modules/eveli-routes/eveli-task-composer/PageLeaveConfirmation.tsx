import React from 'react';
import { useIntl, FormattedMessage } from 'react-intl';
import { useBlocker } from '@tanstack/react-router'

import { Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Button } from '@mui/material';
import { CancelButton } from '@dxs-ts/eveli-primitives';



export const PageLeavingConfirmation: React.FC<{
  navigationConfirmationRequired: () => boolean;
}> = ({ navigationConfirmationRequired }) => {


  const intl = useIntl();
  const { proceed, reset, status } = useBlocker({
    shouldBlockFn: ({ current, next }) => {
      //@ts-ignore
      const oldLocale = current.params.locale;
      //@ts-ignore
      const newLocale = next.params.locale;
      if(!oldLocale || !newLocale) {
        return navigationConfirmationRequired();
      }
      if(oldLocale != newLocale && current.pathname === next.pathname.replace(`/${newLocale}/`, `/${oldLocale}/`)) {
        return false;
      }
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