import { createFileRoute } from '@tanstack/react-router'
import React from 'react';

import { Dialog, DialogActions, DialogContent, DialogTitle, DialogContentText, Button, Alert } from '@mui/material';

import { FormattedMessage } from 'react-intl';
import { useFetch } from '@dxs-ts/envir-fetch';

import { PublicationApi } from '@dxs-ts/eveli-api';
import { parseStencilSearchParams, StencilRouteSearchParams } from '@dxs-ts/stencil-routes';
import { CancelButton } from '@dxs-ts/eveli-primitives';


export const Route = createFileRoute('/secured/$locale/assets/migrate/')({
  component: Component,
  validateSearch: (search: Record<string, unknown>): StencilRouteSearchParams => parseStencilSearchParams(search)
})

function Component() {
  const { locale } = Route.useParams();
  
  
  const inputFile = React.useRef<HTMLInputElement>(null);
  const [uploadErrorText , setUploadErrorText] = React.useState<string>();
  const [open, setOpen] = React.useState(true);
  const { migrateAsset } = useFetch('worker/rest/api/assets/migration.POST', {});

  async function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    try {
      const file: File = (e.target as any).files[0];
      const enc = new TextDecoder("utf-8");
      const decoded = await file.arrayBuffer().then(d => enc.decode(d));
      const init: PublicationApi.PublicationUpload = JSON.parse(decoded);
      await migrateAsset(init, () => {
        setOpen(false);
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
  const handleClose = () => {
    setOpen(false);
  }

  return (
  <Dialog open={open} onClose={handleClose} maxWidth='sm' >
    <input type="file" hidden ref={inputFile} accept="json" onChange={handleFileChange} />
      <DialogTitle>
       <FormattedMessage id='migration.dialogTitle'/>
      </DialogTitle>
      <DialogContent>
        { !!uploadErrorText &&
          <Alert severity="error">{uploadErrorText}</Alert>
        }
        <DialogContentText>
          <FormattedMessage id='migration.dialog.upload.description'/>
        </DialogContentText>          
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={handleClose} />
        <Button 
          onClick={() => inputFile.current?.click()}
          autoFocus
        >
          <FormattedMessage id='migration.upload'/>             
        </Button>
      </DialogActions>
    </Dialog>
  );
}