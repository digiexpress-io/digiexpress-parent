import React from 'react';
import { Box, MenuList, MenuItem, ListItemText, Divider } from '@mui/material';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';

import { FormattedMessage } from 'react-intl';

import client from '@taskclient';
import Styles from '@styles';
import { usePopover } from './CellPopover';
import CellHoverButton from './CellMenuButton';
import TaskEditDialog from 'core/TaskEdit';


const HoverMenu: React.FC<{ onEdit: () => void }> = ({ onEdit }) => {
  const Popover = usePopover();

  return (
    <>
      <Popover.Delegate>
        <MenuList dense>
          <MenuItem onClick={onEdit}>
            <ListItemText>
              <FormattedMessage id={`tasktable.menu.edit`} />
            </ListItemText>
          </MenuItem>
          <MenuItem>
            <ListItemText>OPTION2</ListItemText>
          </MenuItem>
          <MenuItem>
            <ListItemText>OPTION With longer text 3</ListItemText>
          </MenuItem>

          <Divider />
          <MenuItem>
            <ListItemText>OPTION4</ListItemText>
          </MenuItem>
          <MenuItem>
            <ListItemText>OPTION5</ListItemText>
          </MenuItem>
          <Divider />
          <MenuItem>
            <ListItemText>Archive task</ListItemText>
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
  row: client.TaskDescriptor,
  def: client.Group,
  active: boolean,
  setDisabled: () => void
}> = ({ row, def, active, setDisabled }) => {
  const [edit, setEdit] = React.useState(false);

  function handleStartEdit() {
    setEdit(true);
    setDisabled();
  }

  function handleEndEdit() {
    setEdit(false);
  }


  return (<Styles.TableCell width="35px">
    <Box width="35px" justifyContent='right'> {/* Box is needed to prevent table cell resize on hover */}
      <TaskEditDialog open={edit} onClose={handleEndEdit} task={row} />

      {active && <HoverMenu onEdit={handleStartEdit} />}
    </Box>
  </Styles.TableCell>);
}

export default FormattedCell;

