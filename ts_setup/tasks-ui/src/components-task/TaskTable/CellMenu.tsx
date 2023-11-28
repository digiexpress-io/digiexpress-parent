import React from 'react';
import { Box, MenuList, MenuItem, ListItemText, Divider, styled } from '@mui/material';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';
import DeleteIcon from '@mui/icons-material/Delete';
import { FormattedMessage } from 'react-intl';

import { TaskDescriptor, Group } from 'descriptor-task';
import { usePopover } from './CellPopover';
import CellHoverButton from './CellMenuButton';
import TaskEditDialog from '../TaskEdit';
import Customer from 'components-customer';
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

const CellMenu: React.FC<{
  onEdit: () => void,
  onCRM: () => void,
}> = ({ onEdit, onCRM }) => {
  const Popover = usePopover();

  return (
    <>
      <Popover.Delegate>
        <MenuList dense>
          <CellMenuItem onClick={onEdit} title={`tasktable.menu.edit`} />
          <CellMenuItem onClick={onCRM} title={`tasktable.menu.viewData`} />
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
  const [crm, setCrm] = React.useState(false);

  function handleStartEdit() {
    setEdit(true);
    setDisabled();
  }

  function handleEndEdit() {
    setEdit(false);
  }

  function handleStartWork() {
    setCrm(true);
    setDisabled();
  }

  function handleCrm() {
    setCrm(false);
  }

  return (
    <StyledTableCell width="35px">
      <Box width="35px" justifyContent='right'> {/* Box is needed to prevent table cell resize on hover */}
        <TaskEditDialog open={edit} onClose={handleEndEdit} task={row} />
        <Customer.CustomerDetailsDialog open={crm} onClose={handleCrm} task={row} />
        {active &&
          <CellMenu
            onEdit={handleStartEdit}
            onCRM={handleStartWork}
          />}
      </Box>
    </StyledTableCell>
  );
}

export default FormattedCell;

