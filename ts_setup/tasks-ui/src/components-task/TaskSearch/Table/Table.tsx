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

import { TableBody, TableFillerRows } from 'components-generic';
import { cyan_mud } from 'components-colors';

import { PreferenceContextType } from 'descriptor-prefs';
import { TaskDescriptor, useTasks } from 'descriptor-task';
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


function taskPaginationPropsAreEqual(prev: TaskPagination, next: TaskPagination): boolean {
  if(prev.src.length != next.src.length) {
    return false;
  }
  if(prev.entries.length != next.entries.length) {
    return false;
  }


  for(let index = 0; index < prev.entries.length; index++) {
    const prevProps = prev.entries[index];
    const nextProps = next.entries[index];

    if(prevProps === undefined && nextProps === undefined) {
      continue;
    }

    if(prevProps === undefined || nextProps === undefined) {
      return false;
    }
    if(!prevProps.equals(nextProps)) {
      return false;
    }
  }
  return true;
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
    propsAreEqual: taskPaginationPropsAreEqual
  })
}

interface RowProps {
  rowId: number;
  row: TaskDescriptor;
  columns: ColumnName[];
}

const Row: React.FC<RowProps> = React.memo((props) => {

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
},
(prevProps: Readonly<RowProps>, nextProps: Readonly<RowProps>) => {
  return (
    prevProps.rowId == nextProps.rowId &&
    prevProps.columns.filter(x => !nextProps.columns.includes(x)).length === 0 &&
    prevProps.columns.length === nextProps.columns.length &&
    prevProps.row.equals(nextProps.row))
  }
)

const TableForGroupByPagination: React.FC<{ state: TaskPagination, setState: SetTaskPagination }> = (props) => {
  const { loading } = useTasks();
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


interface DelegateProps {
  groupByType: GroupByTypes,
  groupId: string,
  content: TaskPagination,
  setContent: React.Dispatch<React.SetStateAction<TaskPagination>>
}

const Delegate: React.FC<DelegateProps> = React.memo(({ groupByType, groupId, content, setContent }) => {

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

},
(prevProps: Readonly<DelegateProps>, nextProps: Readonly<DelegateProps>) => {
  const isEqual = (
    prevProps.groupByType == nextProps.groupByType &&
    prevProps.groupId === nextProps.groupId &&
    prevProps.content.equals(nextProps.content)
  );
  if(!isEqual) {
    _logger.debug(`reloading task table - ${nextProps.groupId}`);
  }
  return (isEqual)
  } 
)

export const TableForGroupBy: React.FC<{ groupByType: GroupByTypes, groupId: string }> = ({ groupByType, groupId }) => {
  const prefCtx = useTaskPrefs();
  const grouping = useGrouping();
  const [content, setContent] = React.useState<TaskPagination>(initTable(groupId, prefCtx));
  
  React.useEffect(() => {
    const group = grouping.getByGroupId(groupId);
    const records = group?.value.map(index => grouping.collection.origin[index]);

    setContent(prev => prev.withSrc(records))
  }, [grouping]);


  return (<Delegate 
    content={content} 
    setContent={setContent as React.Dispatch<React.SetStateAction<TaskPagination>>} 
    groupByType={groupByType} groupId={groupId}
  />);

}
