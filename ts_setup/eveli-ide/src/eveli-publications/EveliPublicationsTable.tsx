
import React, { useRef, useState } from 'react';
import AddIcon from '@mui/icons-material/Add';
import SaveIcon from '@mui/icons-material/Save';
import FileUploadIcon from '@mui/icons-material/FileUpload';
import CircleIcon from '@mui/icons-material/Circle';
import DeleteIcon from '@mui/icons-material/Delete';
import SendIcon from '@mui/icons-material/Send';
import MaterialTable, { Column } from '@material-table/core';
import { Box, IconButton, Tooltip, DialogContent, Dialog, DialogContentText, DialogActions, Button, DialogTitle, Stack, Typography } from '@mui/material';

import { useIntl, FormattedMessage } from 'react-intl';
import { useFetch } from '@dxs-ts/eveli-fetch';

import { useConfig } from '../api-config';
import { PublicationApi } from '../api-publications';
import { useMaterialTableLabels } from '../api-mui-table';
import { EveliDateTimeFormatter } from '../eveli-datetime-formatter';

import { NewPublicationDialog } from './NewPublicationDialog';
import { UploadPublicationDialog } from './UploadPublicationDialog';
import { EveliPermissions } from '@/eveli-permissions';



interface TableState {
  columns: Array<Column<PublicationApi.Publication>>;
}

const DeploymentInfo: React.FC<PublicationApi.Publication> = ({ description }) => {
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
          <DialogContentText sx={{ whiteSpace: 'pre-wrap' }} variant='body2'>
            {description}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}><FormattedMessage id='button.accept' /></Button>
        </DialogActions>
      </Dialog>
      <Box width='200px' height='46px' textOverflow='ellipsis' overflow='hidden' sx={{ cursor: 'pointer' }} onClick={handleOpen}>
        {description}
      </Box>
    </>);
}



function parseErrors(errors: any): any[] {
  return errors?.map?.errors || [];
}

const PublicationStatus: React.FC<PublicationApi.Publication & { onSubmit: () => void }> = ({ status, id, onSubmit, external, errors }) => {
  const intl = useIntl();
  const [statusDialogOpen, setStatusDialogOpen] = useState(false);
  const { saveDeployment } = useFetch('worker/rest/api/assets/deployments/$deploymentId.PUT', {})

  const handleClose = () => {
    setStatusDialogOpen(false);
  }
  const handleOpen = () => {
    setStatusDialogOpen(true);
  }

  const handleDeploy = () => {
    handleSubmit('DEPLOYED');
  }
  const handleUnDeployed = () => {
    handleSubmit('READY');
  }

  const handleSubmit = (status: string): void => {
    saveDeployment({ id, status }, () => {
      setStatusDialogOpen(false);
      onSubmit();
    });
  }

  let color: 'success' | 'error' | 'primary' | 'warning';
  if (status == 'DEPLOYED') {
    color = 'success';
  } else if (status == 'ERROR') {
    color = 'error';
  } else if (status == 'READY') {
    color = 'primary';
  } else {
    color = 'warning';
  }



  return (<div>
    <Dialog open={statusDialogOpen} onClose={handleClose} maxWidth='md' fullWidth>
      <DialogTitle fontWeight='bold'>{intl.formatMessage({ id: 'publications.changeStatus' })}</DialogTitle>
      <DialogContent>
        <Stack spacing={1}>
          {external === true ?
            (<>
              <span>{intl.formatMessage({ id: 'publications.external' })}</span>
              <span>{intl.formatMessage({ id: 'publications.currentStatus' })} {status}</span>
            </>) : (<>
              <span>{intl.formatMessage({ id: 'publications.currentStatus' })} {status}</span>
              <Button disabled={status === 'READY'} variant='outlined' color='error' startIcon={<DeleteIcon />} onClick={handleUnDeployed}>{intl.formatMessage({ id: 'publications.remove' })}</Button>
              <Button disabled={status === 'DEPLOYED'} variant='contained' endIcon={<SendIcon />} onClick={handleDeploy}>{intl.formatMessage({ id: 'publications.deploy' })}</Button>
            </>)}

          {parseErrors(errors).map((e, key) => (<div key={key}>{JSON.stringify(e)}</div>))}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} variant='text'><FormattedMessage id='button.cancel' /></Button>
      </DialogActions>
    </Dialog>
    <IconButton onClick={handleOpen}><CircleIcon color={color} /></IconButton>
  </div>);
}

