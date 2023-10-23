import React from 'react';
import { Box, MenuList, MenuItem, ListItemText, Divider, styled } from '@mui/material';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';
import DeleteIcon from '@mui/icons-material/Delete';
import { FormattedMessage } from 'react-intl';

import { TaskDescriptor, Group } from 'taskdescriptor';
import { usePopover } from './CellPopover';
import CellHoverButton from './CellMenuButton';
import TaskEditDialog from 'core/TaskEdit';
import TaskWorkDialog from 'core/TaskWork';
import { StyledTableCell } from './StyledTable';

const StyledBox = styled(Box)(({ theme }) => ({
  color: theme.palette.error.main,
  display: "flex",
  alignItems: 'center'
}));

const CellMenuItem: React.FC<{
  onClick?: () => void,
  title: string,
}> = ({ title, onClick }) => {
  return (
    <MenuItem onClick={onClick}>
      <ListItemText>
        <FormattedMessage id={title} />
      </ListItemText>
    </MenuItem>
  )
}

const HoverMenu: React.FC<{
  onEdit: () => void,
  onWork: () => void,
}> = ({ onEdit, onWork }) => {
  const Popover = usePopover();

  return (
    <>
      <Popover.Delegate>
        <MenuList dense>
          <CellMenuItem onClick={onEdit} title={`tasktable.menu.edit`} />
          <CellMenuItem onClick={onWork} title={`tasktable.menu.work`} />
          <CellMenuItem title={`tasktable.menu.viewData`} />
          <Divider />
          <MenuItem>
            <StyledBox>
              <DeleteIcon />
              <FormattedMessage id={`tasktable.menu.archive`} />
            </StyledBox>
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
  row: TaskDescriptor,
  def: Group,
  active: boolean,
  setDisabled: () => void
}> = ({ row, active, setDisabled }) => {
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

  return (
    <StyledTableCell width="35px">
      <Box width="35px" justifyContent='right'> {/* Box is needed to prevent table cell resize on hover */}
        <TaskEditDialog open={edit} onClose={handleEndEdit} task={row} />
        <TaskWorkDialog open={work} onClose={handleEndWork} task={row} />
        {active &&
          <HoverMenu
            onEdit={handleStartEdit}
            onWork={handleStartWork}
          />}
      </Box>
    </StyledTableCell>
  );
}

export default FormattedCell;

