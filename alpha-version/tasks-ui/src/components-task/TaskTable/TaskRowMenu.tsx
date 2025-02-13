import React from 'react';
import { Box, MenuList, MenuItem, ListItemText, Divider, styled, SxProps, IconButton } from '@mui/material';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';
import DeleteIcon from '@mui/icons-material/Delete';
import { FormattedMessage } from 'react-intl';

import { TaskDescriptor } from 'descriptor-task';
import { useTableCellPopover } from 'components-generic';
import { CustomerDetailsDialog } from 'components-customer';


import { TaskEditDialog } from '../TaskEdit';
import { cyan } from '@mui/material/colors';


const iconButtonSx: SxProps = { fontSize: 'small', color: cyan, p: 0.5 }

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

export const TaskRowMenu: React.FC<{
  row: TaskDescriptor,
  children?: React.ReactNode | undefined
}> = ({ row, children }) => {
  const Popover = useTableCellPopover();
  const [edit, setEdit] = React.useState(false);
  const [crm, setCrm] = React.useState(false);

  function handleStartEdit() {
    setEdit(true);
  }

  function handleEndEdit() {
    setEdit(false);
  }

  function handleStartWork() {
    setCrm(true);
  }
  function handleCrm() {
    setCrm(false);
  }

  const button = children ? children : (<IconButton sx={iconButtonSx}><MoreHorizOutlinedIcon fontSize='small' /></IconButton>);
  return (
    <>
      <TaskEditDialog open={edit} onClose={handleEndEdit} task={row} />
      <CustomerDetailsDialog open={crm} onClose={handleCrm} customer={row.customerId} />

      <Popover.Delegate>
        <MenuList dense>
          <CellMenuItem onClick={handleStartEdit} title={`tasktable.menu.edit`} />
          <CellMenuItem onClick={handleStartWork} title={`tasktable.menu.viewData`} disabled={!!!row.customerId} />
          <Divider />
          <MenuItem>
            <StyledBox>
              <DeleteIcon />
              <FormattedMessage id={`tasktable.menu.archive`} />
            </StyledBox>
          </MenuItem>
        </MenuList>
      </Popover.Delegate>

      <Box onClick={Popover.onClick}>{ button }</Box>
    </>
  );
}

