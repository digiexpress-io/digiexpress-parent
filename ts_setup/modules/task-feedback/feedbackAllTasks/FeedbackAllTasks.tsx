import React from 'react';
import { Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import { FeedbackApi, useFeedback } from '../api-feedback';
import { StatusIndicator } from '../status-indicator';

import { WithTableStyles } from '@dxs-ts/xui-table';
import { ColumnDef, sortingFns, type FilterFn } from '@tanstack/react-table';
import { DateTime } from 'luxon';

const normalizeYMD = (d: Date) => new Date(d.getFullYear(), d.getMonth(), d.getDate());

const dateFilterFn: FilterFn<any> = (row, columnId, filterValue: { date: Date | null; type: 'EQUAL' | 'LT' | 'GTE' }) => {
  const { date, type } = filterValue ?? {};
  if (!date) return true;

  const raw = row.getValue(columnId);
  if (!raw) return false;

  const cellDate = typeof raw === 'string' ? new Date(raw) : (raw as Date);
  if (isNaN(cellDate.getTime())) return false;

  const a = normalizeYMD(cellDate).getTime();
  const b = normalizeYMD(date).getTime();

  switch (type) {
    case 'EQUAL': return a === b;
    case 'LT':    return a < b;
    case 'GTE':   return a >= b;
    default:      return true;
  }
};

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
      meta: { enableSelection: false },
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
      meta: { enableSelection: false },
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
      meta: { enableSelection: false },
      cell: (info) => info.getValue(),
    },
    {
      header: intl.formatMessage({ id: 'feedback.updated' }),
      accessorKey: 'updatedOnDate',
      size: 200,
      minSize: 200,
      filterFn: dateFilterFn,
      sortingFn: sortingFns.datetime,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: { isDate: true, enableSelection: false },
      cell: (info) => {
        const dateTime = DateTime.fromJSDate(new Date(info.getValue()))
          .setLocale('fi')
          .toLocaleString(DateTime.DATE_SHORT);
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
