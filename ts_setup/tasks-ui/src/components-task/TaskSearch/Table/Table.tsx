import React from 'react';
import { 
  TableHead as MTableHead, 
  TableCell as MTableCell,
  TableContainer as MTableContainer, 
  Table as MTable, 
  TablePagination as MPagination,
  TableRow as MTableRow,
  TableSortLabel,
  Box
} from '@mui/material';

import { SxProps } from '@mui/system';

import { visuallyHidden } from '@mui/utils';
import { FormattedMessage } from 'react-intl';

import Table from 'table';
import Context from 'context';

import { TableBody, TableFillerRows } from 'components-generic';
import { cyan_mud } from 'components-colors';

import { PreferenceContextType } from 'descriptor-prefs';
import { TaskDescriptor } from 'descriptor-task';
import { useGrouping, GroupByTypes, useTaskPrefs, ColumnName } from '../TableContext';

import { Title } from './TableTitle';
import CellMenu from '../TableCells/CellMenu';
import CellTitle from '../TableCells/CellTitle';
import CellAssignees from '../TableCells/CellAssignees';
import CellDueDate from '../TableCells/CellDueDate';
import CellPriority from '../TableCells/CellPriority';
import CellStatus from '../TableCells/CellStatus';
import CellRoles from '../TableCells/CellRoles';


import LoggerFactory from 'logger';

const _logger = LoggerFactory.getLogger();



type TaskPagination = Table.TablePagination<TaskDescriptor>;
type SetTaskPagination = React.Dispatch<React.SetStateAction<TaskPagination>>;



function getPrefSortGroup(classifierValue: string) {
  return classifierValue + ".";
}
function getPrefSortId(classifierValue: string, column: ColumnName) {
  return getPrefSortGroup(classifierValue) + column;
}

function getRowBackgroundColor(index: number): SxProps {
  const isOdd = index % 2 === 1;

  if (isOdd) {
    return { backgroundColor: cyan_mud };
  }
  return { backgroundColor: 'background.paper' };
}

function initTable(classifierValue: string, prefCtx: PreferenceContextType): TaskPagination {
  const prefGroup = getPrefSortGroup(classifierValue);
  const storedPref = prefCtx.pref.sorting.find(({ dataId }) => dataId.startsWith(prefGroup));
  const storedPrefCol = storedPref?.dataId.substring(prefGroup.length) as keyof TaskDescriptor;

  return new Table.TablePaginationImpl<TaskDescriptor>({
    src: [],
    orderBy: storedPrefCol ?? 'dueDate',
    order: storedPref?.direction ?? 'asc',
    sorted: true,
    rowsPerPage: 5,
  })
}



const Row: React.FC<{ rowId: number, row: TaskDescriptor, columns: ColumnName[] }> = (props) => {
  const { pref } = useTaskPrefs();

  const [hoverItemsActive, setHoverItemsActive] = React.useState(false);
  function handleEndHover() {
    setHoverItemsActive(false);
  }
  function handleStartHover() {
    setHoverItemsActive(true);
  }
  const sx = getRowBackgroundColor(props.rowId);

  const cells = React.useMemo(() => {
    return (<>
    {props.columns.includes("title") && <CellTitle rowId={props.rowId} row={props.row} />}
    {props.columns.includes("assignees") && <CellAssignees rowId={props.rowId} row={props.row} />}
    {props.columns.includes("dueDate") && <CellDueDate rowId={props.rowId} row={props.row} />}
    {props.columns.includes("priority") && <CellPriority rowId={props.rowId} row={props.row} />}
    {props.columns.includes("roles") && <CellRoles rowId={props.rowId} row={props.row} />}
    {props.columns.includes("status") && <CellStatus rowId={props.rowId} row={props.row} />}
    </>);
  }, [props]);

  return (<MTableRow sx={sx} hover tabIndex={-1} key={props.row.id} onMouseEnter={handleStartHover} onMouseLeave={handleEndHover}>
    {cells}
    <CellMenu {...props} active={hoverItemsActive} setDisabled={handleEndHover} />
  </MTableRow>);
}


