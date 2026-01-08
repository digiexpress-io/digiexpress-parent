import React from 'react';
import { Box, Typography, LinkProps, Link } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { DateTimeFormatter } from '@dxs-ts/xui-datetime';
import { ColumnDef, flexRender } from '@tanstack/react-table';
import { Link as RouterLink } from '@tanstack/react-router'
import { useFetch } from '@dxs-ts/envir-fetch';
import { BatchApi } from '@dxs-ts/eveli-api';
import { WithTableStyles } from '@dxs-ts/xui-table';
import { BatchHealthBall } from '../eveli-batches-health-ball';




export const EveliBatchesTable: React.FC = () => {
  const { findAll } = useFetch('worker/rest/api/batches.GET', {})
  const [data, setData] = React.useState<BatchApi.Batch[]>([]);
  
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
      </Box>
      <WithTableStyles data={data} columns={columns} options={{ tableId: 'batches'}}/>
    </Box>
  );  
}

const AnyDateTimeShort: React.FC<{ value: any }> = ({ value }) => {
  return <DateTimeFormatter value={value} />;
};

const BatchLink: React.FC<{ value: BatchApi.Batch }> = ({ value }) => {

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