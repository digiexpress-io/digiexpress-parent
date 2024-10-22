import MaterialTable, { Column } from '@material-table/core';
import moment from 'moment';
import React, { useRef, useState } from 'react';
import { FormattedDate, FormattedTime, useIntl } from 'react-intl';
import { useConfig } from '../../context/ConfigContext';
import { useFetch } from '../../hooks/useFetch';

import { localizeTable } from '../../util/localizeTable';
import { NewWorkflowRelease } from './NewWorkflowRelease';
import { WorkflowRelease } from '../../types/WorkflowRelease';
import { downloadFile } from '../../util/downloadFile';
import { WorkflowTagDialog } from './WorkflowTagDialog';
import Visibility from '@mui/icons-material/Visibility';

interface TableState  {
  columns: Array<Column<WorkflowRelease>>;
}

export const WorkflowReleaseTable: React.FC = () => {
  const intl = useIntl();
  const config = useConfig();
  const apiUrl = config.wrenchApiUrl;
  const tableLocalization = localizeTable((id: string) => intl.formatMessage({ id }));
  const tableRef = useRef();
  const { response:workflows, refresh:refreshWorkflowReleases } = useFetch<WorkflowRelease[]>(`${apiUrl}/workflowReleases/`);
  const [newDialogOpen, setNewDialogOpen] = useState(false);
  const [tagDialogOpen, setTagDialogOpen] = useState(false);
  const [workflowRelease, setWorkflowRelease] = useState<WorkflowRelease|null>(null);

 

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

  const tableState: TableState = {
    columns: [
      {
        title: intl.formatMessage({id: 'workflowReleaseTableHeader.name'}),
        field: 'name',
        headerStyle: { fontWeight: 'bold' }
      },
      {
        title: intl.formatMessage({id: 'workflowReleaseTableHeader.description'}),
        field: 'description',
        headerStyle: { fontWeight: 'bold' },
      },
      {
        title: intl.formatMessage({id: 'workflowReleaseTableHeader.updated'}),
        field: 'created',
        filtering: false,
        type: 'date',
        defaultSort: 'desc',
        render: data => formatDateTime(data.created),
        headerStyle: { fontWeight: 'bold' }
      },
      {
        title: intl.formatMessage({id: 'workflowReleaseTableHeader.updatedBy'}),
        field: 'user',
        headerStyle: { fontWeight: 'bold' }
      }
    ]
  };

  return (
      <>
      <MaterialTable
        title = {intl.formatMessage({id: 'workflowReleaseTable.title'})}
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
            icon: 'add',
            tooltip: intl.formatMessage({id: 'workflowReleaseTable.addButton'}),
            isFreeAction: true,
            hidden: !config.modifiableAssets,
            onClick: () => {setWorkflowRelease(null);setNewDialogOpen(true);}
          },
          {
            icon: ()=>(<Visibility/>),
            tooltip: intl.formatMessage({id: 'workflowReleaseTable.viewButton'}),
            onClick: (event, data) => {setWorkflowRelease(data as WorkflowRelease);setTagDialogOpen(true)}
          },
          {
            icon: 'save_alt',
            tooltip: intl.formatMessage({id: 'workflowReleaseTable.exportButton'}),
            onClick: (event, data) => {!Array.isArray(data) &&  downloadFile(JSON.stringify(data, undefined, 2), data.name + '.json', 'text/json')}
          }
        ]}
       
        isLoading={false}
        data={workflows||[]}
      />
      <NewWorkflowRelease open={newDialogOpen} setOpen={setNewDialogOpen} workflowRelease={workflowRelease} onSubmit={() => refreshWorkflowReleases()} /> 
      {workflowRelease &&
        <WorkflowTagDialog open={tagDialogOpen} workflowRelease={workflowRelease} setOpen={setTagDialogOpen}/>
      }
      </>
  );
}
