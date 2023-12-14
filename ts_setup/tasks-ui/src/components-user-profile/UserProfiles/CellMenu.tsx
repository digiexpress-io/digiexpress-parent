import React from 'react';
import { Box, MenuList, MenuItem, ListItemText } from '@mui/material';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';
import { FormattedMessage } from 'react-intl';

import { UserProfileDescriptor } from 'descriptor-user-profile';
import { UserProfileSearchState } from './table-ctx';

import { useTableCellPopover, StyledTableCell } from 'components-generic';
import CellHoverButton from './CellMenuButton';


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
}> = ({ onEdit }) => {
  const Popover = useTableCellPopover();

  return (
    <>
      <Popover.Delegate>
        <MenuList dense>
          <CellMenuItem onClick={onEdit} title={`userprofileTable.menu.viewUser`} />
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
  const [edit, setEdit] = React.useState(false);

  function handleStartEdit() {
    setEdit(true);
    setDisabled();
  }




  return (
    <StyledTableCell width="35px">
      <Box width="35px" justifyContent='right'> {/* Box is needed to prevent table cell resize on hover */}
        {active &&
          <CellMenu
            onEdit={handleStartEdit}
          />}
      </Box>
    </StyledTableCell>
  );
}

export default FormattedCell;

