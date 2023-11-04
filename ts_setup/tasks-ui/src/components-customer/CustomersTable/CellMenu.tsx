import React from 'react';
import { Box, MenuList, MenuItem, ListItemText } from '@mui/material';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';
import { FormattedMessage } from 'react-intl';

import { ProjectDescriptor, Group } from 'descriptor-project';
import { usePopover } from './CellPopover';
import CellHoverButton from './CellMenuButton';
import { StyledTableCell } from './StyledTable';

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
  const Popover = usePopover();

  return (
    <>
      <Popover.Delegate>
        <MenuList dense>
          <CellMenuItem onClick={onEdit} title={`projecttable.menu.edit`} />
        </MenuList>
      </Popover.Delegate>
      <CellHoverButton onClick={Popover.onClick}>
        <MoreHorizOutlinedIcon fontSize='small' />
      </CellHoverButton>
    </>)
}

const FormattedCell: React.FC<{
  rowId: number,
  row: ProjectDescriptor,
  def: Group,
  active: boolean,
  setDisabled: () => void
}> = ({ row, active, setDisabled }) => {


  function handleStartEdit() {
    setDisabled();
  }

  return (
    <StyledTableCell width="35px">
      <Box width="35px" justifyContent='right'> {/* Box is needed to prevent table cell resize on hover */}
        {active && <CellMenu onEdit={handleStartEdit} />}
      </Box>
    </StyledTableCell>
  );
}

export default FormattedCell;

