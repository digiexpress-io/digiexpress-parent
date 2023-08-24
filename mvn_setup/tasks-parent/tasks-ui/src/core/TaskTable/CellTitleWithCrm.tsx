import React from 'react';
import { Box } from '@mui/material';
import ArrowForwardIosOutlinedIcon from '@mui/icons-material/ArrowForwardIosOutlined';

import client from '@taskclient';
import Styles from '@styles';

import TaskOps from '../TaskOps';
import TaskCell from './TaskCell';
import CellHoverButton from './CellMenuButton';
import TaskClient from '@taskclient';

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
  const { resetTab } = TaskClient.useMenu();
  const [edit, setEdit] = React.useState(false);

  function handleStartEdit() {
    setEdit(true);
    setDisabled();
  }

  function handleEndEdit() {
    setEdit(false);
    resetTab();
  }


  return (
    <Styles.TableCell width="500px">
      <Box justifyContent='left' display='flex'>
        <TaskCell id={row.id + "/Subject"} name={row.title} maxWidth={"500px"} />
        <TaskOps.WorkOnTaskDialog open={edit} onClose={handleEndEdit} task={row} />
        {active && <HoverMenu onEdit={handleStartEdit} />}
      </Box>
    </Styles.TableCell>
  );

}

export default FormattedCell;

