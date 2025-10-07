import React, { useCallback, useMemo, useState } from 'react';
import { FormattedMessage, useIntl } from 'react-intl';
import { DialogContent, DialogTitle, Box, Divider, Typography, DialogActions, Button, Dialog } from '@mui/material';
import { checkHttpResponse, handleRejection } from '../middleware';
import type { FormConfiguration, DialobAdminConfig } from '../types';
import { useAdminBackend } from '../backend';
import { CancelButton } from '@dxs-ts/eveli-primitives';

export interface DeleteDialogProps {
  deleteModalOpen: boolean;
  handleDeleteModalClose: () => void;
  formConfiguration: FormConfiguration | undefined;
  setFetchAgain: React.Dispatch<React.SetStateAction<boolean>>;
  config: DialobAdminConfig;
}

export const DeleteDialog: React.FC<DeleteDialogProps> = ({
  setFetchAgain,
  deleteModalOpen,
  handleDeleteModalClose,
  formConfiguration,
  config,
}) => {
  const intl = useIntl();
  const { deleteAdminFormConfiguration } = useAdminBackend(config);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const titleText = useMemo(
    () =>
      formConfiguration?.metadata?.label ||
      intl.formatMessage({ id: 'adminUI.dialog.emptyTitle' }),
    [formConfiguration, intl]
  );

  const checkHttpResponseAsync = useCallback(
    async (response: any) => {
      try {
        return await checkHttpResponse(response, config.setLoginRequired);
      } catch (ex) {
        handleRejection(ex, config.setTechnicalError);
        throw ex;
      }
    },
    [config]
  );

  const handleConfirmDelete = useCallback(async () => {
    if (!formConfiguration) return;
    setIsSubmitting(true);
    try {
      const resp = await deleteAdminFormConfiguration(formConfiguration.id);
      await checkHttpResponseAsync(resp);
      setFetchAgain((prev) => !prev);
      handleDeleteModalClose();
    } catch (ex) {
      handleRejection(ex, config.setTechnicalError);
    } finally {
      setIsSubmitting(false);
    }
  }, [
    formConfiguration,
    deleteAdminFormConfiguration,
    checkHttpResponseAsync,
    setFetchAgain,
    handleDeleteModalClose,
    config,
  ]);

  const onKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === 'Enter' && !isSubmitting) {
        e.preventDefault();
        handleConfirmDelete();
      }
    },
    [isSubmitting, handleConfirmDelete]
  );

  return (
    <Box>
      <Dialog
        sx={{
          padding: '0 20px 20px 20px',
          border: 'none',
          height: '50%',
          top: '20%',
        }}
        open={deleteModalOpen}
        onClose={handleDeleteModalClose}
        onKeyDown={onKeyDown}
        maxWidth="lg"
        aria-labelledby="delete-dialog-title"
      >
        <DialogTitle sx={{ p: 3 }} id="delete-dialog-title">
          <Typography variant="h4" component="div">
            <FormattedMessage id="heading.deleteDialog" />
          </Typography>
        </DialogTitle>
        <Divider />
        <DialogContent sx={{ p: 3 }}>
          <Typography sx={{ my: 1 }}>
            <FormattedMessage id="adminUI.dialog.deleteQuestion" /> {`"${titleText}"?`}
          </Typography>
        </DialogContent>
        <Divider />
        <DialogActions sx={{ display: 'flex', justifyContent: 'space-between', p: '12px' }}>
          <CancelButton onClick={handleDeleteModalClose} />
          <Button
            color="error"
            onClick={handleConfirmDelete}
            disabled={isSubmitting || !formConfiguration}
          >
            <FormattedMessage id="button.accept" />
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
