import React from 'react'
import { FormattedMessage } from 'react-intl';
import { DialogContent, DialogTitle, TextField, Typography, Button, Dialog, DialogActions } from '@mui/material';

import { DialobRestApi, useDialobForms } from '@/api-dialob-form';




export interface DialogAddLabelProps {
  onClose: () => void;
  source: DialobRestApi.FormListItem
}

export const DialogAddLabel: React.FC<DialogAddLabelProps> = ({ onClose, source }) => {

  const { addFormLabel } = useDialobForms();
  const [label, setLabel] = React.useState('');
  const [isSubmitting, setSubmitting] = React.useState(false);
  const isErrors: boolean = !label;

  function handleChangeLabel(event: React.ChangeEvent<HTMLInputElement>) {
    setLabel(event.currentTarget.value);
  }

  const handleSubmit = async () => {
    setSubmitting(false);
    const added = await addFormLabel({ newLabel: label, form: source });
    console.log('Form label added', added);
    setSubmitting(true); 
  }

  return (
    <Dialog maxWidth='md' open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='heading.addLabel' defaultMessage='Add new label' /> </DialogTitle>
      <DialogContent>
        <Typography><FormattedMessage id="adminUI.dialog.newLabel" defaultMessage='Value of the label' /></Typography>
        <TextField
          name='label'
          error={isErrors}
          required
          onChange={handleChangeLabel}
          value={label}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}><FormattedMessage id={'button.cancel'} /></Button>
        <Button onClick={handleSubmit} disabled={isSubmitting || isErrors} ><FormattedMessage id={'button.accept'} /></Button>
      </DialogActions>
    </Dialog>
  )
}
