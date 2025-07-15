import React from 'react';
import { Box, IconButton, Tooltip, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import FileUploadIcon from '@mui/icons-material/FileUpload';
import FileDownloadIcon from '@mui/icons-material/FileDownload';

import { FormattedMessage } from 'react-intl';
import FileSaver from 'file-saver';

import { useDialobForms } from '@/api-dialob-form';
import { FileUploadButton } from './FileUploadButton';
import { DialogCreate } from '../dialog-create';



export const FormTableToolbar: React.FC<{}> = () => {
  const { uploadJsonForm, downloadAllForms, uploadCsvForm } = useDialobForms();
  
  const formJsonUploadRef = React.useRef<HTMLInputElement | null>(null);
  const formCsvUploadRef = React.useRef<HTMLInputElement | null>(null);

  const [createRef, setCreateRef] = React.useState<boolean>(false);
 
  function handleDownloadAll() {
    downloadAllForms().then(({ blob, fileName}) => FileSaver.saveAs(blob, fileName));
  }

  return (
    <Box display='flex' alignItems='center' mb={2}>
      <Typography variant='h1' sx={{ flexGrow: 1 }}>
        <FormattedMessage id='adminUI.dialog.heading' />
      </Typography>

      {createRef && <DialogCreate onClose={() => setCreateRef(false)} /> }
      <IconButton onClick={() => setCreateRef(true)}>
        <AddIcon fontSize='small'/>
      </IconButton>
      
      {/**
       * Upload FORM as JSON create/update operation
       */}
      <FileUploadButton accept='.json' uploadRef={formJsonUploadRef}  onChange={uploadJsonForm} />
      <Tooltip title={<FormattedMessage id='upload.json' />}>
        <IconButton onClick={() => formJsonUploadRef.current?.click()}>
          <FileUploadIcon fontSize='small' />
        </IconButton>
      </Tooltip>
      
      {/**
       * Upload FORM as CSV, creates always new form
       */}
      <FileUploadButton accept='.csv' uploadRef={formCsvUploadRef} onChange={uploadCsvForm} />
      <Tooltip title={<FormattedMessage id='upload.csv' />}>
        <IconButton onClick={() => formCsvUploadRef.current?.click()}>
          <FileUploadIcon fontSize='small' />
        </IconButton>
      </Tooltip>

      {/**
       * Download everything
       */}
      <Tooltip title={<FormattedMessage id='download.all' />}>
        <IconButton onClick={handleDownloadAll}>
          <FileDownloadIcon fontSize='small' />
        </IconButton>
      </Tooltip>
    </Box>)
}