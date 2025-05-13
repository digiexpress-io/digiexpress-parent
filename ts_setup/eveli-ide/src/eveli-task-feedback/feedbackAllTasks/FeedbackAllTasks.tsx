import React from 'react';
import { Box, FormControl, MenuItem, Select, SelectChangeEvent, Typography, useTheme } from '@mui/material';
import { useIntl } from 'react-intl';
import { useNavigate } from '@tanstack/react-router';

import * as Burger from '@/eveli-styles';
import { useFeedback } from '../../api-feedback';
import { StatusIndicator } from '../status-indicator';
import { FeedbackReducer } from './FeedbackReducer';

import { WithTableStyles } from '@/eveli-table';
import { ColumnDef, sortingFns } from '@tanstack/react-table';

export interface FeedbackAllTasksProps { }

export const FeedbackAllTasks: React.FC<FeedbackAllTasksProps> = () => {
  const intl = useIntl();
  const navigate = useNavigate();
  const theme = useTheme();
  const { findAllFeedback } = useFeedback();
  const [state, setState] = React.useState(new FeedbackReducer({ data: [] }));

  React.useEffect(() => {
    findAllFeedback().then(data => setState(prev => prev.withData(data)));
  }, []);

  function handleSearch(searchString: string) {
    setState(prev => prev.withSearchBy(searchString))
  }

  function handleChangeCategory(event: SelectChangeEvent<string>) {
    const { value } = event.target;
    setState(prev => prev.withFilterByCategory(value))
  }

  function handleChangeSubCategory(event: SelectChangeEvent<string>) {
    const { value } = event.target;
    setState(prev => prev.withFilterBySubCategory(value))
  }

  function handleFeedbackNav(feedbackId: string) {
    navigate({
      from: '/secured/$locale',
      params: { feedbackId },
      to: '/secured/$locale/worker/feedback/$feedbackId'
    });
  }

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
      header: intl.formatMessage({ id: 'feedback.taskReferenceId' }),
      accessorKey: 'sourceId',
      size: 250,
      minSize: 250,
      filterFn: 'includesString',
      sortingFn: sortingFns.alphanumeric,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (info) => (
        <Box
          sx={{ cursor: 'pointer', color: 'primary.main', textDecoration: 'underline' }}
          onClick={() => handleFeedbackNav(info.row.original.sourceId)}
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
      cell: (info) => info.getValue(),
    },
  ];

  return (
    <Box >
      <Box sx={{ display: 'inline-block' }}>
        <Box display='flex' alignItems='center' mb={2}>
          <Typography variant='h1' sx={{ flexGrow: 1 }}>
            {intl.formatMessage({ id: 'feedback.all' })}
          </Typography>
        </Box>

        <Box display='flex' mb={3} gap={1} alignItems='end' width="100%">
          <Box flex="1">
            <Burger.TextField
              label='feedback.search'
              onChange={handleSearch}
              value={state.searchBy ?? ''}
              placeholder={intl.formatMessage({ id: 'feedback.search.placeholder' })}
            />
          </Box>

          <Box flex="2" display='flex' gap={2}>
            <FormControl sx={{ flex: 1 }}>
              <Typography fontWeight='bold'>{intl.formatMessage({ id: 'feedback.search.filter.category' })}</Typography>
              <Select
                sx={{ padding: 0 }}
                value={state.filterByCategory ?? ''}
                onChange={handleChangeCategory}
                fullWidth
              >
                <MenuItem value=''>{intl.formatMessage({ id: 'feedback.filter.selectNone' })}</MenuItem>
                {state.categories.map((name) => (
                  <MenuItem key={name} value={name}>
                    {name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <FormControl sx={{ flex: 1 }}>
              <Typography fontWeight='bold'>{intl.formatMessage({ id: 'feedback.search.filter.subCategory' })}</Typography>
              <Select
                sx={{ padding: 0 }}
                value={state.filterBySubCategory ?? ''}
                onChange={handleChangeSubCategory}
                fullWidth
              >
                <MenuItem value=''>{intl.formatMessage({ id: 'feedback.filter.selectNone' })}</MenuItem>
                {state.subcategories.map((name) => (
                  <MenuItem key={name} value={name}>
                    {name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>
        </Box>

        <WithTableStyles
          columns={columns}
          data={state.visibleRows}
          options={{
            initialPageSize: 15
          }}
        />
      </Box>
    </Box>

  );
};
