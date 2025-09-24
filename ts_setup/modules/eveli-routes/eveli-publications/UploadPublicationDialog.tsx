import React from 'react';

import { Dialog, DialogActions, DialogContent, DialogTitle, Stack, DialogContentText, Button, Typography, Alert } from '@mui/material';

import { FormattedMessage } from 'react-intl';
import { PublicationApi } from '@dxs-ts/eveli-api';
import { useFetch } from '@dxs-ts/envir-fetch';
import { CancelButton } from '@dxs-ts/eveli-primitives';


export interface UploadReleaseProps {
  onSubmit: () => void;
  open: boolean;
  setOpen: (open: boolean) => void
}

export const UploadPublicationDialog: React.FC<UploadReleaseProps> = ({ onSubmit, open, setOpen }) => {
  const inputFile = React.useRef<HTMLInputElement>(null);
  const [uploadErrorText , setUploadErrorText] = React.useState<string>();
  const { saveDeployment } = useFetch('worker/rest/api/assets/deployments.POST', {});

  const handleClose = () => {
    setOpen(false);
  }

  async function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {

    try {
      const file: File = (e.target as any).files[0];
      const enc = new TextDecoder("utf-8");
      const decoded = await file.arrayBuffer().then(d => enc.decode(d));
      const init: PublicationApi.PublicationUpload = JSON.parse(decoded);
      await saveDeployment(init, () => {
        setOpen(false);
        onSubmit();
      });
    } catch (error: any) {
      
      console.error(error)
      if (error instanceof SyntaxError) {
        setUploadErrorText(error.stack);
        return 
      }
      setUploadErrorText(JSON.stringify(error))
    }
  }

  return (
      <Dialog open={open} onClose={handleClose} maxWidth='sm' >
          <input type="file" hidden ref={inputFile} accept="json" onChange={handleFileChange} />
            <DialogTitle>
             <FormattedMessage id='publications.uploadDialogTitle'/>
            </DialogTitle>
            <DialogContent>
              { !!uploadErrorText &&
                <Alert severity="error">{uploadErrorText}</Alert>
              }
              <DialogContentText>
                <FormattedMessage id='publications.dialog.upload.description'/> 
              </DialogContentText>          
            </DialogContent>
            <DialogActions>
              <CancelButton onClick={handleClose} />
              <Button 
                onClick={() => inputFile.current?.click()}
                autoFocus
              >
                <FormattedMessage id='publications.upload'/>        
              </Button>
            </DialogActions>
      </Dialog>
  );
}