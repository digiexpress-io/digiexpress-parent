import React from 'react';

import { Dialog, DialogActions, DialogContent, DialogTitle, Stack, DialogContentText, Button } from '@mui/material';

import { useIntl, FormattedMessage } from 'react-intl';
import { useFetch } from '@dxs-ts/eveli-fetch';
import * as Burger from '@/burger';

import { PublicationApi } from '../api-publications';


export interface UploadReleaseProps {
  onSubmit: () => void;
  open: boolean;
  setOpen: (open: boolean) => void
}

export const UploadPublicationDialog: React.FC<UploadReleaseProps> = ({ onSubmit, open, setOpen }) => {
  const intl = useIntl();
  const [file, setFile] = React.useState<string | undefined>();
  const init: PublicationApi.PublicationUpload | undefined = file ? JSON.parse(file) : undefined;

  const { saveDeployment } = useFetch('worker/rest/api/assets/deployments.POST', {});

  const handleClose = () => {
    setOpen(false);
  }

  const handleSubmit = (): void => {
    if(!init) {
      return;
    }

    saveDeployment(init, () => {
      setOpen(false);
      onSubmit();
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
          <Button onClick={handleClose}  variant='text'><FormattedMessage id='button.cancel'/></Button>
          <Button variant='contained' disabled={!file} onClick={handleSubmit}  ><FormattedMessage id='button.accept'/></Button>
        </DialogActions>
      </Dialog>
  );
}