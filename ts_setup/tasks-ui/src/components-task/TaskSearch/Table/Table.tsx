import React from 'react';
import { 
  TableHead as MTableHead, 
  TableCell as MTableCell,
  TableContainer as MTableContainer, 
  Table as MTable, 
  TableRow as MTableRow,
  Box
} from '@mui/material';

import { FormattedMessage } from 'react-intl';

import { TableBody, TableFillerRows } from 'components-generic';
import { useTasks } from 'descriptor-task';
import { GroupByTypes, useTaskPrefs, ColumnName, TaskPagination } from '../TableContext';

import { Title } from './TableTitle';

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
  const { pref } = prefCtx;

  const columns: ColumnName[] = React.useMemo(() => {
    return pref.visibility.filter(v => v.enabled).map(v => v.dataId as ColumnName);
  }, [pref]);

  return (<>
    <MTableContainer>
      <MTable size='small'>
        <MTableHead>
          <MTableRow>

            { /* reserved title column */}
            <TableHeader sortable={false} name='title' setContent={setContent} content={content} classifierValue={groupId}>
              <Title groupByType={groupByType} classifierValue={groupId} groupCount={content.entries.length}/>
            </TableHeader>


            { columns.filter(c => c !== 'title').map((id) => (
              <TableHeader key={id} name={id as ColumnName} sortable setContent={setContent} content={content} classifierValue={groupId}>  
                <FormattedMessage id={`tasktable.header.${id}`} />
              </TableHeader>
            ))}

            {/* menu column */}
            {columns.length > 0 && <MTableCell />}
          </MTableRow>
        </MTableHead>


        <TableBody>
          {content.entries.map((row, rowId) => (<TableRow key={row.id} rowId={rowId} row={row} columns={columns}/>))}
          <TableFillerRows content={content} loading={loading} plusColSpan={7} />
        </TableBody>

      </MTable>
    </MTableContainer>

    <Box display='flex' sx={{ paddingLeft: 1, marginTop: -2 }}>
      <Box alignSelf="center" flexGrow={1}></Box>
      <TablePagination state={content} setState={setContent}/>
    </Box>
  </>);
  }