import React from 'react';
import { IconButton, SxProps, Box } from '@mui/material';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';

import client from '@taskclient';
import Styles from '@styles';
import TaskCell from './TaskCell';
import { usePopover } from './CellPopover';
import { CellProps } from './task-table-types';



const iconButtonSx: SxProps = { fontSize: 'small', color: 'uiElements.main', p: 0.5 }


const IconButtonWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (<>
    {React.Children.map(children, (el, index) => <IconButton key={index} sx={iconButtonSx}>{el}</IconButton>)}
  </>)
}


const HoverMenu: React.FC<{}> = () => {
  return (<IconButtonWrapper><MoreHorizOutlinedIcon fontSize='small' /></IconButtonWrapper>)
}

const Menu: React.FC<CellProps> = ({ row }) => {
  return (<TaskCell id={row.id + "/Menu"} name={<></>} />);
}


const FormattedCell: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  def: client.Group,
  active: boolean
}> = ({ row, def, active }) => {

  return (<Styles.TableCell width="35px">
    <Box width="35px" justifyContent='center'> {/* Box is needed to prevent table cell resize on hover */}
      {active && <><Menu row={row} def={def} /><HoverMenu /></>}
    </Box>
  </Styles.TableCell>);
}

export default FormattedCell;

