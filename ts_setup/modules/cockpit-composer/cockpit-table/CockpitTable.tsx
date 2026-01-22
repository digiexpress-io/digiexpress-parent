import React from 'react';
import { Box, Typography, IconButton, Tooltip } from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';

import { ColumnDef } from '@tanstack/react-table';
import { DateTime } from 'luxon';

import { useIntl } from 'react-intl';
import { useQuery } from '@tanstack/react-query';
import { WithTableStyles } from '@dxs-ts/xui-table';
import { CockpitApi, useCockpitsBackend } from '@dxs-ts/cockpit-api';
import { CockpitCreateDialog } from '../cockpit-create';
import { CockpitLink } from './CockpitLink';
import { CockpitStatusIndicator } from '../cockpit-status-indicator';



export const COCKPIT_TABLE_QUERY_KEY = 'find-all-cockpits';

export const CockpitTable: React.FC = () => {
  const intl = useIntl();
  const backend = useCockpitsBackend();
  const [createCockpitOpen, setCreateCockpitOpen] = React.useState(false);

  const { data, refetch } = useQuery({
    queryKey: [COCKPIT_TABLE_QUERY_KEY],
    queryFn: () => backend.persistence.findAllCockpits(),
    initialData: [],
  });

  const { data: activity } = useQuery({
    queryKey: [COCKPIT_TABLE_QUERY_KEY, 'activity'],
    queryFn: () => backend.persistence.findActivity(),
  });

  const columns: ColumnDef<CockpitApi.CockpitSummary, any>[] = [
    {
      header: intl.formatMessage({ id: 'cockpit.name' }),
      accessorKey: 'name',
      size: 200,
      minSize: 200,
      enableSorting: true,
      enableResizing: true,
      enableColumnFilter: true,
      cell: ({ row }) => (
        <CockpitLink name={row.original.name} id={row.original.id} />
      ),
    },
    {
      header: intl.formatMessage({ id: 'cockpit.description' }),
      accessorKey: 'description',
      size: 250,
      minSize: 250,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
    },
    {
      header: intl.formatMessage({ id: 'cockpitTable.col.header.status' }),
      accessorKey: 'active',
      size: 150,
      minSize: 150,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
      cell: ({ row }) => {
        const isActive = activity?.activeCockpitId === row.original.id;
        return (<CockpitStatusIndicator isActive={isActive} />);
      },
    }
  ]


  return (
    <>
      <Box display="flex" alignItems="center" mb={2}>
        <Typography variant="h1" sx={{ flexGrow: 1 }}>{intl.formatMessage({ id: 'cockpitsTable.title' })}</Typography>
        <Box display="flex" gap={1}>
          <Tooltip title={intl.formatMessage({ id: 'cockpitsTable.createButton' })}>
            <IconButton onClick={() => setCreateCockpitOpen(true)}>
              <AddIcon />
            </IconButton>
          </Tooltip>
        </Box>
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
      <CockpitCreateDialog
        open={createCockpitOpen}
        setOpen={setCreateCockpitOpen}
        onSubmit={refetch}
      />
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

