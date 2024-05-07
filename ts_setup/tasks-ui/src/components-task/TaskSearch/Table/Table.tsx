import React from 'react';
import {
  TableHead as MTableHead,
  TableCell as MTableCell,
  TableContainer as MTableContainer,
  Table as MTable,
  TableRow as MTableRow,
  Box,
  Paper, TableBody, Typography, useTheme
} from '@mui/material';

import { FormattedMessage } from 'react-intl';

import { TableFillerRows } from 'components-generic';
import { useTasks } from 'descriptor-task';
import { GroupByTypes, useTaskPrefs, ColumnName, TaskPagination } from '../TableContext';

import { useTitle } from './TableTitle';

import { TablePagination } from './TablePagination';
import { TableHeader } from './TableHeader';
import { TableRow } from './TableRow';




interface DelegateProps {
  groupByType: GroupByTypes,
  groupId: string,
  content: TaskPagination,
  setContent: React.Dispatch<React.SetStateAction<TaskPagination>>
}

export const TaskTable: React.FC<DelegateProps> = ({ groupByType, groupId, content, setContent }) => {

  const { loading } = useTasks();
  const prefCtx = useTaskPrefs();
  const title = useTitle({ classifierValue: groupId, groupByType });
  const { pref } = prefCtx;
  const theme = useTheme();
  const radius = theme.shape.borderRadius;


  const columns: ColumnName[] = React.useMemo(() => {
    return pref.visibility.filter(v => v.enabled).map(v => v.dataId as ColumnName);
  }, [pref]);


  return (<Paper sx={{ mx: 1 }}>
    <Box sx={{
      width: '100%', 
      height: 3, 
      backgroundColor: title.color,
      borderTopLeftRadius: radius,
      borderTopRightRadius: radius,
    }} />
    <Box sx={{ pl: 1, py: 2 }}>
      <Typography variant='h5'>
        <FormattedMessage id={'taskSearch.filter.groupedByTitle'} values={{ type: title.title }} />
      </Typography>
    </Box>

    <MTableContainer>
      <MTable size='small'>
        <MTableHead>
          <MTableRow>

            {columns.map((id) => (
              <TableHeader key={id} name={id as ColumnName} sortable setContent={setContent} content={content} classifierValue={groupId}>
                <FormattedMessage id={`tasktable.header.${id}`} />
              </TableHeader>
            ))}

            {/* menu column */}
            {columns.length > 0 && <MTableCell />}
          </MTableRow>
        </MTableHead>


        <TableBody>
          {content.entries.map((row, rowId) => (<TableRow key={row.id} rowId={rowId} row={row} columns={columns} />))}
          <TableFillerRows content={content} loading={loading} plusColSpan={7} />
        </TableBody>

      </MTable>
    </MTableContainer>

    <Box display='flex'>
      <Box alignSelf="center" flexGrow={1}></Box>
      <TablePagination state={content} setState={setContent} />
    </Box>
  </Paper>);
}