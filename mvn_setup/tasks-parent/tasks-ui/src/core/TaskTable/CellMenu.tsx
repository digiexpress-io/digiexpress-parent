import React from 'react';
import { Box, MenuList, MenuItem, ListItemText, Divider, styled } from '@mui/material';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';
import DeleteIcon from '@mui/icons-material/Delete';
import { FormattedMessage } from 'react-intl';

import client from '@taskclient';
import { usePopover } from './CellPopover';
import CellHoverButton from './CellMenuButton';
import TaskEditDialog from 'core/TaskEdit';
import TaskWorkDialog from 'core/TaskWork';
import { StyledTableCell } from './StyledTable';

const StyledListItemText = styled(ListItemText)(({ theme }) => ({
  color: theme.palette.error.main,
  "&>span":{
    display: "flex",
    alignItems: 'center'
  }
}));

const HoverMenu: React.FC<{ 
  onEdit: () => void, 
  onWork: () => void,
}> = ({ onEdit, onWork }) => {
  const Popover = usePopover();

  return (
    <>
      <Popover.Delegate>
        <MenuList dense>
          <MenuItem onClick={onEdit}>
            <ListItemText>
              <FormattedMessage id={`tasktable.menu.edit`} />
            </ListItemText>
          </MenuItem>
          <MenuItem onClick={onWork}>
            <ListItemText>
              <FormattedMessage id={`tasktable.menu.work`} />
            </ListItemText>
          </MenuItem>
          <MenuItem>
            <ListItemText>
              <FormattedMessage id={`tasktable.menu.viewData`} />
            </ListItemText>
          </MenuItem>
          <Divider />
          <MenuItem>
            <StyledListItemText>
              <DeleteIcon />
              <FormattedMessage id={`tasktable.menu.archive`} />
            </StyledListItemText>
          </MenuItem>
        </MenuList>
      </Popover.Delegate>
      <CellHoverButton onClick={Popover.onClick}>
        <MoreHorizOutlinedIcon fontSize='small' />
      </CellHoverButton>
    </>)
}

const FormattedCell: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  def: client.Group,
  active: boolean,
  setDisabled: () => void
}> = ({ row, def, active, setDisabled }) => {
  const [edit, setEdit] = React.useState(false);
  const [work, setWork] = React.useState(false);

  function handleStartEdit() {
    setEdit(true);
    setDisabled();
  }

  function handleEndEdit() {
    setEdit(false);
  }

  function handleStartWork() {
    setWork(true);
    setDisabled();
  }

  function handleEndWork() {
    setWork(false);
  }

  return (<StyledTableCell width="35px">
    <Box width="35px" justifyContent='right'> {/* Box is needed to prevent table cell resize on hover */}
      <TaskEditDialog open={edit} onClose={handleEndEdit} task={row} />
      <TaskWorkDialog open={work} onClose={handleEndWork} task={row} />
      {active && 
        <HoverMenu 
          onEdit={handleStartEdit} 
          onWork={handleStartWork} 
        />}
    </Box>
  </StyledTableCell>);
}

export default FormattedCell;

