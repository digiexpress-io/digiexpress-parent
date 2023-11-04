import React from 'react';
import { Box } from '@mui/material';
import ArrowForwardIosOutlinedIcon from '@mui/icons-material/ArrowForwardIosOutlined';

import { TaskDescriptor, Group } from 'descriptor-task';
import TaskCell from './TaskCell';
import CellHoverButton from './CellMenuButton';
import CRMDialog from '../CRM';
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
  const [crm, setCrm] = React.useState(false);

  function handleStartEdit() {
    setCrm(true);
    setDisabled();
  }

  function handleCrm() {
    setCrm(false);
  }


  return (
    <StyledTableCell width="500px">
      <Box justifyContent='left' display='flex'>
        <TaskCell id={row.id + "/Subject"} name={row.title} maxWidth={"500px"} />
        <CRMDialog open={crm} onClose={handleCrm} task={row} />
        {active && <HoverMenu onEdit={handleStartEdit} />}
      </Box>
    </StyledTableCell>
  );

}

export default FormattedCell;

