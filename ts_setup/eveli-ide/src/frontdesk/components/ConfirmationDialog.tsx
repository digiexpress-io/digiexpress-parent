import { Button, ButtonProps, Dialog, DialogActions, DialogContent, DialogContentText, DialogProps, DialogTitle } from '@mui/material';
import React from 'react';
import { FormattedMessage } from 'react-intl';

export interface ConfirmationDialogProps  {
  title ?: string;
  accept ?: string;
  cancel ?: string;
  dialogOptions ?: Partial<DialogProps>;
  cancelOptions ?: Partial<ButtonProps>;
  acceptOptions ?: Partial<ButtonProps>;
  open: boolean;
  text: string;
  onClose: () => void;
  onAccept: () => void;
  onCancel: () => void;
};

export const ConfirmationDialog: React.FC<ConfirmationDialogProps> = ({open, title, text, accept, cancel, onClose, onAccept, onCancel, dialogOptions, cancelOptions, acceptOptions}) => {

  return (
    <Dialog open={open} onClose={onClose} {...dialogOptions}>
      { title && <DialogTitle>{title}</DialogTitle> }
      <DialogContent>
        <DialogContentText>{text}</DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel} {...cancelOptions} color='secondary'>{cancel || <FormattedMessage id='button.cancel' /> }</Button>
        <Button onClick={onAccept} color='primary' {...acceptOptions}>{accept || <FormattedMessage id='button.accept' />}</Button>
      </DialogActions>
    </Dialog>
  );
}