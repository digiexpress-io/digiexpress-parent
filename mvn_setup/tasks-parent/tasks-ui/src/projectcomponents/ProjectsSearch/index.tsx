import React from 'react';
import { TableHead, TableCell, TableRow, Box, Stack } from '@mui/material';

import Context from 'context';
import { ProjectDescriptor, Group } from 'projectdescriptor';
import ProjectsTable from '../ProjectsTable';
import { NavigationSticky } from '../NavigationSticky';
import FilterRepoType from './FilterRepoType';
import FilterUsers from './FilterUsers';
import FilterByString from './FilterByString';
import GroupBy from './GroupBy';



function getRowBackgroundColor(index: number): string {
  const isOdd = index % 2 === 1;

  if (isOdd) {
    return 'uiElements.light';
  }
  return 'background.paper';
}

const Header: React.FC<ProjectsTable.TableConfigProps & { columns: (keyof ProjectDescriptor)[] }> = ({ content, setContent, group, columns }) => {

  const includesTitle = columns.includes("title");

  const headersToShow = includesTitle ?
    columns.filter(c => c !== 'title') :
    columns.slice(1);

  return (
    <TableHead>
      <TableRow>

        { /* reserved title column */}
        <TableCell align='left' padding='none'>
          <ProjectsTable.Title group={group} />
          <ProjectsTable.SubTitle values={group.records.length} message='project.search.projectCount' />
        </TableCell>

        { /* without title */}
        <ProjectsTable.ColumnHeaders columns={headersToShow} content={content} setContent={setContent} />

        {/* menu column */}
        {columns.length > 0 && <TableCell></TableCell>}
      </TableRow>
    </TableHead>
  );
}

const Row: React.FC<{
  rowId: number,
  row: ProjectDescriptor,
  def: Group,
  columns: (keyof ProjectDescriptor)[]
}> = (props) => {

  const [hoverItemsActive, setHoverItemsActive] = React.useState(false);
  function handleEndHover() {
    setHoverItemsActive(false);
  }
  return (<TableRow sx={{ backgroundColor: getRowBackgroundColor(props.rowId) }} hover tabIndex={-1} key={props.row.id}
    onMouseEnter={() => setHoverItemsActive(true)} onMouseLeave={handleEndHover}>
    {props.columns.includes("title") && <ProjectsTable.CellTitle {...props} />}
    {props.columns.includes("repoType") && <ProjectsTable.CellRepoType {...props} />}
    {props.columns.includes("users") && <ProjectsTable.CellUsers {...props} />}
    {props.columns.includes("created") && <ProjectsTable.CellCreatedDate {...props} />}

    <ProjectsTable.CellMenu {...props} active={hoverItemsActive} setDisabled={handleEndHover} />
  </TableRow>);
}

const columnTypes: (keyof ProjectDescriptor)[] = [
  'title',
  'repoType',
  'users',
  'created',
]

const ProjectSearch: React.FC<{}> = () => {
  const projects = Context.useProjects();
  const [state, setState] = React.useState(projects.state.withDescriptors());
  const [columns, setColumns] = React.useState([
    ...columnTypes
  ]);

  React.useEffect(() => {
    setState(prev => prev.withProjects(projects.state))
  }, [projects.state]);

  return (<Box>
    <NavigationSticky>
      <FilterByString onChange={({ target }) => setState(prev => prev.withSearchString(target.value))} />
      <Stack direction='row' spacing={1}>
        <GroupBy value={state.groupBy} onChange={(value) => setState(prev => prev.withGroupBy(value))} />
        <FilterRepoType value={state.filterBy} onChange={(value) => setState(prev => prev.withFilterByRepoType(value))} />
        <FilterUsers value={state.filterBy} onChange={(value) => setState(prev => prev.withFilterByUsers(value))} />
      </Stack>
    </NavigationSticky>
    <Box mt={1} />
    <ProjectsTable.Groups groups={state.groups} orderBy='created'>
      {{
        Header: (props) => <Header columns={columns} {...props} />,
        Rows: ({ content, group, loading }) => (
          <ProjectsTable.TableBody>
            {content.entries.map((row, rowId) => (<Row key={row.id} rowId={rowId} row={row} def={group} columns={columns} />))}
            <ProjectsTable.TableFiller content={content} loading={loading} plusColSpan={7} />
          </ProjectsTable.TableBody>
        )
      }}
    </ProjectsTable.Groups>
  </Box>
  );
}


export default ProjectSearch;