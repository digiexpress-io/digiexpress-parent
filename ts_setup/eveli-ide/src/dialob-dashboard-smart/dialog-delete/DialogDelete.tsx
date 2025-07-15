
import { DialogContent, DialogTitle, Button, Dialog, DialogActions, Stack } from '@mui/material';
import { DialobRestApi, useDialobForms } from '@/api-dialob-form';
import { FormattedMessage } from 'react-intl';
import React from 'react';



export interface DialobActionCreateProps {
  onClose: () => void;
  source: DialobRestApi.FormListItem
}

export const DialobActionDelete: React.FC<DialobActionCreateProps> = ({ onClose, source }) => {
  const { deleteForm } = useDialobForms();
  const [isSubmitting, setSubmitting] = React.useState(false);
  const handleSubmit = async () => {
    setSubmitting(false);
    deleteForm({form: source});
    setSubmitting(true); 
  }
  return (
    <Dialog open={true} maxWidth='md' onClose={onClose}>
      <DialogTitle>
        <FormattedMessage id='heading.deleteDialog' />
      </DialogTitle>

      <DialogContent>
        <Stack spacing={1}>
          <div>
            <FormattedMessage id='adminUI.dialog.deleteQuestion' /> 
            {source.metadata.label || <FormattedMessage id='adminUI.dialog.emptyTitle' />}
            {"?"}
          </div>
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}><FormattedMessage id='button.cancel' /></Button>
        <Button disabled={isSubmitting} color='error' onClick={handleSubmit}><FormattedMessage id={'button.accept'} /></Button>
      </DialogActions>
    </Dialog>);
}