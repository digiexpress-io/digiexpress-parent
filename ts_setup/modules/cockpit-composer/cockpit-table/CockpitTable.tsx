import React from 'react';
import { Box, Typography } from '@mui/material';

import { ColumnDef, FilterFnOption, flexRender } from '@tanstack/react-table';
import { DateTime } from 'luxon';

import { useIntl } from 'react-intl';
import { anyDateFilter, TableDateFilter, WithTableStyles } from '@dxs-ts/xui-table';
import { CockpitApi } from '@dxs-ts/cockpit-api';



export const COCKPIT_TABLE_QUERY_KEY = 'find-all-cockpits';

export const CockpitTable: React.FC = () => {
  const intl = useIntl();

  const columns: ColumnDef<CockpitApi.CockpitSummary, any>[] = [
     {
      header: intl.formatMessage({ id: 'contractTable.col.header.contractNumber'}),
      accessorKey: 'contractNumber',
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
      size: 150,
      minSize: 150,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
    },
    {
      header: intl.formatMessage({ id: 'contractTable.col.header.status' }),
      accessorKey: 'contractStatusIntl',
      size: 150,
      minSize: 150,
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
        <Typography variant="h1" sx={{ flexGrow: 1 }}>{intl.formatMessage({ id: 'cockpitsTable.title' })}</Typography>
      </Box>
      <WithTableStyles 
        data={[]} 
        columns={columns} 
        options={{ tableId: 'cockpits' }} 
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

const filterMaturityDate: FilterFnOption<CockpitApi.CockpitSummary> = (row, _columnId: string, filterValue: TableDateFilter) => {
  const latestTagDate = row.original.contractMaturityDate;
  return anyDateFilter(latestTagDate, filterValue);
}

const filterCreated: FilterFnOption<CockpitApi.CockpitSummary> = (row, _columnId: string, filterValue: TableDateFilter) => {
  const lastSaved = row.original.createdAt;
  return anyDateFilter(lastSaved, filterValue);
}