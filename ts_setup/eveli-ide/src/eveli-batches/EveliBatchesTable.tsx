import React from 'react';
import { ColumnDef, flexRender } from '@tanstack/react-table';
import { Link as RouterLink } from '@tanstack/react-router'
import { useFetch } from '@dxs-ts/eveli-fetch';
import { WithTableStyles } from '@/eveli-table';
import { Box, Typography, IconButton, Tooltip, LinkProps, Link } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useIntl, FormattedMessage } from 'react-intl';

import { useNavigate } from '@tanstack/react-router';
import { BatchApi } from '@/api-batch';
import { BatchHealthBall } from '../eveli-batches-health-ball';
import { DateTime } from 'luxon';



export const EveliBatchesTable: React.FC = () => {
  const intl = useIntl();
  const { findAll } = useFetch('worker/rest/api/batches.GET', {})
  const [data, setData] = React.useState<BatchApi.Batch[]>([]);
  const navigate = useNavigate();
  
  React.useEffect(() => {
    findAll().then(setData);
  }, []);

  const columns: ColumnDef<BatchApi.Batch, any>[] = [
    {
      header: '',
      accessorKey: 'health',
      size: 50,
      minSize: 50,
      enableSorting: false,
      enableResizing: true,
      cell: (data) => <BatchHealthBall batch={data.row.original} />
    },
    {
      header: 'Batch Name',
      accessorKey: 'batchName',
      size: 200,
      minSize: 300,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
      meta: {
        enableSelection: true
      },
      cell: (created) => flexRender(BatchLink, { value: created.row.original })
    },
    {
      header: 'Last run',
      filterFn: 'includesString',
      accessorFn: (data) => data.transitives?.instances[0]?.createdAt,
      size: 170,
      minSize: 170,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (created) => flexRender(AnyDateTimeShort, { value: created.getValue() })
    },
    {
      header: 'Comment',
      accessorKey: 'comment',
      filterFn: 'includesString',
      size: 550,
      minSize: 150,
      enableColumnFilter: true,
      enableResizing: true,
    },

  ]

  return (
    <Box sx={{ display: 'inline-block' }}>
      <Box display="flex" alignItems="center" mb={2}>
        <Typography variant="h1" sx={{ flexGrow: 1 }}>
          <FormattedMessage id="batchesView.title" defaultMessage="Batches" />
        </Typography>

          <Tooltip title={intl.formatMessage({ id: 'taskButton.addInstance' })}>
            <IconButton
              onClick={() => {
                navigate({
                  from: '/secured/$locale',
                  to: '/secured/$locale/worker/batches/create',
                });
              }}
            >
              <AddIcon />
            </IconButton>
          </Tooltip>
      </Box>
  
      <WithTableStyles data={data} columns={columns} options={{ tableId: 'batches'}}/>
    </Box>
  );  
}

const AnyDateTimeShort: React.FC<{ value: any }> = ({ value }) => {
  const rawDate = value;
  if (!rawDate) {
    return <div>--</div>
  }
  const dateTime = DateTime.fromISO(rawDate).setLocale('fi');
  const formatted = dateTime.toLocaleString(DateTime.DATETIME_SHORT);

  return <div>{formatted}</div>;
}

const BatchLink: React.FC<{ value: BatchApi.Batch }> = ({ value }) => {

  if (!value.transitives || value.transitives?.instances.length === 0) {
    return (<>{value.batchName}</>)
  }

  return (
    <Box
      sx={{
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        minWidth: 0,
      }}
    >
      <Link
        href="#"
        component={LinkOverride}
        batchId={value.id}
      >
        {value.batchName}
      </Link>
    </Box>
  );
};

const LinkOverride = React.forwardRef<any, LinkProps & { batchId?: string }>((itemProps, ref) => {
  const { batchId } = itemProps;
  return (<RouterLink
    ref={ref}
    from='/secured/$locale/worker'
    to='/secured/$locale/worker/batches/$batchId'
    params={{ batchId: `${batchId!}` }}
    children={itemProps.children}
  />)
})