import { DialogContent, DialogTitle, Button, Dialog, DialogActions, Stack, Typography } from '@mui/material';
import { DialobRestApi, useDialobForms } from '@dxs-ts/eveli-api';
import { FormattedMessage } from 'react-intl';
import React from 'react';
import { CancelButton } from '../../eveli-styles';

export interface DialogDeleteProps {
  onClose: () => void;
  source: DialobRestApi.FormListItem;
}

export const DialogDelete: React.FC<DialogDeleteProps> = ({ onClose, source }) => {
  const { deleteForm } = useDialobForms();
  const [isSubmitting, setSubmitting] = React.useState(false);

  const handleSubmit = async () => {
    if (isSubmitting) return;
    setSubmitting(true);
    try {
      await deleteForm({ form: source });
      onClose();
    } finally {
      setSubmitting(false);
    }
  };

  const onKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !isSubmitting) {
      e.preventDefault();
      handleSubmit();
    }
  };

  const title = source?.metadata?.label;

  return (
    <Dialog open maxWidth="md" onClose={onClose} onKeyDown={onKeyDown}>
      <DialogTitle>
        <FormattedMessage id="heading.deleteDialog" />
      </DialogTitle>

      <DialogContent>
        <Stack spacing={1}>
          <Typography>
            <FormattedMessage id="adminUI.dialog.deleteQuestion" />{' '}
            {title || <FormattedMessage id="adminUI.dialog.emptyTitle" />}?
          </Typography>
        </Stack>
      </DialogContent>

      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button color="error" disabled={isSubmitting} onClick={handleSubmit}>
          <FormattedMessage id="button.accept" />
        </Button>
      </DialogActions>
    </Dialog>
  );
};
