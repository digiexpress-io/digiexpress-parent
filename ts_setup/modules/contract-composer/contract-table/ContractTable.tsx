import React from 'react';
import { Box, Typography } from '@mui/material';

import { ColumnDef, FilterFnOption, flexRender } from '@tanstack/react-table';
import { DateTime } from 'luxon';

import { useIntl } from 'react-intl';
import { useQuery } from '@tanstack/react-query';
import { anyDateFilter, TableDateFilter, WithTableStyles } from '@dxs-ts/xui-table';
import { ContractApi, useContractBackend } from '@dxs-ts/contract-api';

import { IndicatorStatus } from './IndicatorStatus';
import { filterStringOrArrayFn, filterContractRefOrSubjectFn, taskSortingFn } from './tableHelpers';
import { IndicatorSubject } from './IndicatorSubject';
import { Policyholder } from './Policyholder';


export const CONTRACT_TABLE_QUERY_KEY = 'find-all-contract';

export const ContractTable: React.FC = () => {
  const intl = useIntl();
  const backend = useContractBackend();

  const { data, error, refetch, isPending } = useQuery({
    queryKey: [CONTRACT_TABLE_QUERY_KEY],
    queryFn: () => backend.persistence.findAllContracts(),
    initialData: [],
  });

  const columns: ColumnDef<ContractApi.ContractSummary, any>[] = [
     {
      header: intl.formatMessage({ id: 'contractTable.col.header.contractNumber'}),
      accessorKey: 'contractNumber',
      cell: (contract) => flexRender(IndicatorSubject, { contract: contract.row.original }),
      filterFn: filterContractRefOrSubjectFn,
      size: 100,
      minSize: 100,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
    },
    {
      header: intl.formatMessage({ id: 'contractTable.col.header.contractType'}),
      accessorKey: 'contractType',
      size: 120,
      minSize: 120,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
    },
    {
      header: intl.formatMessage({ id: 'contractTable.col.header.policyholder'}),
      accessorKey: 'policyholder.externalId',
      cell: (cell) => flexRender(Policyholder, { contract: cell.row.original }),
      size: 150,
      minSize: 150,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
    },
    {
      header: intl.formatMessage({ id: 'contractTable.col.header.status' }),
      accessorKey: 'contractStatusIntl',
      filterFn: filterStringOrArrayFn,
      size: 150,
      minSize: 150,
      cell: (cell) => flexRender(IndicatorStatus, { status: cell.row.original.contractStatus }),
      sortingFn: taskSortingFn,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      }
    },

    {
      header: intl.formatMessage({ id: 'contractTable.col.header.created'}),
      accessorKey: 'createdAt',
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: { isDate: true },
      filterFn: filterCreated,
      cell: (created) => flexRender(AnyDateTimeShort, { value: created.getValue() })
    },
    {
      header: intl.formatMessage({ id: 'contractTable.col.header.maturityDate'}),
      accessorKey: 'contractMaturityDate',
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: { isDate: true },
      filterFn: filterMaturityDate,
      cell: (dueDate) => flexRender(AnyDateTimeShort, { value: dueDate.getValue() })
    },
  ]


  return (
    <>
      <Box display="flex" alignItems="center" mb={2}>
        <Typography variant="h1" sx={{ flexGrow: 1 }}>{intl.formatMessage({ id: 'contractsTable.title' })}</Typography>
      </Box>
      <WithTableStyles 
        data={data} 
        columns={columns} 
        options={{ tableId: 'contracts' }} 
        theme={{
          rowProps: { height: '50px' }
        }}
      />
    </>
  );
}


const AnyDateTimeShort: React.FC<{ value: any }> = ({ value }) => {
  const rawDate = value;
  if (!rawDate) {
    return <div>--</div>
  }
  if((typeof rawDate)) {

  }

  const isJSDate = (typeof rawDate) === 'object';

  const dateTime = (isJSDate ? DateTime.fromJSDate(rawDate) : DateTime.fromISO(rawDate)).setLocale('fi');
  const formatted = dateTime.toLocaleString(DateTime.DATE_SHORT);

  return <div>{formatted}</div>;
}

const filterMaturityDate: FilterFnOption<ContractApi.ContractSummary> = (row, _columnId: string, filterValue: TableDateFilter) => {
  const latestTagDate = row.original.contractMaturityDate;
  return anyDateFilter(latestTagDate, filterValue);
}

const filterCreated: FilterFnOption<ContractApi.ContractSummary> = (row, _columnId: string, filterValue: TableDateFilter) => {
  const lastSaved = row.original.createdAt;
  return anyDateFilter(lastSaved, filterValue);
}