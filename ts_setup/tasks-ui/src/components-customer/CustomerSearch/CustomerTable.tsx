import React from 'react';
import {
  TableHead as MTableHead,
  TableCell as MTableCell,
  TableContainer as MTableContainer,
  Table as MTable,
  TablePagination as MPagination,
  TableRow as MTableRow,
  TableSortLabel,
  Box, Button, TableBody
} from '@mui/material';
import { SxProps } from '@mui/system';

import { visuallyHidden } from '@mui/utils';
import { FormattedMessage } from 'react-intl';

import Table from 'table';
import Backend from 'descriptor-backend';

import { cyan_mud } from 'components-colors';

import { NavigationSticky, FilterByString, TableFillerRows } from 'components-generic';


import { PreferenceContextType } from 'descriptor-prefs';
import { CustomerDescriptor, ImmutableCustomerStore } from 'descriptor-customer';
import { useTasks } from 'descriptor-task';

import { useCustomerPrefs, ColumnName, ImmutableCustomersSearchState, CustomerTableProvider } from './TableContext';
import CellMenu from './TableCell/CellMenu';
import CellDisplayName from './TableCell/CellDisplayName';
import CellCustomerType from './TableCell/CellCustomerType';
import CellCreated from './TableCell/CellCreated';
import CellLastLogin from './TableCell/CellLastLogin';
import CellTasks from './TableCell/CellTasks';


import LoggerFactory from 'logger';
const _logger = LoggerFactory.getLogger();



type CustomerPagination = Table.TablePagination<CustomerDescriptor>;
type SetCustomerPagination = React.Dispatch<React.SetStateAction<CustomerPagination>>;


function getRowBackgroundColor(index: number): SxProps {
  const isOdd = index % 2 === 1;

  if (isOdd) {
    return { backgroundColor: cyan_mud };
  }
  return { backgroundColor: 'background.paper' };
}

function initTable(prefCtx: PreferenceContextType): CustomerPagination {
  const storedPref = prefCtx.pref.sorting.find(() => true);
  const storedPrefCol = storedPref?.dataId as keyof CustomerDescriptor;

  return new Table.TablePaginationImpl<CustomerDescriptor>({
    src: [],
    orderBy: storedPrefCol ?? 'created',
    order: storedPref?.direction ?? 'asc',
    sorted: true,
    rowsPerPage: 15,
  })
}

const CustomerPagination: React.FC<{ state: CustomerPagination, setState: SetCustomerPagination }> = (props) => {
  const { loading } = useTasks();
  const { state, setState } = props;
  if(state.src.length === 0) {
    return null;
  }

  return loading ? null :
    (<MPagination
      rowsPerPageOptions={state.rowsPerPageOptions}
      component="div"
      count={state.src.length}
      rowsPerPage={state.rowsPerPage}
      page={state.page}
      onPageChange={(_event, newPage) => setState((state: CustomerPagination) => state.withPage(newPage))}
      onRowsPerPageChange={(event: React.ChangeEvent<HTMLInputElement>) => setState((state: CustomerPagination) => state.withRowsPerPage(parseInt(event.target.value, 10)))}
    />)
}



const Row: React.FC<{ rowId: number, row: CustomerDescriptor, columns: ColumnName[] }> = (props) => {
  const { pref } = useCustomerPrefs();

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
      {props.columns.includes("displayName") && <CellDisplayName {...props} children={hoverItemsActive} />}
      {props.columns.includes("customerType") && <CellCustomerType rowId={props.rowId} row={props.row} />}
      {props.columns.includes("created") && <CellCreated rowId={props.rowId} row={props.row} />}
      {props.columns.includes("lastLogin") && <CellLastLogin rowId={props.rowId} row={props.row} />}
      {props.columns.includes("tasks") && <CellTasks rowId={props.rowId} row={props.row} />}
    </>);
  }, [props]);

  return (<MTableRow sx={sx} hover tabIndex={-1} key={props.row.id} onMouseEnter={handleStartHover} onMouseLeave={handleEndHover}>
    {cells}
    <CellMenu rowId={props.rowId} row={props.row} active={hoverItemsActive} setDisabled={handleEndHover} />
  </MTableRow>);
}


