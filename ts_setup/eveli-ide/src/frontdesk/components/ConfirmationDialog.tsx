import { ButtonProps, Dialog, DialogActions, DialogContent, DialogContentText, DialogProps, DialogTitle } from '@mui/material';
import React from 'react';

import * as Burger from '@/burger';

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
        {/* TODO correct types */}
        {/* @ts-ignore */}
        <Burger.SecondaryButton onClick={handleCancel}
          {...cancelOptions} label={cancel || 'button.cancel'} />
        {/* @ts-ignore */}
        <Burger.PrimaryButton onClick={handleAccept}
          {...acceptOptions} label={accept || 'button.accept'} />
      </DialogActions>
    </Dialog>
  );
}