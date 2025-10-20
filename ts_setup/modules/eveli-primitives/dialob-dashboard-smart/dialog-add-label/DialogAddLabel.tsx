import React from 'react'
import { FormattedMessage, useIntl } from 'react-intl';
import { DialogContent, DialogTitle, TextField, Typography, Button, Dialog, DialogActions, FormHelperText } from '@mui/material';

import { DialobRestApi, useDialobForms } from '@dxs-ts/eveli-api';




export interface DialogAddLabelProps {
  onClose: () => void;
  source: DialobRestApi.FormListItem
}

export const DialogAddLabel: React.FC<DialogAddLabelProps> = ({ onClose, source }) => {
  const intl = useIntl();
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
      <DialogTitle>{intl.formatMessage({ id: "adminUI.dialog.form.addFormLabel.title" })}</DialogTitle>
      <DialogContent>
        <Typography>{intl.formatMessage({ id: "adminUI.dialog.form.addFormLabel.desc" })}</Typography>
        <TextField
          name='label'
          error={isErrors}
          required
          onChange={handleChangeLabel}
          value={label}
        />
        {isErrors && <FormHelperText error>{intl.formatMessage({ id: 'error.valueRequired' })}</FormHelperText>}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={handleSubmit} disabled={isSubmitting || isErrors} >{intl.formatMessage({ id: 'button.accept' })}</Button>
      </DialogActions>
    </Dialog>
  )
}