export const HeaderCellWithPrefs: React.FC<{
  name: ColumnName;
  sortable: boolean;
  children: React.ReactNode;
  setContent: SetCustomerPagination;
  content: CustomerPagination;
}> = ({ name, sortable, children, setContent, content }) => {

  const { pref, withSorting } = useCustomerPrefs();
  const vis = pref.getVisibility(name);
  const hidden = vis?.enabled !== true;

  if (hidden) {
    return null;
  }

  if (!sortable) {
    return (<MTableCell align='left' padding='none'>{children}</MTableCell>)
  }

  const createSortHandler = (property: ColumnName) =>
    (_event: React.MouseEvent<unknown>) => setContent(prev => {
      const dataId = name;
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


const TableTitle: React.FC = () => {
  const sx = { borderRadius: '8px 8px 0px 0px', boxShadow: "unset", fontWeight: 'bolder' };
  return (
    <Button color="primary" variant="contained" sx={sx}>
      <FormattedMessage id={`customertable.header.spotlight.searchResults`} />
    </Button>);
}

const CustomerTableDelegate: React.FC<{}> = () => {
  const backend = Backend.useBackend();
  const prefCtx = useCustomerPrefs();
  const { pref } = prefCtx;

  const [content, setContent] = React.useState(initTable(prefCtx));
  const [state, setState] = React.useState<ImmutableCustomersSearchState>(new ImmutableCustomersSearchState({}));
  const [loading, setLoading] = React.useState<boolean>(false);
  const { searchString, isSearchStringValid, records: found } = state;
  const columns: ColumnName[] = pref.visibility.filter(v => v.enabled).map(v => v.dataId as ColumnName);

  React.useEffect(() => {
    if (isSearchStringValid) {
      _logger.debug(`reloading customer table - ${searchString}`);

      setLoading(true);
      new ImmutableCustomerStore(backend.store).findCustomers(searchString).then(newRecords => {
        setState(prev => prev.withRecords(newRecords));
        setLoading(false);
      });
    }
  }, [searchString, isSearchStringValid]);

  React.useEffect(() => setContent(prev => prev.withSrc(found)), [found]);

  return (
      <Box>
        <NavigationSticky>
          <FilterByString onChange={({ target }) => setState(prev => prev.withSearchString(target.value))} />
        </NavigationSticky>

        <MTableContainer>
          <MTable size='small'>
            <MTableHead>
              <MTableRow>

                { /* reserved title column */}
                <HeaderCellWithPrefs sortable={false} name='displayName' setContent={setContent} content={content}>
                  <TableTitle />
                </HeaderCellWithPrefs>


                {columns.filter(c => c !== 'displayName').map((id) => (
                  <HeaderCellWithPrefs key={id} name={id as ColumnName} sortable setContent={setContent} content={content}>
                    <FormattedMessage id={`customertable.header.${id}`} />
                  </HeaderCellWithPrefs>
                ))}

                {/* menu column */}
                {columns.length > 0 && <MTableCell />}
              </MTableRow>
            </MTableHead>

            <TableBody>
              {content.entries.map((row, rowId) => (<Row key={row.id} rowId={rowId} row={row} columns={columns} />))}
              <TableFillerRows content={content} loading={loading} plusColSpan={6} />
            </TableBody>

          </MTable>
        </MTableContainer>

        <Box display='flex' sx={{ paddingLeft: 1, marginTop: -2 }}>
          <Box alignSelf="center" flexGrow={1}></Box>
          <CustomerPagination state={content} setState={setContent} />
        </Box>
      </Box>);

}


export const CustomerTable: React.FC<{}> = () => {
  return (
    <CustomerTableProvider>
      <CustomerTableDelegate />
    </CustomerTableProvider>);

}