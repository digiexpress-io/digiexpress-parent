
import React, { useContext, useRef, useState } from 'react';
import AddIcon from '@mui/icons-material/Add';
import SaveIcon from '@mui/icons-material/Save';
import MaterialTable, { Column } from '@material-table/core';

import { useIntl } from 'react-intl';
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
import { Box, IconButton, Tooltip } from '@mui/material';


interface TableState {
  columns: Array<Column<Publication>>;
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

  const getRelease = (releaseTag: Publication) => {
    let url = `${serviceUrl}worker/rest/api/assets/deployments/${releaseTag.body.name}`;
    return session.cFetch(`${url}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    })
      .then(response => handleErrors(response))
      .then((response: Response) => response.json())
      .then(json => {
        downloadFile(JSON.stringify(json, undefined, 2), releaseTag.body.name + '.json', 'text/json');
      })
      .catch(error => {
        enqueueSnackbar(intl.formatMessage({ id: 'assetRelease.downloadFailed' }, { cause: (error.message || 'N/A') }), { variant: 'error' });
      });
  }

  const tableState: TableState = {
    columns: [
      {
        title: intl.formatMessage({ id: 'publicationsTableHeader.name' }),
        field: 'body.name',
        headerStyle: { fontWeight: 'bold' }
      },
      {
        title: intl.formatMessage({ id: 'publicationsTableHeader.description' }),
        field: 'body.description',
        headerStyle: { fontWeight: 'bold' },
      },
      {
        title: intl.formatMessage({ id: 'publicationsTableHeader.contentTag' }),
        field: 'body.stencilTagName',
        headerStyle: { fontWeight: 'bold' },
      },
      {
        title: intl.formatMessage({ id: 'publicationsTableHeader.workflowTag' }),
        field: 'body.workflowTagName',
        headerStyle: { fontWeight: 'bold' },
      },
      {
        title: intl.formatMessage({ id: 'publicationsTableHeader.wrenchTag' }),
        field: 'body.wrenchTagName',
        headerStyle: { fontWeight: 'bold' },
      },
      {
        title: intl.formatMessage({ id: 'publicationsTableHeader.created' }),
        field: 'body.created',
        filtering: false,
        type: 'date',
        defaultSort: 'desc',
        render: data => <DateTimeFormatter value={data.body.created} />,
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
          }
        ]}

        isLoading={false}
        data={assetReleases || []}
      />
      <NewPublicationDialog open={newDialogOpen} setOpen={setNewDialogOpen} onSubmit={() => refreshAssetReleases()} />
    </>
  );
}
