import { ButtonProps, Dialog, DialogActions, DialogContent, DialogContentText, DialogProps, DialogTitle, Button } from '@mui/material';
import React from 'react';

import * as Burger from '@/burger';
import { FormattedMessage } from 'react-intl';

export interface ConfirmationDialogProps {
  title?: string;
  accept?: string;
  cancel?: string;
  dialogOptions?: Partial<DialogProps>;
  cancelOptions?: Partial<ButtonProps>;
  acceptOptions?: Partial<ButtonProps>;
  open: boolean;
  text: string;
  onClose: () => void;
  onAccept: () => void;
  onCancel: () => void;
};

export const ConfirmationDialog: React.FC<ConfirmationDialogProps> = (props) => {
  const { open, title, text, accept, cancel, onClose, onAccept, onCancel, dialogOptions, cancelOptions, acceptOptions } = props;

  const handleCancel: React.MouseEventHandler<HTMLElement> = (event) => {
    onCancel();
  };

  const handleAccept: React.MouseEventHandler<HTMLElement> = (event) => {
    onAccept();
  };

  return (
    <Dialog open={open} onClose={onClose} {...dialogOptions}>
      {title && <DialogTitle>{title}</DialogTitle>}
      <DialogContent>
        <DialogContentText>{text}</DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleCancel} variant='text' {...cancelOptions}>
          <FormattedMessage id={cancel || 'button.cancel'}/>
        </Button>
        <Button variant='contained' onClick={handleAccept} {...acceptOptions}>
          <FormattedMessage id={accept || 'button.accept'}/>
        </Button>
      </DialogActions>
    </Dialog>
  );
}