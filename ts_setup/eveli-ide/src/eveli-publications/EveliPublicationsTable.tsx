import React, { useState } from 'react';
import { Box, IconButton, Tooltip, Typography, Stack, Button, Dialog, DialogContent, DialogContentText, DialogActions, DialogTitle } from '@mui/material';
import { useIntl, FormattedMessage } from 'react-intl';
import { ColumnDef, sortingFns } from '@tanstack/react-table';
import { WithTableStyles } from '@/eveli-table';

import AddIcon from '@mui/icons-material/Add';
import SaveIcon from '@mui/icons-material/Save';
import FileUploadIcon from '@mui/icons-material/FileUpload';
import CircleIcon from '@mui/icons-material/Circle';
import DeleteIcon from '@mui/icons-material/Delete';
import SendIcon from '@mui/icons-material/Send';

import { useFetch } from '@dxs-ts/eveli-fetch';
import { useConfig } from '../api-config';
import { PublicationApi } from '../api-publications';
import { EveliDateTimeFormatter } from '../eveli-datetime-formatter';
import { NewPublicationDialog } from './NewPublicationDialog';
import { UploadPublicationDialog } from './UploadPublicationDialog';
import { EveliPermissions } from '@/eveli-permissions';

const DeploymentInfo: React.FC<PublicationApi.Publication> = ({ description }) => {
  const [open, setOpen] = useState(false);
  return (
    <>
      <Dialog open={open} fullWidth maxWidth='md'>
        <DialogContent>
          <DialogContentText sx={{ whiteSpace: 'pre-wrap' }} variant='body2'>
            {description}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}><FormattedMessage id='button.accept' /></Button>
        </DialogActions>
      </Dialog>
      <Box width='200px' height='46px' textOverflow='ellipsis' overflow='hidden' sx={{ cursor: 'pointer' }} onClick={() => setOpen(true)}>
        {description}
      </Box>
    </>
  );
};

const parseErrors = (errors: any): any[] => errors?.map?.errors || [];

const PublicationStatus: React.FC<PublicationApi.Publication & { onSubmit: () => void }> = ({ status, id, onSubmit, external, errors }) => {
  const intl = useIntl();
  const [open, setOpen] = useState(false);
  const { saveDeployment } = useFetch('worker/rest/api/assets/deployments/$deploymentId.PUT', {});

  const handleSubmit = (newStatus: string) => {
    saveDeployment({ id, status: newStatus }, () => {
      setOpen(false);
      onSubmit();
    });
  };

  const color = status === 'DEPLOYED' ? 'success' : status === 'ERROR' ? 'error' : status === 'READY' ? 'primary' : 'warning';

  return (
    <>
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth='md' fullWidth>
        <DialogTitle fontWeight='bold'>{intl.formatMessage({ id: 'publications.changeStatus' })}</DialogTitle>
        <DialogContent>
          <Stack spacing={1}>
            {external ? (
              <>
                <span>{intl.formatMessage({ id: 'publications.external' })}</span>
                <span>{intl.formatMessage({ id: 'publications.currentStatus' })} {status}</span>
              </>
            ) : (
              <>
                <span>{intl.formatMessage({ id: 'publications.currentStatus' })} {status}</span>
                <Button disabled={status === 'READY'} variant='outlined' color='error' startIcon={<DeleteIcon />} onClick={() => handleSubmit('READY')}>
                  {intl.formatMessage({ id: 'publications.remove' })}
                </Button>
                <Button disabled={status === 'DEPLOYED'} variant='contained' endIcon={<SendIcon />} onClick={() => handleSubmit('DEPLOYED')}>
                  {intl.formatMessage({ id: 'publications.deploy' })}
                </Button>
              </>
            )}
            {parseErrors(errors).map((e, key) => (<div key={key}>{JSON.stringify(e)}</div>))}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)} variant='text'><FormattedMessage id='button.cancel' /></Button>
        </DialogActions>
      </Dialog>
      <IconButton onClick={() => setOpen(true)}><CircleIcon color={color} /></IconButton>
    </>
  );
};

export const PublicationsTable: React.FC = () => {
  const intl = useIntl();
  const config = useConfig();
  const { assetReleases, refreshAssetReleases, isLoading } = useFetch('worker/rest/api/assets/publications.GET', {});
  const { getRelease } = useFetch('worker/rest/api/assets/deployments/$deploymentId.GET', {});

  const [newDialogOpen, setNewDialogOpen] = useState(false);
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);

  const columns: ColumnDef<PublicationApi.Publication, any>[] = [
    {
      header: '',
      accessorKey: 'status',
      cell: info => <PublicationStatus {...info.row.original} onSubmit={refreshAssetReleases} />,
      size: 60,
    },
    {
      header: intl.formatMessage({ id: 'publicationsTableHeader.name' }),
      accessorKey: 'name',
      cell: info => info.getValue(),
    },
    {
      header: intl.formatMessage({ id: 'publicationsTableHeader.description' }),
      accessorKey: 'description',
      cell: info => <DeploymentInfo {...info.row.original} />,
    },
    {
      header: intl.formatMessage({ id: 'publicationsTableHeader.liveDate' }),
      accessorKey: 'startsAt',
      sortingFn: sortingFns.datetime,
      cell: info => <EveliDateTimeFormatter value={info.getValue()} />,
    },
    {
      header: intl.formatMessage({ id: 'publicationsTableHeader.created' }),
      accessorKey: 'createdAt',
      sortingFn: sortingFns.datetime,
      cell: info => <EveliDateTimeFormatter value={info.getValue()} />,
    },
    {
      header: intl.formatMessage({ id: 'publicationsTableHeader.createdBy' }),
      accessorKey: 'createdBy',
      cell: info => info.getValue(),
    },
    {
      header: '',
      accessorKey: 'id',
      cell: info => (
        <Box justifySelf='end'>
          <EveliPermissions id='EXPORT_EVELI_PUBLICATION'>
            <Tooltip title={intl.formatMessage({ id: 'publicationsTable.exportButton' })}>
              <IconButton onClick={() => getRelease(info.row.original)}>
                <SaveIcon color='primary' />
              </IconButton>
            </Tooltip>
          </EveliPermissions>
        </Box>
      ),
      size: 60,
    },
  ];

  return (
    <>
      <Box display='flex' justifyContent='space-between' alignItems='center' mb={2}>
        <Typography variant='h1'>
          <FormattedMessage id='publicationsTable.title' />
        </Typography>
        <Box display='flex' gap={1}>
          <EveliPermissions id='CREATE_EVELI_PUBLICATION'>
            <Tooltip title={intl.formatMessage({ id: 'publicationsTable.addButton' })}>
              <IconButton onClick={() => setNewDialogOpen(true)}>
                <AddIcon />
              </IconButton>
            </Tooltip>
          </EveliPermissions>
          <EveliPermissions id='CREATE_EVELI_PUBLICATION'>
            <Tooltip title={intl.formatMessage({ id: 'publicationsTable.uploadButton' })}>
              <IconButton onClick={() => setUploadDialogOpen(true)}>
                <FileUploadIcon />
              </IconButton>
            </Tooltip>
          </EveliPermissions>
        </Box>
      </Box>
      <WithTableStyles
        columns={columns}
        data={assetReleases || []}
        options={{ initialPageSize: 30 }}
      />
      <NewPublicationDialog open={newDialogOpen} setOpen={setNewDialogOpen} onSubmit={refreshAssetReleases} />
      <UploadPublicationDialog open={uploadDialogOpen} setOpen={setUploadDialogOpen} onSubmit={refreshAssetReleases} />
    </>
  );
};
