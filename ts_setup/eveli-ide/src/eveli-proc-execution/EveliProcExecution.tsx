import React from 'react';
import { Typography } from '@mui/material';
import { FormattedDate, FormattedMessage, FormattedTime, useIntl } from 'react-intl';
import { useFetch } from '@dxs-ts/eveli-fetch';

import { ProcExecutionApi } from '../api-proc-execution';
import { WithTableStyles } from '@/eveli-table';
import { ColumnDef, sortingFns } from '@tanstack/react-table';




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
  const { findLast6Months } = useFetch('worker/rest/api/processes.GET', {})
  const [data, setData] = React.useState<ProcExecutionApi.ProcessExecution[]>([]);

  React.useEffect(() => {
    findLast6Months().then(setData);
  }, []);


  const columns: ColumnDef<ProcExecutionApi.ProcessExecution, any>[] = [
    {
      header: intl.formatMessage({ id: 'processTableHeader.workflow' }),
      accessorKey: 'workflowName',
      filterFn: 'includesString',
      sortingFn: sortingFns.alphanumeric,
      size: 200,
      minSize: 200,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (workflowName) => workflowName.getValue(),
    },
    {
      header: intl.formatMessage({ id: 'processTableHeader.questionnaireId' }),
      accessorKey: 'questionnaireId',
      filterFn: 'includesString',
      sortingFn: sortingFns.alphanumeric,
      size: 300,
      minSize: 300,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (questionnaireId) => questionnaireId.getValue(),
    },
    {
      header: intl.formatMessage({ id: 'processTableHeader.status' }),
      accessorKey: 'status',
      filterFn: 'includesString',
      sortingFn: sortingFns.alphanumeric,
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (status) => intl.formatMessage({ id: 'process.status.' + status.getValue() }),
    },
    {
      header: intl.formatMessage({ id: 'processTableHeader.created' }),
      accessorKey: 'created',
      filterFn: 'includesString',
      sortingFn: sortingFns.datetime,
      size: 200,
      minSize: 200,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (info) => formatDate(info.getValue())
    },
  ]
  return (
  <>
    <Typography variant='h1'>
      <FormattedMessage id='processTable.title'/>
    </Typography>
    <WithTableStyles columns={columns} data={data} options={{ initialPageSize: 30, tableId: 'process-execution' }}/>
  </>);
}
