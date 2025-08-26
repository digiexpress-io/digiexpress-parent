import React from 'react';
import { Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import { FeedbackApi, useFeedback } from '../api-feedback';
import { StatusIndicator } from '../status-indicator';

import { WithTableStyles } from '@dxs-ts/xui-table';
import { ColumnDef, sortingFns } from '@tanstack/react-table';
import { DateTime } from 'luxon';

export interface FeedbackAllTasksProps {
  onOpenFeedback(feedbackId: string): void;
}

export const FeedbackAllTasks: React.FC<FeedbackAllTasksProps> = ({ onOpenFeedback }) => {
  const intl = useIntl();
  const { findAllFeedback } = useFeedback();
  const [data, setData] = React.useState<FeedbackApi.Feedback[]>([]);

  React.useEffect(() => {
    findAllFeedback().then(setData);
  }, []);

  const columns: ColumnDef<any, any>[] = [
    {
      header: '',
      accessorKey: 'status',
      size: 50,
      minSize: 50,
      enableSorting: false,
      enableColumnFilter: false,
      enableResizing: false,
      cell: (info) => <StatusIndicator size='LARGE' taskId={info.row.original.sourceId} />,
    },
    {
      header: intl.formatMessage({ id: 'feedback.taskReferenceId.short' }),
      accessorKey: 'sourceId',
      size: 150,
      minSize: 150,
      filterFn: 'includesString',
      sortingFn: sortingFns.alphanumeric,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (info) => (
        <Box
          sx={{ cursor: 'pointer', color: 'primary.main', textDecoration: 'underline' }}
          onClick={() => onOpenFeedback(info.row.original.sourceId)}
        >
          {info.getValue()}
        </Box>
      ),
    },
    {
      header: intl.formatMessage({ id: 'feedback.mainCategory' }),
      accessorKey: 'content.main',
      size: 200,
      minSize: 200,
      filterFn: 'includesString',
      sortingFn: sortingFns.alphanumeric,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: { enableSelection: true },
      cell: (info) => info.getValue(),
    },
    {
      header: intl.formatMessage({ id: 'feedback.subCategory' }),
      accessorKey: 'content.sub',
      size: 200,
      minSize: 200,
      filterFn: 'includesString',
      sortingFn: sortingFns.alphanumeric,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: { enableSelection: true },
      cell: (info) => info.getValue(),
    },
    {
      header: intl.formatMessage({ id: 'feedback.createdBy' }),
      accessorKey: 'createdBy',
      size: 200,
      minSize: 200,
      filterFn: 'includesString',
      sortingFn: sortingFns.alphanumeric,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: { enableSelection: true },
      cell: (info) => info.getValue(),
    },
    {
      header: intl.formatMessage({ id: 'feedback.updatedBy' }),
      accessorKey: 'updatedBy',
      size: 200,
      minSize: 200,
      filterFn: 'includesString',
      sortingFn: sortingFns.alphanumeric,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: { enableSelection: true },
      cell: (info) => info.getValue(),
    },
    {
      header: intl.formatMessage({ id: 'feedback.updated' }),
      accessorKey: 'updatedOnDate',
      size: 200,
      minSize: 200,
      filterFn: 'includesString',
      sortingFn: sortingFns.datetime,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: { enableSelection: true },
      cell: (info) => {
        const dateTime = DateTime.fromJSDate(new Date(info.getValue()))
          .setLocale("fi")
          .toLocaleString(DateTime.DATE_SHORT)
        return dateTime;
      },
    },
  ];

  return (
    <Box>
      <Box sx={{ display: 'inline-block' }}>
        <Box display='flex' alignItems='center' mb={2}>
          <Typography variant='h1' sx={{ flexGrow: 1 }}>
            {intl.formatMessage({ id: 'feedback.all' })}
          </Typography>
        </Box>

        <WithTableStyles
          columns={columns}
          data={data}
          options={{
            initialPageSize: 15,
            tableId: 'feedback'
          }}
        />
      </Box>
    </Box>
  );
};
