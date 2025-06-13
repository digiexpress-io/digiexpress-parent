import React from 'react';
import { ColumnDef } from '@tanstack/react-table';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { WithTableStyles } from '@/eveli-table';
import { Box, Typography, IconButton, Tooltip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useIntl, FormattedMessage } from 'react-intl';

import { useNavigate } from '@tanstack/react-router';
import { BatchApi } from '@/api-batch';
import { BatchHealthBall } from './BatchHealthBall';



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
      cell: () => <BatchHealthBall health="COMPLETED_SUCCESS" />
    },
    {
      header: 'Batch Name',
      accessorKey: 'batchName',
      size: 150,
      minSize: 100,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
      meta: {
        enableSelection: true
      }
    },
    {
      header: 'Status',
      accessorKey: 'status',
      filterFn: 'includesString',
      size: 150,
      minSize: 150,
      enableColumnFilter: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      }
    },
    {
      header: 'Doc type',
      accessorKey: 'docType',
      filterFn: 'includesString',
      size: 150,
      minSize: 150,
      enableColumnFilter: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      }
    },
    {
      header: 'Comment',
      accessorKey: 'comment',
      filterFn: 'includesString',
      size: 250,
      minSize: 150,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: 'Updated',
      accessorKey: 'updatedAt',
      filterFn: 'includesString',
      size: 150,
      minSize: 150,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: 'Updated by',
      accessorKey: 'updatedBy',
      filterFn: 'includesString',
      size: 150,
      minSize: 150,
      enableColumnFilter: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      }
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

