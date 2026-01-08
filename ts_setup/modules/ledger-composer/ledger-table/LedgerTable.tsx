import React from 'react';
import { Box, Typography } from '@mui/material';

import { ColumnDef, FilterFnOption, flexRender } from '@tanstack/react-table';


import { useIntl } from 'react-intl';
import { useQuery } from '@tanstack/react-query';
import { anyDateFilter, TableDateFilter, WithTableStyles } from '@dxs-ts/xui-table';
import { LedgerApi, useLedgerBackend } from '@dxs-ts/ledger-api';
import { FormatAnyDateTimeShort } from '@dxs-ts/xui-datetime';


import { filterContractRefOrSubjectFn } from './tableHelpers';
import { IndicatorSubject } from './IndicatorSubject';



export const LEDGER_TABLE_QUERY_KEY = 'find-all-ledger';

export const LedgerTable: React.FC = () => {
  const intl = useIntl();
  const backend = useLedgerBackend();

  const { data, error, refetch, isPending } = useQuery({
    queryKey: [LEDGER_TABLE_QUERY_KEY],
    queryFn: () => backend.persistence.findAllLedgers(),
    initialData: [],
  });

  const columns: ColumnDef<LedgerApi.LedgerSummary, any>[] = [
     {
      header: intl.formatMessage({ id: 'ledgerTable.col.header.ledgerNumber'}),
      accessorKey: 'ledgerNumber',
      cell: (ledger) => flexRender(IndicatorSubject, { ledger: ledger.row.original }),
      filterFn: filterContractRefOrSubjectFn,
      size: 150,
      minSize: 100,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
    },
    {
      header: intl.formatMessage({ id: 'ledgerTable.col.header.updatedAt' }),
      accessorKey: 'updatedAt',
      cell: (updatedAt) => flexRender(FormatAnyDateTimeShort, { value: updatedAt.getValue() }),
      filterFn: filterUpdated,
      size: 150,
      minSize: 100,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
      meta: { enableDate: true },
    },
    {
      header: intl.formatMessage({ id: 'ledgerTable.col.header.lastChangeType' }),
      accessorKey: 'currentBlackBook',
      cell: (ledger) => flexRender((props) => <>{props.value}</>, { value: ledger.getValue().bookType }),
      size: 150,
      minSize: 100,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
      meta: { enableDate: true },
    },
    {
      header: intl.formatMessage({ id: 'ledgerTable.col.header.netAddedToAccount' }),
      accessorKey: 'currentBlackBook',
      cell: (ledger) => flexRender((props) => <>{props.value}</>, { value: ledger.getValue().bookDeltaAmount }),
      size: 150,
      minSize: 100,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
      meta: { enableDate: true },
    },
    {
      header: intl.formatMessage({ id: 'ledgerTable.col.header.removedFromAccount' }),
      accessorKey: 'currentBlackBook',
      cell: (ledger) => flexRender((props) => <>{props.value}</>, { value: ledger.getValue().bookOutflowAmount }),
      size: 150,
      minSize: 100,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
      meta: { enableDate: true },
    }
  ]


  return (
    <>
      <Box display="flex" alignItems="center" mb={2}>
        <Typography variant="h1" sx={{ flexGrow: 1 }}>{intl.formatMessage({ id: 'ledgersTable.title' })}</Typography>
      </Box>
      <WithTableStyles 
        data={data} 
        columns={columns} 
        options={{ tableId: 'ledgers' }} 
        theme={{
          rowProps: { height: '50px' }
        }}
      />
    </>
  );
}

const filterUpdated: FilterFnOption<LedgerApi.LedgerSummary> = (row, _columnId: string, filterValue: TableDateFilter) => {
  const lastUpdated = row.original.updatedAt;
  return anyDateFilter(lastUpdated, filterValue);
}
