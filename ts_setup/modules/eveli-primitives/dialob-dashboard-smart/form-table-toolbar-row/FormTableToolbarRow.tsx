import { DashboardItem, useDialobForms } from '@dxs-ts/eveli-api';
import { Box, IconButton, Tooltip } from '@mui/material';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import CloseIcon from '@mui/icons-material/Close';
import DownloadIcon from '@mui/icons-material/Download';
import { useIntl } from 'react-intl';
import FileSaver from 'file-saver';
import React from 'react';

import { DialogCopy } from '../dialog-copy';
import { DialogDelete } from '../dialog-delete';

export const FormTableToolbarRow: React.FC<{ value: DashboardItem }> = ({ value }) => {
  const intl = useIntl();
  const { downloadAllForms } = useDialobForms();
  const [isCopyOpen, setCopyOpen] = React.useState(false);
  const [isDeleteOpen, setDeleteOpen] = React.useState(false);

  function handleDownload(_event: React.MouseEvent) {
    (async () => {
      const { blob, fileName }: { blob: Blob; fileName: string } = await downloadAllForms([value]);
      FileSaver.saveAs(blob, fileName);
    })();
  }  

  function handleDeleteOpen(_event: React.MouseEvent) {
    setDeleteOpen(true);
  }
  function handleDeleteClose() {
    setDeleteOpen(false);
  }

  function handleCopyOpen(_event: React.MouseEvent) {
    setCopyOpen(true);
  }
  function handleCopyClose() {
    setCopyOpen(false);
  }

  return (
    <Box display="flex">
      {isCopyOpen && <DialogCopy onClose={handleCopyClose} source={value} />}
      {isDeleteOpen && <DialogDelete onClose={handleDeleteClose} source={value} />}
      <Tooltip title={intl.formatMessage({ id: 'adminUI.table.tooltip.copy' })} placement="top-end" arrow>
        <IconButton size="small" onClick={handleCopyOpen}>
          <ContentCopyIcon fontSize="small" />
        </IconButton>
      </Tooltip>

      <Tooltip title={intl.formatMessage({ id: 'adminUI.table.tooltip.delete' })} placement="top-end" arrow>
        <IconButton size="small" onClick={handleDeleteOpen} color="error">
          <CloseIcon fontSize="small" />
        </IconButton>
      </Tooltip>

      <Tooltip title={intl.formatMessage({ id: 'adminUI.table.tooltip.download' })} placement="top-end" arrow>
        <IconButton size="small" onClick={handleDownload}>
          <DownloadIcon fontSize="small" />
        </IconButton>
      </Tooltip>
    </Box>
  );
};
