import React from 'react';
import { Box } from '@mui/material';
import ArrowForwardIosOutlinedIcon from '@mui/icons-material/ArrowForwardIosOutlined';

import client from '@taskclient';

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

/*
const StartWorkButton: React.FC<{ onClick: () => void }> = ({onClick}) => {
  return (<Button onClick={onClick} variant='outlined' color='inherit' sx={{ pr: 1 }} endIcon={<ArrowForwardIosOutlinedIcon />}>
      <Typography variant='caption'><FormattedMessage id='core.myWork.button.task.start' /></Typography>
        </Button>)
}
        */

const FormattedCell: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  def: client.Group,

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

