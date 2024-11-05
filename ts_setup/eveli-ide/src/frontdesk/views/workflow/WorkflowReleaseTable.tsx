import React, { useRef, useState } from 'react';

import MaterialTable, { Column } from '@material-table/core';
import AddIcon from '@mui/icons-material/Add';
import SaveIcon from '@mui/icons-material/Save';
import PreviewIcon from '@mui/icons-material/Preview';

import { localizeTable } from '../../util/localizeTable';
import { useIntl } from 'react-intl';

import { NewWorkflowTagDialog } from './NewWorkflowTagDialog';
import { ViewWorkflowTagDialog } from './ViewWorkflowTagDialog';
import { WorkflowRelease } from '../../types/WorkflowRelease';

import { downloadFile } from '../../util/downloadFile';
import { DateTimeFormatter } from '../../components/DateTimeFormatter';
import { useConfig } from '../../context/ConfigContext';
import { useFetch } from '../../hooks/useFetch';
import { Typography } from '@mui/material';

interface TableState {
  columns: Array<Column<WorkflowRelease>>;
}

export const WorkflowReleaseTable: React.FC = () => {
  const intl = useIntl();
  const { serviceUrl, modifiableAssets } = useConfig();

  const tableLocalization = localizeTable((id: string) => intl.formatMessage({ id }));
  const tableRef = useRef();
  const { response: workflows, refresh: refreshWorkflowReleases } = useFetch<WorkflowRelease[]>(`${serviceUrl}rest/api/assets/workflows/tags`);
  const [newDialogOpen, setNewDialogOpen] = useState(false);
  const [tagDialogOpen, setTagDialogOpen] = useState(false);
  const [workflowRelease, setWorkflowRelease] = useState<WorkflowRelease | null>(null);

  const tableState: TableState = {
    columns: [
      {
        title: intl.formatMessage({ id: 'workflowReleaseTableHeader.name' }),
        field: 'body.name',
        headerStyle: { fontWeight: 'bold' }
      },
      {
        title: intl.formatMessage({ id: 'workflowReleaseTableHeader.description' }),
        field: 'body.description',
        headerStyle: { fontWeight: 'bold' },
      },
      {
        title: intl.formatMessage({ id: 'workflowReleaseTableHeader.updated' }),
        field: 'body.updated',
        filtering: false,
        type: 'date',
        defaultSort: 'desc',
        render: data => <DateTimeFormatter value={data.body.created} />,
        headerStyle: { fontWeight: 'bold' }
      },
      {
        title: intl.formatMessage({ id: 'workflowReleaseTableHeader.updatedBy' }),
        field: 'body.user',
        headerStyle: { fontWeight: 'bold' }
      }
    ]
  };


  return (
    <>
      <MaterialTable
        title={<Typography variant='h1'>{intl.formatMessage({ id: 'workflowReleaseTable.title' })}</Typography>}
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
            tooltip: intl.formatMessage({ id: 'workflowReleaseTable.addButton' }),
            isFreeAction: true,
            hidden: !modifiableAssets,
            onClick: () => { setWorkflowRelease(null); setNewDialogOpen(true); }
          },
          {
            icon: PreviewIcon,
            tooltip: intl.formatMessage({ id: 'workflowReleaseTable.viewButton' }),
            onClick: (event, data) => { setWorkflowRelease(data as WorkflowRelease); setTagDialogOpen(true) }
          },
          {
            icon: SaveIcon,
            tooltip: intl.formatMessage({ id: 'workflowReleaseTable.exportButton' }),
            onClick: (event, data) => { !Array.isArray(data) && downloadFile(JSON.stringify(data, undefined, 2), data.body.name + '.json', 'text/json') }
          }
        ]}

        isLoading={false}
        data={workflows || []}
      />
      <NewWorkflowTagDialog open={newDialogOpen} setOpen={setNewDialogOpen} onSubmit={() => refreshWorkflowReleases()} />
      {workflowRelease && <ViewWorkflowTagDialog open={tagDialogOpen} workflowRelease={workflowRelease} setOpen={setTagDialogOpen} />}
    </>
  );
}
