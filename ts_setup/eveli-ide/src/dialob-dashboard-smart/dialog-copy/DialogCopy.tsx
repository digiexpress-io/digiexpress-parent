import React from 'react'
import { FormattedMessage } from 'react-intl';
import { DialogContent, DialogTitle, TextField, Typography, FormHelperText, Button, Dialog, Stack, DialogActions } from '@mui/material';


import { DialobRestApi, useDialobForms, Visitor_CreateNewForm } from '@/api-dialob-form';




export interface DialogCopyProps {
  onClose: () => void;
  source: DialobRestApi.FormListItem
}

export const DialogCopy: React.FC<DialogCopyProps> = ({ onClose, source }) => {

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
    copyForm({ newLabel: label, newName: name, sourceFormId: source.id });
    setSubmitting(true); 
  }

  return (
    <Dialog maxWidth='md' open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='heading.copyDialog' /> </DialogTitle>
      <DialogContent>
        <Stack spacing={1}>
          
          <Typography><FormattedMessage id="adminUI.dialog.formName" /></Typography>
          <TextField
            name='name'
            error={!!errors.name}
            required
            onChange={handleNameChange}
            value={name}
          />
          {errors.name && <FormHelperText error>{errors.name}</FormHelperText>}

          <Typography><FormattedMessage id="adminUI.dialog.formLabel" /></Typography>
          <TextField
            name='label'
            error={!!errors.label}
            required
            onChange={handleChangeLabel}
            value={label}
          />
          {errors.label && <FormHelperText error>{errors.label}</FormHelperText>}

        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}><FormattedMessage id={'button.cancel'} /></Button>
        <Button onClick={handleSubmit} disabled={isSubmitting || isErrors} ><FormattedMessage id={'button.accept'} /></Button>
      </DialogActions>
    </Dialog>
  )
}
