import React from 'react';
import { Box } from '@mui/material';

import client from '@taskclient';
import Styles from '@styles';
import TaskCell from './TaskCell';
import { usePopover } from './CellPopover';
import { CellProps } from './task-table-types';



const FormattedCell: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  def: client.Group,
  children: React.ReactNode
}> = ({ row, def }) => {

  return (
    <Styles.TableCell width="500px">
      <Box width='500px' justifyContent='left' display='flex'>
        <TaskCell id={row.id + "/Subject"} name={row.title} maxWidth={"500px"} />
      </Box>
    </Styles.TableCell>
  );

}

export default FormattedCell;

