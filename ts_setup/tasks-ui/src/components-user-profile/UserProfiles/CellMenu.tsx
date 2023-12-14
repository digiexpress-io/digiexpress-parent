import React from 'react';
import { Box, MenuList, MenuItem, ListItemText } from '@mui/material';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';
import { FormattedMessage } from 'react-intl';

import { UserProfileDescriptor } from 'descriptor-user-profile';
import { UserProfileSearchState } from './table-ctx';

import { useTableCellPopover, StyledTableCell } from 'components-generic';
import CellHoverButton from './CellMenuButton';
import SelectedUserProfileDialog from './SelectedUserProfileDialog';

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
  onViewUser: () => void,
}> = ({ onViewUser }) => {
  const Popover = useTableCellPopover();

  return (
    <>
      <Popover.Delegate>
        <MenuList dense>
          <CellMenuItem onClick={onViewUser} title={`userprofileTable.menu.viewUser`} />
        </MenuList>
      </Popover.Delegate>
      <CellHoverButton onClick={Popover.onClick}>
        <MoreHorizOutlinedIcon fontSize='small' />
      </CellHoverButton>
    </>)
}

const FormattedCell: React.FC<{
  rowId: number,
  row: UserProfileDescriptor,
  def: UserProfileSearchState,
  active: boolean,
  setDisabled: () => void
}> = ({ row, active, setDisabled }) => {
  const [userDialogOpen, setUserDialogOpen] = React.useState(false);

  function handleUserDialog() {
    setUserDialogOpen(prev => !prev);
    setDisabled();
  }




  return (<>
    <SelectedUserProfileDialog open={userDialogOpen} onClose={handleUserDialog} />
    <StyledTableCell width="35px">
      <Box width="35px" justifyContent='right'> {/* Box is needed to prevent table cell resize on hover */}
        {active && <CellMenu onViewUser={handleUserDialog} />}
      </Box>
    </StyledTableCell>
  </>
  );
}

export default FormattedCell;

