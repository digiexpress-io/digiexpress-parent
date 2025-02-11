
import React, { useContext, useRef, useState } from 'react';
import AddIcon from '@mui/icons-material/Add';
import SaveIcon from '@mui/icons-material/Save';
import FileUploadIcon from '@mui/icons-material/FileUpload';
import MaterialTable, { Column } from '@material-table/core';

import { useIntl, FormattedMessage } from 'react-intl';
import { useSnackbar } from 'notistack';

import { localizeTable } from '../../util/localizeTable';
import { downloadFile } from '../../util/downloadFile';
import { handleErrors } from '../../util/cFetch';

import { SessionRefreshContext } from '../../context/SessionRefreshContext';
import { useConfig } from '../../context/ConfigContext';
import { useFetch } from '../../hooks/useFetch';

import { Publication } from '../../types/Publication';
import { NewPublicationDialog } from './NewPublicationDialog';


import { DateTimeFormatter } from '../../components/DateTimeFormatter';
import { TableHeader } from '../../components/TableHeader';
import { Box, IconButton, Tooltip, DialogContent, Dialog, DialogContentText, DialogActions, Button } from '@mui/material';
import { UploadPublicationDialog } from './UploadPublicationDialog';


interface TableState {
  columns: Array<Column<Publication>>;
}


const DeploymentInfo: React.FC<Publication> = ({description}) => {
  const [open, setOpen] = useState(false);

  function handleOpen() {
    setOpen(true)
  }

  function handleClose() {
    setOpen(false)
  }


  return (
  <>
    <Dialog open={open} fullWidth maxWidth='md'>
      <DialogContent>
        <DialogContentText sx={{whiteSpace: 'pre-wrap'}} variant='body2'>
          {description}
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}><FormattedMessage id='button.accept' /></Button>
      </DialogActions>
    </Dialog>
    <Box width='200px' height='46px' textOverflow='ellipsis' overflow='hidden' sx={{cursor: 'pointer'}} onClick={handleOpen}>
      {description}
    </Box>
  </>);
}

export const PublicationsTable: React.FC = () => {
  const intl = useIntl();
  const { serviceUrl } = useConfig();
  const config = useConfig();
  const session = useContext(SessionRefreshContext);
  const { enqueueSnackbar } = useSnackbar();
  const tableLocalization = localizeTable((id: string) => intl.formatMessage({ id }));
  const tableRef = useRef();
  const { response: assetReleases, refresh: refreshAssetReleases } = useFetch<Publication[]>(`${serviceUrl}worker/rest/api/assets/publications`);
  const [newDialogOpen, setNewDialogOpen] = useState(false);
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);

  const getRelease = (releaseTag: Publication) => {
    let url = `${serviceUrl}worker/rest/api/assets/deployments/${releaseTag.name}`;
    return session.cFetch(`${url}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    })
      .then(response => handleErrors(response))
      .then((response: Response) => response.json())
      .then(json => {
        downloadFile(JSON.stringify(json, undefined, 2), releaseTag.name + '.json', 'text/json');
      })
      .catch(error => {
        enqueueSnackbar(intl.formatMessage({ id: 'assetRelease.downloadFailed' }, { cause: (error.message || 'N/A') }), { variant: 'error' });
      });
  }

  const tableState: TableState = {
    columns: [
      {
        title: intl.formatMessage({ id: 'publicationsTableHeader.name' }),
        field: 'name',
        headerStyle: { fontWeight: 'bold' }
      },
      {
        title: intl.formatMessage({ id: 'publicationsTableHeader.description' }),
        field: 'description',
        headerStyle: { fontWeight: 'bold' },
        render: (data) => <DeploymentInfo {...data}/>,
      },
      {
        title: intl.formatMessage({ id: 'publicationsTableHeader.liveDate' }),
        field: 'startsAt',
        filtering: false,
        type: 'date',
        defaultSort: 'desc',
        render: data => <DateTimeFormatter value={data.startsAt} />,
        headerStyle: { fontWeight: 'bold' }
      },
      {
        title: intl.formatMessage({ id: 'publicationsTableHeader.created' }),
        field: 'body.created',
        filtering: false,
        type: 'date',
        defaultSort: 'desc',
        render: data => <DateTimeFormatter value={data.createdAt} />,
        headerStyle: { fontWeight: 'bold' }
      },
      {
        title: intl.formatMessage({ id: 'publicationsTableHeader.createdBy' }),
        field: 'body.user',
        headerStyle: { fontWeight: 'bold' }
      },
      {
        render: data => (
          <Box justifySelf='end'>
            <Tooltip title={intl.formatMessage({ id: 'publicationsTable.exportButton' })}>
              <IconButton onClick={() => { !Array.isArray(data) && getRelease(data) }}>
                <SaveIcon color='primary' />
              </IconButton>
            </Tooltip>
          </Box>
        )
      }
    ]
  };

  return (
    <>
      <MaterialTable
        title={<TableHeader id='publicationsTable.title' />}
        localization={tableLocalization}
        columns={tableState.columns}
        tableRef={tableRef}
        options={{
          actionsColumnIndex: -1,
          debounceInterval: 500,
          padding: 'dense',
          filtering: false,
          maxColumnSort: 1,
          search: true,
          paging: false
        }}
        actions={[
          {
            icon: AddIcon,
            tooltip: intl.formatMessage({ id: 'publicationsTable.addButton' }),
            isFreeAction: true,
            hidden: !config.modifiableAssets,
            onClick: () => { setNewDialogOpen(true); }
          },
          {
            icon: FileUploadIcon,
            tooltip: intl.formatMessage({ id: 'publicationsTable.uploadButton' }),
            isFreeAction: true,
            onClick: () => { setUploadDialogOpen(true); }
          }
        ]}

        isLoading={false}
        data={assetReleases || []}
      />
      <NewPublicationDialog open={newDialogOpen} setOpen={setNewDialogOpen} onSubmit={() => refreshAssetReleases()} />
      <UploadPublicationDialog open={uploadDialogOpen} setOpen={setUploadDialogOpen} onSubmit={() => refreshAssetReleases()} />
    </>
  );
}
