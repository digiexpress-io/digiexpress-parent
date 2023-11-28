import React from 'react';
import { TableHead, TableCell, TableRow, Box } from '@mui/material';

import Context from 'context';
import { CustomerDescriptor } from 'descriptor-customer';
import { NavigationSticky, FilterByString, TableBody, TableFillerRows } from 'components-generic';

import { CustomersSearchState, CustomerTable, initCustomersSearchState, TableConfigProps } from './table-ctx';
import { TableTitle, TableSubTitle } from './TableTitle';
import { SortableHeaders } from './TableHeaders';

import CellMenu from './CellMenu';
import CellDisplayName from './CellDisplayName';
import CellCustomerType from './CellCustomerType';
import CellCreated from './CellCreated';
import CellLastLogin from './CellLastLogin';
import CellTasks from './CellTasks';



function getRowBackgroundColor(index: number): string {
  const isOdd = index % 2 === 1;

  if (isOdd) {
    return 'uiElements.light';
  }
  return 'background.paper';
}

const Header: React.FC<TableConfigProps & { columns: (keyof CustomerDescriptor)[] }> = ({ content, setContent, group, columns }) => {

  const includesTitle = columns.includes("displayName");

  const headersToShow = includesTitle ?
    columns.filter(c => c !== 'displayName') :
    columns.slice(1);

  return (
    <TableHead>
      <TableRow>

        { /* reserved title column */}
        <TableCell align='left' padding='none'>
          <TableTitle group={group} />
        </TableCell>

        { /* without title */}
        <SortableHeaders columns={headersToShow} content={content} setContent={setContent} />

        {/* menu column */}
        {columns.length > 0 && <TableCell></TableCell>}
      </TableRow>
    </TableHead>
  );
}

const Row: React.FC<{
  rowId: number,
  row: CustomerDescriptor,
  def: CustomersSearchState,
  columns: (keyof CustomerDescriptor)[]
}> = (props) => {

  const [hoverItemsActive, setHoverItemsActive] = React.useState(false);
  function handleEndHover() {
    setHoverItemsActive(false);
  }
  return (<TableRow sx={{ backgroundColor: getRowBackgroundColor(props.rowId) }} hover tabIndex={-1} key={props.row.id}
    onMouseEnter={() => setHoverItemsActive(true)} onMouseLeave={handleEndHover}>
    {props.columns.includes("displayName") && <CellDisplayName {...props} children={hoverItemsActive} />}
    {props.columns.includes("customerType") && <CellCustomerType {...props} />}
    {props.columns.includes("created") && <CellCreated {...props} />}
    {props.columns.includes("lastLogin") && <CellLastLogin {...props} />}
    {props.columns.includes("tasks") && <CellTasks {...props} />}

    <CellMenu {...props} active={hoverItemsActive} setDisabled={handleEndHover} />
  </TableRow>);
}

const columnTypes: (keyof CustomerDescriptor)[] = [
  'displayName',
  'customerType',
  'created',
  'lastLogin',
  'tasks']

const CustomerSearch: React.FC<{}> = () => {
  const backend = Context.useBackend();
  const tasks = Context.useTasks();
  const profile = tasks.state.profile;
  const [columns, setColumns] = React.useState([...columnTypes]);
  const [state, setState] = React.useState<CustomersSearchState>(initCustomersSearchState(profile));
  const [loading, setLoading] = React.useState<boolean>(false);
  const { searchString, isSearchStringValid } = state;

  React.useEffect(() => {
    if (isSearchStringValid) {
      setLoading(true);
      backend.customer.findCustomers(searchString).then(newRecords => {
        setState(prev => prev.withRecords(newRecords));
        setLoading(false);
      });
    }
  }, [searchString, isSearchStringValid]);

  return (<Box>
    <NavigationSticky>
      <FilterByString onChange={({ target }) => setState(prev => prev.withSearchString(target.value))} />
    </NavigationSticky>
    <Box mt={1} />
    <CustomerTable group={state} defaultOrderBy='displayName' loading={loading}>
      {{
        Header: (props) => <Header columns={columns} {...props} />,
        Rows: ({ content, group, loading }) => (
          <TableBody>
            {content.entries.map((row, rowId) => (<Row key={row.id} rowId={rowId} row={row} def={group} columns={columns} />))}
            <TableFillerRows content={content} loading={loading} plusColSpan={5} />
          </TableBody>
        )
      }}
    </CustomerTable>
  </Box>
  );
}


export default CustomerSearch;