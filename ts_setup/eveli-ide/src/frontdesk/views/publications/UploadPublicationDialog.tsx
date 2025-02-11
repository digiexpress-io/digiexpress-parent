import React, { useContext } from 'react';

import { Dialog, DialogActions, DialogContent, DialogTitle, Stack, DialogContentText } from '@mui/material';


import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';

import { useConfig } from '../../context/ConfigContext';
import { SessionRefreshContext } from '../../context/SessionRefreshContext';
import { PublicationUpload } from '../../types/Publication';
import { handleErrors } from '../../util/cFetch';


import * as Burger from '@/burger';


export interface UploadReleaseProps {
  onSubmit: () => void;
  open: boolean;
  setOpen: (open: boolean) => void
}

export const UploadPublicationDialog: React.FC<UploadReleaseProps> = ({ onSubmit, open, setOpen }) => {
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const { serviceUrl } = useConfig()
  const session = useContext(SessionRefreshContext);
  const [file, setFile] = React.useState<string | undefined>();
  const init: PublicationUpload | undefined = file ? JSON.parse(file) : undefined;

  const handleClose = () => {
    setOpen(false);
  }

  const handleSubmit = (): void => {
    if(!file) {
      return;
    }

    session.cFetch(`${serviceUrl}worker/rest/api/assets/deployments`, {
      method: 'POST',
      headers: {
        'Accept': 'application/json'
      },
      body: init
    })
      .then(response => handleErrors(response))
      .then((response: any) => {
        setOpen(false);
        onSubmit();
      })
      .catch(error => {
        enqueueSnackbar(intl.formatMessage({ id: 'publications.tagCreationFailed' }, { cause: (error.message || 'N/A') }), { variant: 'error' });
      });
  }

  return (
      <Dialog open={open} onClose={handleClose} maxWidth='md' fullWidth>
        <DialogTitle fontWeight='bold'>{intl.formatMessage({ id: 'publications.dialogTitle' })}</DialogTitle>
        <DialogContent>
          <Stack spacing={1}>
            <Burger.FileField value="" onChange={setFile} label='publications.upload' />

            <DialogContentText sx={{whiteSpace: 'pre-wrap'}} variant='body2'>
              {init?.description}
            </DialogContentText>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Burger.SecondaryButton onClick={handleClose} label='button.cancel' />
          <Burger.PrimaryButton disabled={!file} onClick={handleSubmit} label='button.accept' />
        </DialogActions>
      </Dialog>
  );
}