import React from 'react';
import { Box, Typography } from '@mui/material';

import { ColumnDef, FilterFnOption, flexRender } from '@tanstack/react-table';
import { DateTime } from 'luxon';

import { useIntl } from 'react-intl';
import { useQuery } from '@tanstack/react-query';
import { anyDateFilter, TableDateFilter, WithTableStyles } from '@dxs-ts/xui-table';
import { ContractApi, useContractBackend } from '@dxs-ts/contract-api';



import { IndicatorStatus } from './IndicatorStatus';
import { filterStringOrArrayFn, filterTaskRefOrSubjectFn, taskSortingFn } from './tableHelpers';


export const CONTRACT_TABLE_QUERY_KEY = 'find-all-contract';

export const TaskTable: React.FC = () => {
  const intl = useIntl();
  const backend = useContractBackend();

  const { data, error, refetch, isPending } = useQuery({
    queryKey: [CONTRACT_TABLE_QUERY_KEY],
    queryFn: () => backend.persistence.findAllContracts(),
    initialData: [],
  });

  
  const columns: ColumnDef<ContractApi.Contract, any>[] = [
    {
      header: intl.formatMessage({ id: 'taskTable.col.header.addInfo', defaultMessage: 'Info' }),
      accessorKey: 'additionalInfo',
      size: 100,
      minSize: 100,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
    },
    {
      header: intl.formatMessage({ id: 'taskTable.col.header.client', defaultMessage: 'Client' }),
      accessorKey: 'clientIdentificator',
      filterFn: 'includesString',
      size: 150,
      minSize: 150,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({ id: 'taskTable.col.header.status', defaultMessage: 'Status' }),
      accessorKey: 'statusIntl',
      filterFn: filterStringOrArrayFn,
      size: 150,
      minSize: 150,
      cell: (cell) => flexRender(IndicatorStatus, { status: cell.row.original.status! }),
      sortingFn: taskSortingFn,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      }
    },
    {
      header: intl.formatMessage({ id: 'taskTable.col.header.due', defaultMessage: 'Due' }),
      accessorKey: 'dueDate',
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: { isDate: true },
      filterFn: filterDueDate,
      cell: (dueDate) => flexRender(AnyDateTimeShort, { value: dueDate.getValue() })
    },
    {
      header: intl.formatMessage({ id: 'taskTable.col.header.created', defaultMessage: 'Created' }),
      accessorKey: 'created',
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: { isDate: true },
      filterFn: filterCreated,
      cell: (created) => flexRender(AnyDateTimeShort, { value: created.getValue() })
    }
  ]


  return (
    <>
      <Box display="flex" alignItems="center" mb={2}>
        <Typography variant="h1" sx={{ flexGrow: 1 }}>{intl.formatMessage({ id: 'taskTable.title', defaultMessage: 'Tasks' })}</Typography>
      </Box>
      <WithTableStyles data={data} columns={columns} options={{ tableId: 'tasks' }} />
    </>
  );
}


const AnyDateTimeShort: React.FC<{ value: any }> = ({ value }) => {
  const rawDate = value;
  if (!rawDate) {
    return <div>--</div>
  }
  const dateTime = DateTime.fromISO(rawDate).setLocale('fi');
  const formatted = dateTime.toLocaleString(DateTime.DATE_SHORT);

  return <div>{formatted}</div>;
}

const filterDueDate: FilterFnOption<ContractApi.Contract> = (row, _columnId: string, filterValue: TableDateFilter) => {
  const latestTagDate = row.original.;
  return anyDateFilter(latestTagDate, filterValue);
}

const filterCreated: FilterFnOption<ContractApi.Contract> = (row, _columnId: string, filterValue: TableDateFilter) => {
  const lastSaved = row.original.created;
  return anyDateFilter(lastSaved, filterValue);
}