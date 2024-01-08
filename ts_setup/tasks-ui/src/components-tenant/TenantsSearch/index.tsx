import React from 'react';
import { TableHead, TableCell, TableRow, Box, Stack } from '@mui/material';

import Context from 'context';
import { TenantEntryDescriptor, Group } from 'descriptor-tenant';
import TenantsTable from '../TenantsTable';
import { NavigationSticky } from '../NavigationSticky';
import FilterByString from './FilterByString';

function getRowBackgroundColor(index: number): string {
  const isOdd = index % 2 === 1;

  if (isOdd) {
    return cyan;
  }
  return 'background.paper';
}

const Header: React.FC<TenantsTable.TableConfigProps & { columns: (keyof TenantEntryDescriptor)[] }> = ({ content, setContent, group, columns }) => {

  const includesTitle = columns.includes("formTitle");

  const headersToShow = includesTitle ?
    columns.filter(c => c !== 'formTitle') :
    columns.slice(1);

  return (
    <TableHead>
      <TableRow>

        { /* reserved title column */}
        <TableCell align='left' padding='none'>
          <TenantsTable.Title group={group} />
          <TenantsTable.SubTitle values={group.records.length} message='project.search.projectCount' />
        </TableCell>

        { /* without title */}
        <TenantsTable.ColumnHeaders columns={headersToShow} content={content} setContent={setContent} />

        {/* menu column */}
        {columns.length > 0 && <TableCell></TableCell>}
      </TableRow>
    </TableHead>
  );
}

const Row: React.FC<{
  rowId: number,
  row: TenantEntryDescriptor,
  def: Group,
  columns: (keyof TenantEntryDescriptor)[]
}> = (props) => {

  const [hoverItemsActive, setHoverItemsActive] = React.useState(false);
  function handleEndHover() {
    setHoverItemsActive(false);
  }
  return (<TableRow sx={{ backgroundColor: getRowBackgroundColor(props.rowId) }} hover tabIndex={-1} key={props.row.source.id}
    onMouseEnter={() => setHoverItemsActive(true)} onMouseLeave={handleEndHover}>
    {props.columns.includes("formTitle") && <TenantsTable.CellFormTitle {...props} />}

    {/* <TenantsTable.CellMenu {...props} active={hoverItemsActive} setDisabled={handleEndHover} /> */}
  </TableRow>);
}

const columnTypes: (keyof TenantEntryDescriptor)[] = [
  'formTitle',
  'lastSaved',
  'created',
]

const TenantEntriesSearch: React.FC<{}> = () => {
  const backend = Context.useBackend();
  const projects = Context.useTenants();
  const tenantEntries = projects.state.tenantEntries;
  const [state, setState] = React.useState(projects.state.toGroupsAndFilters());



  const [columns, setColumns] = React.useState([
    ...columnTypes
  ]);

  React.useEffect(() => {
    setState(prev => prev.withEntries(tenantEntries));
  }, [backend, tenantEntries]);

  return (<Box>
    <NavigationSticky>
      <FilterByString onChange={({ target }) => setState(prev => prev.withSearchString(target.value))} />
      <Stack direction='row' spacing={1}>
        {/*<GroupBy value={state.groupBy} onChange={(value) => setState(prev => prev.withGroupBy(value))} /> */}

      </Stack>
    </NavigationSticky>
    <Box mt={1} />
    <TenantsTable.Groups groups={state.groups} orderBy='created'>
      {{
        Header: (props) => <Header columns={columns} {...props} />,
        Rows: ({ content, group, loading }) => (
          <TenantsTable.TableBody>
            {content.entries.map((row, rowId) => (<Row key={row.source.id} rowId={rowId} row={row} def={group} columns={columns} />))}
            <TenantsTable.TableFiller content={content} loading={loading} plusColSpan={7} />
          </TenantsTable.TableBody>
        )
      }}
    </TenantsTable.Groups>
  </Box>
  );
}


export default TenantEntriesSearch;