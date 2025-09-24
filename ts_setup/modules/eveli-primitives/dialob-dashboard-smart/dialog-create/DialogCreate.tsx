import React from 'react'
import { FormattedMessage } from 'react-intl';
import { DialogContent, DialogTitle, TextField, Typography, FormHelperText, Button, Dialog, Stack, DialogActions } from '@mui/material';

import { Visitor_CreateNewForm, DialobRestApi } from '@dxs-ts/eveli-api';
import { useDialobForms } from '@dxs-ts/eveli-api';

 
const template: DialobRestApi.CreateFormRequest = {
  name: '',
  data: {
    questionnaire: {
      id: 'questionnaire',
      type: 'questionnaire',
      items: []
    }
  },
  metadata: {
    label: '',
    languages: ['fi', 'en']
  }
};


export interface DialogCreateProps {
  onClose: () => void;
}

export const DialogCreate: React.FC<DialogCreateProps> = ({ onClose }) => {

  const { createForm } = useDialobForms();
  
  const [label, setLabel] = React.useState('New form');
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
    createForm({ label, name, template });
    setSubmitting(true); 
  }

  return (
    <Dialog maxWidth='md' open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='heading.addDialog' /> </DialogTitle>
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
