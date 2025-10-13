import React from 'react';
import { Badge, Box, IconButton, Typography } from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import { FileUpload as FileUploadIcon } from '@mui/icons-material';

import { FormattedMessage } from 'react-intl';

import { useDialobForms } from '@dxs-ts/eveli-api';
import { FileUploadButton } from './FileUploadButton';
import { DialogCreate } from '../dialog-create';
import { IconButtonWithText } from './IconWithText';


export const FormTableToolbar: React.FC<{}> = () => {
  const { uploadJsonForm, uploadCsvForm } = useDialobForms();

  const formJsonUploadRef = React.useRef<HTMLInputElement | null>(null);
  const formCsvUploadRef = React.useRef<HTMLInputElement | null>(null);

  const [createRef, setCreateRef] = React.useState<boolean>(false);

  return (
    <Box display='flex' alignItems='center' mb={2}>
      <Typography variant='h1' sx={{ flexGrow: 1 }}>
        <FormattedMessage id='adminUI.dialog.heading' />
      </Typography>

      {createRef && <DialogCreate onClose={() => setCreateRef(false)} />}
      <IconButton onClick={() => setCreateRef(true)}>
        <AddIcon fontSize='small' />
      </IconButton>

      {/**
       * Upload FORM as JSON create/update operation
       */}
      <FileUploadButton accept='.json' uploadRef={formJsonUploadRef} onChange={uploadJsonForm} />

      <IconButtonWithText onClick={() => formJsonUploadRef.current?.click()}>
        <Badge
          badgeContent={<FormattedMessage id='upload.json' />}
          anchorOrigin={{ vertical: 'top', horizontal: 'right' }}>
          <FileUploadIcon fontSize='small' />
        </Badge>
      </IconButtonWithText>

      {/**
       * Upload FORM as CSV, creates always new form
       */}
      <FileUploadButton accept='.csv' uploadRef={formCsvUploadRef} onChange={uploadCsvForm} />
      <IconButtonWithText onClick={() => formCsvUploadRef.current?.click()}>
        <Badge
          badgeContent={<FormattedMessage id='upload.csv' />}
          anchorOrigin={{ vertical: 'top', horizontal: 'right' }}>
          <FileUploadIcon fontSize='small' />
        </Badge>
      </IconButtonWithText>


    </Box>)
}