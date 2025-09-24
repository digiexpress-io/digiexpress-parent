import React from 'react'
import { FormattedMessage, useIntl } from 'react-intl';
import { DialogContent, DialogTitle, Box, Divider, Typography, DialogActions, Button, Dialog } from '@mui/material';
import { checkHttpResponse, handleRejection } from '../middleware';
import type { FormConfiguration, DialobAdminConfig } from '../types';
import { useAdminBackend } from '../backend';

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
  config
}) => {
  const intl = useIntl();

  const { deleteAdminFormConfiguration } = useAdminBackend(config);

  const deleteDialog = async () => {
    if (formConfiguration) {
      deleteAdminFormConfiguration(formConfiguration.id)
        .then((response: Response) => checkHttpResponse(response, config.setLoginRequired))
        .then((response: { json: () => any; }) => response.json())
        .then((response: any) => {
          handleDeleteModalClose();
          setFetchAgain(prevState => !prevState);
        })
        .catch((ex: any) => {
          handleRejection(ex, config.setTechnicalError);
        });
    }
  };

  return (
    <Box>
      <Dialog
        sx={{
          padding: '0 20px 20px 20px',
          border: 'none',
          height: '50%',
          top: "20%"
        }}
        open={deleteModalOpen}
        onClose={handleDeleteModalClose}
        maxWidth={'lg'}
      >
        <DialogTitle sx={{ m: 0, p: 2 }}>
          <Typography variant="h4" component="div"><FormattedMessage id='heading.deleteDialog' /></Typography>
        </DialogTitle>
        <Divider />
        <DialogContent>
          <Typography sx={{ padding: "20px 4px 4px 2px" }}><FormattedMessage id="adminUI.dialog.deleteQuestion" /> {`"${formConfiguration?.metadata.label || intl.formatMessage({ id: "adminUI.dialog.emptyTitle" })}"?`}</Typography>
        </DialogContent>
        <Divider />
        <DialogActions sx={{ display: "flex", justifyContent: "space-between", padding: "12px" }}>
          <Button onClick={handleDeleteModalClose}><FormattedMessage id={'button.cancel'} /></Button>
          <Button
            color='error'
            onClick={() => deleteDialog()}
          >
            <FormattedMessage id={'button.accept'} />
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