const TableForGroupByPagination: React.FC<{ state: TaskPagination, setState: SetTaskPagination }> = (props) => {
  const { loading } = Context.useTasks();
  const { state, setState } = props;

  return loading ? null :
    (<MPagination
      rowsPerPageOptions={state.rowsPerPageOptions}
      component="div"
      count={state.src.length}
      rowsPerPage={state.rowsPerPage}
      page={state.page}
      onPageChange={(_event, newPage) => setState((state: TaskPagination) => state.withPage(newPage))}
      onRowsPerPageChange={(event: React.ChangeEvent<HTMLInputElement>) => setState((state: TaskPagination) => state.withRowsPerPage(parseInt(event.target.value, 10)))}
    />) 
}


export const HeaderCellWithPrefs: React.FC<{ 
  name: ColumnName;
  classifierValue: string;
  sortable: boolean;
  children: React.ReactNode;
  setContent: SetTaskPagination;
  content: TaskPagination;
}> = ({ name, classifierValue, sortable, children, setContent, content }) => {

  const { pref, withSorting } = useTaskPrefs();
  const vis = pref.getVisibility(name);
  const hidden = vis?.enabled !== true;

  if(hidden) {
    return null;
  }

  if(!sortable) {
    return (<MTableCell align='left' padding='none'>{children}</MTableCell>)
  }

  const createSortHandler = (property: ColumnName) =>
    (_event: React.MouseEvent<unknown>) => setContent(prev => {
      const dataId = getPrefSortId(classifierValue, name);
      const next = prev.withOrderBy(property);
      withSorting({ dataId, direction: next.order });
      return next;
    })

  const active = content.orderBy === name;
  const sortDirection = active ? content.order : false;

  return (<MTableCell align='left' padding='none' sortDirection={sortDirection}>
    <TableSortLabel active={active} direction={active ? content.order : 'asc'} onClick={createSortHandler(name)}>
      <>
        {children}
        {active ? (<Box component="span" sx={visuallyHidden}>{content.order === 'desc' ? 'sorted descending' : 'sorted ascending'}</Box>) : null}
      </>
    </TableSortLabel>
  </MTableCell>);
}

export const TableForGroupBy: React.FC<{ groupByType: GroupByTypes, groupId: string }> = ({ groupByType, groupId }) => {
  const { loading } = Context.useTasks();
  const prefCtx = useTaskPrefs();
  const { pref } = prefCtx;
  const grouping = useGrouping();

  const [content, setContent] = React.useState(initTable(groupId, prefCtx));
  const columns: ColumnName[] = pref.visibility.filter(v => v.enabled).map(v => v.dataId as ColumnName);


  React.useEffect(() => {
    _logger.target(({updated: grouping.collection.updated})).debug(`reloading task table - ${groupId}`);
    const group = grouping.getByGroupId(groupId);
    const records = group?.value.map(index => grouping.collection.origin[index]);

    setContent(prev => prev.withSrc(records))
  }, [grouping]);
  

  return (<>
    <MTableContainer>
      <MTable size='small'>
        <MTableHead>
          <MTableRow>

            { /* reserved title column */}
            <HeaderCellWithPrefs sortable={false} name='title' setContent={setContent} content={content} classifierValue={groupId}>
              <Title groupByType={groupByType} classifierValue={groupId} groupCount={content.entries.length}/>
            </HeaderCellWithPrefs>


            { columns.filter(c => c !== 'title').map((id) => (
              <HeaderCellWithPrefs key={id} name={id as ColumnName} sortable setContent={setContent} content={content} classifierValue={groupId}>  
                <FormattedMessage id={`tasktable.header.${id}`} />
              </HeaderCellWithPrefs>
            ))}

            {/* menu column */}
            {columns.length > 0 && <MTableCell />}
          </MTableRow>
        </MTableHead>


        <TableBody>
          {content.entries.map((row, rowId) => (<Row key={row.id} rowId={rowId} row={row} columns={columns}/>))}
          <TableFillerRows content={content} loading={loading} plusColSpan={7} />
        </TableBody>

      </MTable>
    </MTableContainer>

    <Box display='flex' sx={{ paddingLeft: 1, marginTop: -2 }}>
      <Box alignSelf="center" flexGrow={1}></Box>
      <TableForGroupByPagination state={content} setState={setContent}/>
    </Box>
  </>);

}
