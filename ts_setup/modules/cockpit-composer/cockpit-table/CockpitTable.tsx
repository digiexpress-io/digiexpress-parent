import React from 'react';
import { Box, Typography } from '@mui/material';

import { ColumnDef } from '@tanstack/react-table';
import { DateTime } from 'luxon';

import { useIntl } from 'react-intl';
import { useQuery } from '@tanstack/react-query';
import { WithTableStyles } from '@dxs-ts/xui-table';
import { CockpitApi, useCockpitsBackend } from '@dxs-ts/cockpit-api';



export const COCKPIT_TABLE_QUERY_KEY = 'find-all-cockpits';

export const CockpitTable: React.FC = () => {
  const intl = useIntl();
  const backend = useCockpitsBackend();

  const { data, error, refetch, isPending } = useQuery({
    queryKey: [COCKPIT_TABLE_QUERY_KEY],
    queryFn: () => backend.persistence.findAllCockpits(),
    initialData: [],
  });

  console.log("data", data)

  const columns: ColumnDef<CockpitApi.CockpitContainer, any>[] = [
    {
      header: intl.formatMessage({ id: 'cockpitTable.col.header.id' }),
      accessorKey: 'id',
      size: 150,
      minSize: 150,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
    },
    {
      header: intl.formatMessage({ id: 'cockpitTable.col.header.name' }),
      accessorKey: 'name',
      size: 200,
      minSize: 200,
      enableSorting: true,
      enableResizing: true,
      enableColumnFilter: true,
    },
    {
      header: intl.formatMessage({ id: 'cockpitTable.col.header.description' }),
      accessorKey: 'description',
      size: 250,
      minSize: 250,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
    }
  ]


  return (
    <>
      <Box display="flex" alignItems="center" mb={2}>
        <Typography variant="h1" sx={{ flexGrow: 1 }}>{intl.formatMessage({ id: 'cockpitsTable.title' })}</Typography>
      </Box>
      {data.length === 0 ? <>empty</> :
        <WithTableStyles
          data={data}
          columns={columns}
          options={{ tableId: 'cockpits' }}
          theme={{
            rowProps: { height: '50px' }
          }}
        />}
    </>
  );
}


const AnyDateTimeShort: React.FC<{ value: any }> = ({ value }) => {
  const rawDate = value;
  if (!rawDate) {
    return <div>--</div>
  }
  if ((typeof rawDate)) {

  }

  const isJSDate = (typeof rawDate) === 'object';

  const dateTime = (isJSDate ? DateTime.fromJSDate(rawDate) : DateTime.fromISO(rawDate)).setLocale('fi');
  const formatted = dateTime.toLocaleString(DateTime.DATE_SHORT);

  return <div>{formatted}</div>;
}

