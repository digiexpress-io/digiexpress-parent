import MaterialTable, { Column } from '@material-table/core';
import AddIcon from '@mui/icons-material/Add';
import SaveIcon from '@mui/icons-material/Save';
import moment from 'moment';
import React, { useContext, useRef, useState } from 'react';
import { FormattedDate, FormattedTime, useIntl } from 'react-intl';
import { useConfig } from '../../context/ConfigContext';
import { useFetch } from '../../hooks/useFetch';
import { useSnackbar } from 'notistack';

import { localizeTable } from '../../util/localizeTable';
import { downloadFile } from '../../util/downloadFile';
import { AssetRelease } from '../../types/AssetRelease';
import { NewAssetReleaseDialog } from './NewAssetReleaseDialog';
import { SessionRefreshContext } from '../../context/SessionRefreshContext';
import { handleErrors } from '../../util/cFetch';

interface TableState  {
  columns: Array<Column<AssetRelease>>;
}

export const AssetReleaseTable: React.FC = () => {
  const intl = useIntl();
  const config = useConfig();
  const apiUrl = config.wrenchApiUrl;
  const session = useContext(SessionRefreshContext);
  const { enqueueSnackbar } = useSnackbar();
  const tableLocalization = localizeTable((id: string) => intl.formatMessage({ id }));
  const tableRef = useRef();
  const { response:assetReleases, refresh:refreshAssetReleases } = useFetch<AssetRelease[]>(`${apiUrl}/releases/`);
  const [newDialogOpen, setNewDialogOpen] = useState(false);

 

  const formatDateTime = (time:any) => {
    if (time) {
      const localTime = moment.utc(time).local().toDate();
      return (
        <React.Fragment>
          <FormattedDate value={localTime} />&nbsp;<FormattedTime value={localTime} />
        </React.Fragment>
      )
    }
    return "-";
  }

  const getRelease = (releaseTag: AssetRelease) => {
    let url = `${apiUrl}/releaseDownload/${releaseTag.name}`;
    return session.cFetch(`${url}`,{
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    })
    .then(response=>handleErrors(response))
    .then((response:Response) => response.json())
    .then (json=>{
        downloadFile(JSON.stringify(json, undefined, 2), releaseTag.name + '.json', 'text/json');
    })
    .catch(error => {
      enqueueSnackbar(intl.formatMessage({id: 'assetRelease.downloadFailed'}, {cause: (error.message || 'N/A')}), {variant: 'error'});
    });
  }

  const tableState: TableState = {
    columns: [
      {
        title: intl.formatMessage({id: 'assetReleaseTableHeader.name'}),
        field: 'name',
        headerStyle: { fontWeight: 'bold' }
      },
      {
        title: intl.formatMessage({id: 'assetReleaseTableHeader.description'}),
        field: 'description',
        headerStyle: { fontWeight: 'bold' },
      },
      {
        title: intl.formatMessage({id: 'assetReleaseTableHeader.contentTag'}),
        field: 'contentTag',
        headerStyle: { fontWeight: 'bold' },
      },
      {
        title: intl.formatMessage({id: 'assetReleaseTableHeader.workflowTag'}),
        field: 'workflowTag',
        headerStyle: { fontWeight: 'bold' },
      },
      {
        title: intl.formatMessage({id: 'assetReleaseTableHeader.wrenchTag'}),
        field: 'wrenchTag',
        headerStyle: { fontWeight: 'bold' },
      },
      {
        title: intl.formatMessage({id: 'assetReleaseTableHeader.updated'}),
        field: 'created',
        filtering: false,
        type: 'date',
        defaultSort: 'desc',
        render: data => formatDateTime(data.created),
        headerStyle: { fontWeight: 'bold' }
      },
      {
        title: intl.formatMessage({id: 'assetReleaseTableHeader.updatedBy'}),
        field: 'user',
        headerStyle: { fontWeight: 'bold' }
      }
    ]
  };

  return (
      <>
      <MaterialTable
        title = {intl.formatMessage({id: 'assetReleaseTable.title'})}
        localization={tableLocalization}
        columns={tableState.columns}
        tableRef={tableRef}
        options={{
          actionsColumnIndex: -1,
          debounceInterval: 500,
          padding: 'dense',
          filtering: false,
          sorting: true,
          search: true,
          paging: false
        }}
        actions={[
          {
            icon: AddIcon,
            tooltip: intl.formatMessage({id: 'assetReleaseTable.addButton'}),
            isFreeAction: true,
            hidden: !config.modifiableAssets,
            onClick: () => {setNewDialogOpen(true);}
          },
          {
            icon: SaveIcon,
            tooltip: intl.formatMessage({id: 'assetReleaseTable.exportButton'}),
            onClick: (event, data) => {!Array.isArray(data) &&  getRelease(data)}
          }
        ]}
       
        isLoading={false}
        data={assetReleases||[]}
      />
      <NewAssetReleaseDialog open={newDialogOpen} setOpen={setNewDialogOpen} onSubmit={() => refreshAssetReleases()} /> 
      </>
  );
}
