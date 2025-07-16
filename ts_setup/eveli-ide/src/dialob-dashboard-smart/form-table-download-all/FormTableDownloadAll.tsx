import { Table } from '@tanstack/react-table'
import { Box, Button, styled } from '@mui/material';
import FileDownloadIcon from '@mui/icons-material/FileDownload';
import { FormattedMessage } from 'react-intl';

import FileSaver from 'file-saver';

import { DashboardItem, useDialobForms } from '@/api-dialob-form';


const Root =  styled('div')(({ theme }) => ({
  width: '100%',
  padding: theme.spacing(1),
  gap: theme.spacing(1),
  display: 'flex',
  alignItems: 'left',
  flexDirection: 'column',
  '& .EveliTableDrawerSavedFilters-optionButtons': {
    display: 'flex',
    justifyContent: 'center',
    gap: theme.spacing(1),
  }
  
}));

export const FormTableDownloadAll: React.FC<{
  table: Table<DashboardItem>
}> = ({ table }) => {

  const { downloadAllForms } = useDialobForms();

  function handleDownloadAll() {
    const forms = table.getRowModel().rows.map(row => row.original);
    downloadAllForms(forms).then(({ blob, fileName }) => FileSaver.saveAs(blob, fileName));
  }

  return (
    <Root>
      <Button onClick={handleDownloadAll} startIcon={<FileDownloadIcon fontSize='small' />}>
        <FormattedMessage id='download.all' />
      </Button>
    </Root>
  );
}