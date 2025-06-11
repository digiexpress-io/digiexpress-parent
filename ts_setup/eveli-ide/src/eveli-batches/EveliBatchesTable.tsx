import React from 'react';

import { ColumnDef } from '@tanstack/react-table';

import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '@/api-task';

import { WithTableStyles } from '@/eveli-table';

import { Box, Typography, IconButton, Tooltip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useIntl, FormattedMessage } from 'react-intl';

import { useNavigate } from '@tanstack/react-router';



export const EveliBatchesTable: React.FC = () => {
  const intl = useIntl();
  const { findAll } = useFetch('worker/rest/api/tasks.GET', {})
  const [data, setData] = React.useState<TaskApi.Task[]>([]);
  const navigate = useNavigate();
  
  React.useEffect(() => {
    findAll().then(setData);
  }, []);

  const columns: ColumnDef<TaskApi.Task, any>[] = [
    {
      header: 'Info',
      accessorKey: 'additionalInfo',
      size: 100,
      minSize: 100,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
    },
    {
      header: 'Client',
      accessorKey: 'clientIdentificator',
      filterFn: 'includesString',
      size: 150,
      minSize: 150,
      enableColumnFilter: true,
      enableResizing: true,
    }
  ]

  return (
    <Box sx={{ display: 'inline-block' }}>
      <Box display="flex" alignItems="center" mb={2}>
        <Typography variant="h1" sx={{ flexGrow: 1 }}>
          <FormattedMessage id="batchesView.title" />
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

