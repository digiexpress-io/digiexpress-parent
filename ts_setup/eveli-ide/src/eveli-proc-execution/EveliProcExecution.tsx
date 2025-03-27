import React from 'react';
import { Typography } from '@mui/material';
import MaterialTable, { Column } from '@material-table/core';
import { FormattedDate, FormattedMessage, FormattedTime, useIntl } from 'react-intl';
import { useFetch } from '@dxs-ts/eveli-fetch';

import { ProcExecutionApi } from '../api-proc-execution';
import { useMaterialTableLabels } from '../api-mui-table';
import moment from 'moment';



interface TableState {
  columns: Array<Column<ProcExecutionApi.ProcessExecution>>;
}

const formatDate = (time: any) => {
  if (time) {
    const localTime = new Date(time);
    return (
      <React.Fragment>
        <FormattedDate value={localTime} /> - <FormattedTime value={localTime} />
      </React.Fragment>
    )
  }
  return "-";
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
        field: 'questionnaireId',
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
        render: data => formatDate(data.created),
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
