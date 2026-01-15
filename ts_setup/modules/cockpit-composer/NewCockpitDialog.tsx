import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Stack,
  Typography
} from '@mui/material';
import { useIntl } from 'react-intl';
import { useQueryClient } from '@tanstack/react-query';
import { CockpitApi, useCockpitsBackend } from '@dxs-ts/cockpit-api';
import { COCKPIT_TABLE_QUERY_KEY } from './cockpit-table/CockpitTable';

export interface NewCockpitDialogProps {
  open: boolean;
  setOpen: (open: boolean) => void;
  onSubmit: () => void;
}

export const NewCockpitDialog: React.FC<NewCockpitDialogProps> = ({
  open,
  setOpen,
  onSubmit
}) => {
  const intl = useIntl();
  const backend = useCockpitsBackend();
  const queryClient = useQueryClient();

  const [configName, setConfigName] = React.useState('');
  const [configDescription, setConfigDescription] = React.useState('');
  const [isSubmitting, setSubmitting] = React.useState(false);

  const handleClose = () => {
    setOpen(false);
    setConfigName('');
    setConfigDescription('');
    setSubmitting(false);
  };

  async function handleCreateCockpit() {
    if (!configName.trim() || !configDescription.trim()) {
      return;
    }

    setSubmitting(true);

    try {
      const command: CockpitApi.CreateCockpitCommand = {
        configName: configName.trim(),
        configDescription: configDescription.trim()
      };

      await backend.persistence.createOneCockpit(command);
      await queryClient.refetchQueries({ exact: true, queryKey: [COCKPIT_TABLE_QUERY_KEY] }, { throwOnError: false });

      handleClose();
      onSubmit();
    } catch (error) {
      console.error('Error creating cockpit:', error);
      setSubmitting(false);
    }
  }

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="sm"
      fullWidth
    >
      <DialogTitle>
        {intl.formatMessage({ id: 'cockpitCreate.dialog.title' })}
      </DialogTitle>

      <DialogContent>
        <Stack spacing={1}>
          <Typography fontWeight="bold">
            {intl.formatMessage({ id: 'cockpitCreate.field.name' })}
          </Typography>
          <TextField
            fullWidth
            required
            value={configName}
            onChange={(e) => setConfigName(e.target.value)}
          />

          <Typography fontWeight="bold">
            {intl.formatMessage({ id: 'cockpitCreate.field.description' })}
          </Typography>
          <TextField
            fullWidth
            multiline
            rows={3}
            value={configDescription}
            onChange={(e) => setConfigDescription(e.target.value)}
          />
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={handleClose} disabled={isSubmitting}>
          {intl.formatMessage({ id: 'button.cancel' })}
        </Button>
        <Button
          onClick={handleCreateCockpit}
          variant="contained"
          disabled={isSubmitting || !configName.trim() || !configDescription.trim()}
        >
          {isSubmitting
            ? intl.formatMessage({ id: 'cockpitCreate.button.creating' })
            : intl.formatMessage({ id: 'button.accept' })
          }
        </Button>
      </DialogActions>
    </Dialog>
  );
};