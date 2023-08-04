import React from 'react';
import { IconButton, SxProps, Box, MenuList, MenuItem, ListItemText, Divider } from '@mui/material';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';

import { FormattedMessage } from 'react-intl';

import client from '@taskclient';
import Styles from '@styles';
import TaskOps from '../TaskOps';
import { usePopover } from './CellPopover';
import { CellProps } from './task-table-types';



const iconButtonSx: SxProps = { fontSize: 'small', color: 'uiElements.main', p: 0.5 }





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
            <ListItemText>OPTION6</ListItemText>
          </MenuItem>
        </MenuList>
      </Popover.Delegate>
      <IconButton sx={iconButtonSx} onClick={Popover.onClick}>
        <MoreHorizOutlinedIcon fontSize='small' />
      </IconButton>
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
      <TaskOps.EditTaskDialog open={edit} onClose={handleEndEdit} task={row} />

      {active && <HoverMenu onEdit={handleStartEdit} />}
    </Box>
  </Styles.TableCell>);
}

export default FormattedCell;

