import React from 'react';
import MaterialTable, { Column } from '@material-table/core';
import { useIntl } from 'react-intl';

import { useFetch } from '@dxs-ts/eveli-fetch';

import { Process } from '../../types/Process';

import { TableHeader } from '../../components/TableHeader';
import { useMaterialTableLabels } from '@/burger';


interface TableState {
  columns: Array<Column<Process>>;
}

export const ProcessTable: React.FC = () => {
  const intl = useIntl();
  const { loadProcesses } = useFetch('worker/rest/api/processes.GET', {})

  const tableLocalization = useMaterialTableLabels();

  const tableState: TableState = {
    columns: [
      {
        title: intl.formatMessage({ id: 'processTableHeader.workflow' }),
        field: 'workflowName',
        headerStyle: { fontWeight: 'bold' },
      },
      {
        title: intl.formatMessage({ id: 'processTableHeader.questionnaireId' }),
        field: 'questionnaire',
        filtering: false,
        headerStyle: { fontWeight: 'bold' },
      },
      {
        title: intl.formatMessage({ id: 'processTableHeader.status' }),
        field: 'status',
        headerStyle: { fontWeight: 'bold' },
        lookup: {
          'ANSWERED': intl.formatMessage({ id: 'process.status.ANSWERED' }),
          'CREATED': intl.formatMessage({ id: 'process.status.CREATED' }),
          'ANSWERING': intl.formatMessage({ id: 'process.status.ANSWERING' }),
          'IN_PROGRESS': intl.formatMessage({ id: 'process.status.IN_PROGRESS' }),
          'WAITING': intl.formatMessage({ id: 'process.status.WAITING' }),
          'COMPLETED': intl.formatMessage({ id: 'process.status.COMPLETED' }),
          'REJECTED': intl.formatMessage({ id: 'process.status.REJECTED' }),
        },
      },
      {
        title: intl.formatMessage({ id: 'processTableHeader.created' }),
        field: 'created',
        filtering: false,
        headerStyle: { fontWeight: 'bold' },
      }
    ]
  };


  return (
    <MaterialTable
      title={<TableHeader id='processTable.title' />}
      localization={tableLocalization}
      columns={tableState.columns}
      options={{
        actionsColumnIndex: -1,
        debounceInterval: 500,
        padding: 'dense',
        filtering: true,
        paging: true,
        pageSize: 20
      }}


      isLoading={false}
      data={query => loadProcesses(query, tableState.columns)
      }
    />
  );
}
