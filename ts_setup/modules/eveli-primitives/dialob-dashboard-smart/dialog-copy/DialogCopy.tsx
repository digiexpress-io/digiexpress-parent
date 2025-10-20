import React from 'react'
import { useIntl } from 'react-intl';
import { DialogContent, DialogTitle, TextField, Typography, FormHelperText, Button, Dialog, Stack, DialogActions } from '@mui/material';
import { DialobRestApi, useDialobForms, Visitor_CreateNewForm } from '@dxs-ts/eveli-api';


export interface DialogCopyProps {
  onClose: () => void;
  source: DialobRestApi.FormListItem
}

export const DialogCopy: React.FC<DialogCopyProps> = ({ onClose, source }) => {
  const intl = useIntl();
  const { copyForm } = useDialobForms();

  const [label, setLabel] = React.useState("Copy of " + source.metadata.label);
  const [name, setName] = React.useState('');
  const [isSubmitting, setSubmitting] = React.useState(false);

  const errors = Visitor_CreateNewForm.validateInput({ name, label });
  const isErrors: boolean = !!(errors.label || errors.name);

  function handleNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setName(event.currentTarget.value);
  }
  function handleChangeLabel(event: React.ChangeEvent<HTMLInputElement>) {
    setLabel(event.currentTarget.value);
  }

  const handleSubmit = async () => {
    setSubmitting(false);
    const copy = await copyForm({ newLabel: label, newName: name, sourceFormId: source.id });
    console.log('Form copied', copy);
    setSubmitting(true);
  }

  return (
    <Dialog maxWidth='md' open={true} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'heading.copyDialog' })}</DialogTitle>
      <DialogContent>
        <Stack spacing={1}>

          <Typography>{intl.formatMessage({ id: "adminUI.dialog.formName" })}</Typography>
          <TextField
            name='name'
            error={!!errors.name}
            required
            onChange={handleNameChange}
            value={name}
          />
          {errors.name && <FormHelperText error>{intl.formatMessage({ id: errors.name })}</FormHelperText>}

          <Typography>{intl.formatMessage({ id: "adminUI.dialog.formLabel" })}</Typography>
          <TextField
            name='label'
            error={!!errors.label}
            required
            onChange={handleChangeLabel}
            value={label}
          />
          {errors.label && <FormHelperText error>{intl.formatMessage({ id: errors.label })}</FormHelperText>}

        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={handleSubmit} disabled={isSubmitting || isErrors}>
          {intl.formatMessage({ id: 'button.accept' })}</Button>
      </DialogActions>
    </Dialog>
  )
}
