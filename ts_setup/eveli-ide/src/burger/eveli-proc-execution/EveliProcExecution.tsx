import React from 'react';
import { Typography } from '@mui/material';
import MaterialTable, { Column } from '@material-table/core';
import { FormattedMessage, useIntl } from 'react-intl';
import { useFetch } from '@dxs-ts/eveli-fetch';

import { ProcExecutionApi } from '../api-proc-execution';
import { useMaterialTableLabels } from '../api-locale';



interface TableState {
  columns: Array<Column<ProcExecutionApi.ProcessExecution>>;
}

export const EveliProcExecution: React.FC = () => {
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
    <MaterialTable title={
      <Typography variant='h1'>
        <FormattedMessage id='processTable.title'/>
      </Typography>
      }
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