const AddPublicationAction: React.FC = () => {

  return (
    <EveliPermissions id='CREATE_EVELI_PUBLICATION'>
      <div style={{ cursor: 'pointer' }}>
        <AddIcon />
      </div>
    </EveliPermissions>
  );
};

const UploadPublicationAction: React.FC = () => {

  return (
    <EveliPermissions id='CREATE_EVELI_PUBLICATION'>
      <div style={{ cursor: 'pointer' }}>
        <FileUploadIcon />
      </div>
    </EveliPermissions>
  );
};


export const PublicationsTable: React.FC = () => {
  const intl = useIntl();
  const config = useConfig();
  const tableLocalization = useMaterialTableLabels();
  const tableRef = useRef();
  const { assetReleases, refreshAssetReleases, isLoading } = useFetch('worker/rest/api/assets/publications.GET', {});
  const { getRelease } = useFetch('worker/rest/api/assets/deployments/$deploymentId.GET', {});

  const [newDialogOpen, setNewDialogOpen] = useState(false);
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);


  const tableState: TableState = {
    columns: [
      {
        render: data => <PublicationStatus {...data} onSubmit={() => refreshAssetReleases()} />,
      },
      {
        title: intl.formatMessage({ id: 'publicationsTableHeader.name' }),
        field: 'name',
        headerStyle: { fontWeight: 'bold' }
      },
      {
        title: intl.formatMessage({ id: 'publicationsTableHeader.description' }),
        field: 'description',
        headerStyle: { fontWeight: 'bold' },
        render: (data) => <DeploymentInfo {...data} />,
      },
      {
        title: intl.formatMessage({ id: 'publicationsTableHeader.liveDate' }),
        field: 'startsAt',
        filtering: false,
        type: 'date',
        defaultSort: 'desc',
        render: data => <EveliDateTimeFormatter value={data.startsAt} />,
        headerStyle: { fontWeight: 'bold' }
      },
      {
        title: intl.formatMessage({ id: 'publicationsTableHeader.created' }),
        field: 'createdAt',
        filtering: false,
        type: 'date',
        defaultSort: 'desc',
        render: data => <EveliDateTimeFormatter value={data.createdAt} />,
        headerStyle: { fontWeight: 'bold' }
      },
      {
        title: intl.formatMessage({ id: 'publicationsTableHeader.createdBy' }),
        field: 'createdBy',
        headerStyle: { fontWeight: 'bold' }
      },
      {
        render: data => (
          <Box justifySelf='end'>
            <EveliPermissions id='EXPORT_EVELI_PUBLICATION'>
              <Tooltip title={intl.formatMessage({ id: 'publicationsTable.exportButton' })}>
                <IconButton onClick={() => { !Array.isArray(data) && getRelease(data) }}>
                  <SaveIcon color='primary' />
                </IconButton>
              </Tooltip>
            </EveliPermissions>
          </Box>
        )
      }
    ]
  };

  return (
    <>
      <MaterialTable
        title={
          <Typography variant='h1'>
            <FormattedMessage id='publicationsTable.title' />
          </Typography>
        }
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
            icon: AddPublicationAction as any,
            tooltip: intl.formatMessage({ id: 'publicationsTable.addButton' }),
            isFreeAction: true,
            hidden: !config.modifiableAssets,
            onClick: () => { setNewDialogOpen(true); }
          },
          {
            icon: UploadPublicationAction as any,
            tooltip: intl.formatMessage({ id: 'publicationsTable.uploadButton' }),
            isFreeAction: true,
            onClick: () => { setUploadDialogOpen(true); }
          }
        ]}

        isLoading={isLoading}
        data={assetReleases || []}
      />
      <NewPublicationDialog open={newDialogOpen} setOpen={setNewDialogOpen} onSubmit={() => refreshAssetReleases()} />
      <UploadPublicationDialog open={uploadDialogOpen} setOpen={setUploadDialogOpen} onSubmit={() => refreshAssetReleases()} />
    </>
  );
}
