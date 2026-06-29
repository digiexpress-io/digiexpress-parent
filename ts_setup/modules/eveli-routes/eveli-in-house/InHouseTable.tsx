import React from 'react';
import { IconButton, Tooltip, Typography } from '@mui/material';
import { ContentCopy as ContentCopyIcon } from '@mui/icons-material';
import { FormattedMessage, useIntl } from 'react-intl';
import { ColumnDef, sortingFns } from '@tanstack/react-table';
import { WithTableStyles } from '@dxs-ts/xui-table';
import { useFetch } from '@dxs-ts/envir-fetch';


export const InHouseTable: React.FC = () => {
  const intl = useIntl();
  const { inHouseWorkflows } = useFetch('worker/rest/api/tasks/in-house.GET', {});
  const link = window.location.pathname;

  const columns: ColumnDef<any, any>[] = [
    {
      header: intl.formatMessage({ id: 'inHouseTable.col.header.name' }),
      accessorKey: 'name',
      filterFn: 'includesString',
      sortingFn: sortingFns.alphanumeric,
      size: 200,
      minSize: 200,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({ id: 'inHouseTable.col.header.description' }),
      id: 'description',
      accessorFn: () => '',
      size: 300,
      minSize: 200,
      enableSorting: false,
      enableColumnFilter: false,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({ id: 'inHouseTable.col.header.link' }),
      id: 'link',
      accessorFn: (row) => link + '/' + encodeURIComponent(row.name),
      size: 300,
      minSize: 200,
      enableSorting: false,
      enableColumnFilter: false,
      enableResizing: true,
    },
    {
      header: '',
      id: 'copy',
      size: 60,
      minSize: 60,
      enableSorting: false,
      enableColumnFilter: false,
      enableResizing: false,
      cell: ({ row }) => (
        <Tooltip title={intl.formatMessage({ id: 'inHouseTable.col.copy.tooltip' })}>
          <IconButton size='small' onClick={() => navigator.clipboard.writeText(link + '/' + encodeURIComponent(row.original.name))}>
            <ContentCopyIcon fontSize='small' />
          </IconButton>
        </Tooltip>
      ),
    },
  ];

  return (
    <>
      <Typography variant='h1'>
        <FormattedMessage id='inHouseTable.title' />
      </Typography>
      <WithTableStyles columns={columns} data={inHouseWorkflows ?? []} options={{ initialPageSize: 30, tableId: 'in-house' }} />
    </>
  );
};
