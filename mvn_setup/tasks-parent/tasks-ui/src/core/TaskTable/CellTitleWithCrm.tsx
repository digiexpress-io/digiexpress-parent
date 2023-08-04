import React from 'react';
import { Box, Typography } from '@mui/material';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';
import ArrowForwardIosOutlinedIcon from '@mui/icons-material/ArrowForwardIosOutlined';

import { FormattedMessage } from 'react-intl';

import client from '@taskclient';
import Styles from '@styles';

import TaskOps from '../TaskOps';
import TaskCell from './TaskCell';
import CellHoverButton from './CellMenuButton';

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
    <Styles.TableCell width="500px">
      <Box justifyContent='left' display='flex'>
        <TaskCell id={row.id + "/Subject"} name={row.title} maxWidth={"500px"} />
        <TaskOps.StartTaskDialog open={edit} onClose={handleEndEdit} task={row} />
        {active && <HoverMenu onEdit={handleStartEdit} />}
      </Box>
    </Styles.TableCell>
  );

}

export default FormattedCell;

