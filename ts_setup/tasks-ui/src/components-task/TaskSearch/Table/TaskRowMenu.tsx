import React from 'react';
import { Box, MenuList, MenuItem, ListItemText, Divider, styled, SxProps, IconButton } from '@mui/material';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';
import DeleteIcon from '@mui/icons-material/Delete';
import { FormattedMessage } from 'react-intl';

import { TaskDescriptor } from 'descriptor-task';
import { useTableCellPopover } from 'components-generic';
import Customer from 'components-customer';


import TaskEditDialog from '../../TaskEdit';
import { useXTableRow } from 'components-xtable';
import { cyan } from '@mui/material/colors';


const iconButtonSx: SxProps = { fontSize: 'small', color: cyan, p: 0.5 }

const CellHoverButton: React.FC<{ onClick: (event: React.MouseEvent<HTMLElement>) => void, children: React.ReactNode }> = ({ onClick, children }) => {
  return (
    <IconButton sx={iconButtonSx} onClick={onClick}>
      {children}
    </IconButton>)
}

const StyledBox = styled(Box)(({ theme }) => ({
  color: theme.palette.error.main,
  display: "flex",
  alignItems: 'center'
}));

const CellMenuItem: React.FC<{
  onClick?: () => void,
  title: string,
  disabled?: boolean
}> = ({ title, onClick, disabled }) => {

  return (
    <MenuItem onClick={onClick} disabled={disabled}>
      <ListItemText>
        <FormattedMessage id={title} />
      </ListItemText>
    </MenuItem>
  )
}

const TaskMenu: React.FC<{
  onEdit: () => void,
  onCRM: () => void,
  row: TaskDescriptor,
}> = ({ onEdit, onCRM, row }) => {
  const Popover = useTableCellPopover();

  return (
    <>
      <Popover.Delegate>
        <MenuList dense>
          <CellMenuItem onClick={onEdit} title={`tasktable.menu.edit`} />
          <CellMenuItem onClick={onCRM} title={`tasktable.menu.viewData`} disabled={!!!row.customerId} />
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

export const TaskRowMenu: React.FC<{
  row: TaskDescriptor
}> = ({ row }) => {
  const { hoverItemActive: active, onEndHover: setDisabled } = useXTableRow();

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
    <>
      <TaskEditDialog open={edit} onClose={handleEndEdit} task={row} />
      <Customer.CustomerDetailsDialog open={crm} onClose={handleCrm} customer={row.customerId} />
      {active && <TaskMenu row={row} onEdit={handleStartEdit} onCRM={handleStartWork} />}
    </>
  );
}

