import React from 'react';
import { TableHead, TableCell, TableRow, Box, AppBar, Toolbar, Stack, Typography, Menu, MenuItem, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { SearchFieldBar } from '../SearchField';
import client from '@taskclient';
import TaskTable from '../TaskTable';
import Tools from '../TaskTools';

/*
const OptionButton: React.FC<{ onClick: (event: React.MouseEvent<HTMLButtonElement>) => void, label: string }> = ({ onClick, label }) => {
  return (
    <Button variant='outlined' sx={{ borderRadius: 10 }} onClick={onClick} >
      <Typography variant='caption' sx={{ color: 'text.primary' }}><FormattedMessage id={label} /></Typography>
    </Button>
  )
}

const SearchBar: React.FC<{}> = () => {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };

  return (<>
    <Menu
      anchorEl={anchorEl}
      open={open}
      onClose={handleClose}
    >
      <MenuItem onClick={handleClose}>Profile</MenuItem>
      <MenuItem onClick={handleClose}>My account</MenuItem>
      <MenuItem onClick={handleClose}>Logout</MenuItem>
    </Menu>
    <AppBar color='inherit' position='sticky' sx={{ boxShadow: 1 }}>
      <Toolbar sx={{ backgroundColor: 'table.main', '&.MuiToolbar-root': { p: 1, m: 0 } }}>
        <Stack direction='row' spacing={2} alignItems='center'>
          <SearchFieldBar onChange={() => { }} />
          <OptionButton onClick={handleClick} label='Group by' />
          <OptionButton onClick={handleClick} label='Status' />
          <OptionButton onClick={handleClick} label='Priority' />
          <OptionButton onClick={handleClick} label='Owners' />
          <OptionButton onClick={handleClick} label='Roles' />
          <OptionButton onClick={handleClick} label='All' />
          <OptionButton onClick={handleClick} label='Column options' />
        </Stack>
      </Toolbar>
    </AppBar >
  </>

  );
}

*/
function getRowBackgroundColor(index: number): string {
  const isOdd = index % 2 === 1;

  if (isOdd) {
    return 'uiElements.light';
  }
  return 'background.paper';
}

const Header: React.FC<TaskTable.TableConfigProps> = ({ content, setContent, group }) => {

  const columns: (keyof client.TaskDescriptor)[] = React.useMemo(() => [
    'assignees',
    'dueDate',
    'priority',
    'roles',
    'status'
  ], []);

  return (
    <TableHead>
      <TableRow>
        <TableCell align='left' padding='none'>
          <TaskTable.Title group={group} />
          <TaskTable.SubTitle values={group.records.length} message='core.teamSpace.taskCount' />
        </TableCell>

        <TaskTable.ColumnHeaders columns={columns} content={content} setContent={setContent} />
        <TableCell></TableCell>
      </TableRow>
    </TableHead>
  );
}

const Row: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  def: client.Group
}> = (props) => {

  const [hoverItemsActive, setHoverItemsActive] = React.useState(false);
  function handleEndHover() {
    setHoverItemsActive(false);
  }

  return (<TableRow sx={{ backgroundColor: getRowBackgroundColor(props.rowId) }} hover tabIndex={-1} key={props.row.id} onMouseEnter={() => setHoverItemsActive(true)} onMouseLeave={handleEndHover}>
    <TaskTable.CellTitle {...props} children={hoverItemsActive} />
    <TaskTable.CellAssignees {...props} />
    <TaskTable.CellDueDate {...props} />
    <TaskTable.CellPriority {...props} />
    <TaskTable.CellRoles {...props} />
    <TaskTable.CellStatus {...props} />
    <TaskTable.CellMenu {...props} active={hoverItemsActive} setDisabled={handleEndHover} />
  </TableRow>);
}


const Rows: React.FC<TaskTable.TableConfigProps> = ({ content, group, loading }) => {
  return (
    <TaskTable.TableBody>
      {content.entries.map((row, rowId) => (<Row key={row.id} rowId={rowId} row={row} def={group} />))}

      <TaskTable.TableFiller content={content} loading={loading} plusColSpan={7} />
    </TaskTable.TableBody>
  )
}


//<SearchBar />
const TaskSearch: React.FC<{}> = () => {
  return (<Box>
    <TaskTable.Groups groupBy={undefined} orderBy='created'>
      {{ Header, Rows, Tools }}
    </TaskTable.Groups>
  </Box>
  );
}


export default TaskSearch;