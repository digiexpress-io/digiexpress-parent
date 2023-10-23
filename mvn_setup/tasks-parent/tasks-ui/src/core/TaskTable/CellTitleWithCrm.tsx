import React from 'react';
import { Box } from '@mui/material';
import ArrowForwardIosOutlinedIcon from '@mui/icons-material/ArrowForwardIosOutlined';

import { TaskDescriptor, Group } from 'taskdescriptor';
import TaskCell from './TaskCell';
import CellHoverButton from './CellMenuButton';
import TaskWorkDialog from 'core/TaskWork';
import { StyledTableCell } from './StyledTable';

const HoverMenu: React.FC<{ onEdit: () => void }> = ({ onEdit }) => {


  return (
    <>
      <CellHoverButton onClick={onEdit}>
        <ArrowForwardIosOutlinedIcon fontSize='small' />
      </CellHoverButton>
    </>
  )
}

const FormattedCell: React.FC<{
  rowId: number,
  row: TaskDescriptor,
  def: Group,

  active: boolean,
  setDisabled: () => void

}> = ({ row, setDisabled, active }) => {
  const [edit, setEdit] = React.useState(false);

  function handleStartEdit() {
    setEdit(true);
    setDisabled();
  }

  function handleEndEdit() {
    setEdit(false);
  }


  return (
    <StyledTableCell width="500px">
      <Box justifyContent='left' display='flex'>
        <TaskCell id={row.id + "/Subject"} name={row.title} maxWidth={"500px"} />
        <TaskWorkDialog open={edit} onClose={handleEndEdit} task={row} />
        {active && <HoverMenu onEdit={handleStartEdit} />}
      </Box>
    </StyledTableCell>
  );

}

export default FormattedCell;

