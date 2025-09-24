import { DashboardItem, useDialobForms } from '@dxs-ts/eveli-api';
import { Box, IconButton, Tooltip } from '@mui/material';

import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import CloseIcon from '@mui/icons-material/Close';
import DownloadIcon from '@mui/icons-material/Download';

import { useIntl } from 'react-intl';
import FileSaver from 'file-saver';
import React from 'react';

import { DialogCopy } from '../dialog-copy';


export const FormTableToolbarRow: React.FC<{ value: DashboardItem }> = ({ value }) => {
  const intl = useIntl();
  const { downloadAllForms, deleteForm } = useDialobForms();
  const [isCopyOpen, setCopyOpen] = React.useState(false);

  function handleDownload(_event: React.MouseEvent) {
    downloadAllForms([ value ]).then(({ blob, fileName }) => FileSaver.saveAs(blob, fileName));
  }

  function handleDelete(_event: React.MouseEvent) {
    deleteForm({ form: value });
  }

  function handleCopyOpen(_event: React.MouseEvent) {
    setCopyOpen(true);
  }
  function handleCopyClose() {
    setCopyOpen(false);
  }

  return (
    <Box display='flex'>
      {isCopyOpen && <DialogCopy onClose={handleCopyClose} source={value} />}
      <Tooltip title={intl.formatMessage({ id: 'adminUI.table.tooltip.copy' })} placement='top-end' arrow>
        <IconButton size='small' onClick={handleCopyOpen}>
          <ContentCopyIcon fontSize='small'/>
        </IconButton>
      </Tooltip>
      
      <Tooltip title={intl.formatMessage({ id: 'adminUI.table.tooltip.delete' })} placement='top-end' arrow>
        <IconButton size='small' onClick={handleDelete} color='error'>
          <CloseIcon fontSize='small'/>
        </IconButton>
      </Tooltip>

      <Tooltip title={intl.formatMessage({ id: 'download' })} placement='top-end' arrow>
        <IconButton size='small' onClick={handleDownload}>
          <DownloadIcon fontSize='small'/>
        </IconButton>
      </Tooltip>
    </Box>
  );
}