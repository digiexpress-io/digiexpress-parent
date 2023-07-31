import React from 'react';
import { TableRow, SxProps, Box, IconButton } from '@mui/material';
import AssistantPhotoTwoToneIcon from '@mui/icons-material/AssistantPhotoTwoTone';
import { useIntl } from 'react-intl';

import client from '@taskclient';
import Styles from '@styles';
import TaskCell from './TaskCell';
import { usePopover } from './CellPopover';
import { CellProps } from './task-table-types';



function getPriority(def: client.Group): SxProps | undefined {
  if (!def.color) {
    return undefined;
  }
  if (def.type === 'priority') {
    const backgroundColor = def.color;
    return { backgroundColor, borderWidth: 0, color: 'primary.contrastText' }
  }
  return undefined;
}

const Priority: React.FC<CellProps & { color?: string }> = ({ row, color }) => {
  const intl = useIntl();
  const value = intl.formatMessage({ id: `tasktable.header.spotlight.priority.${row.priority}` }).toUpperCase();

  const Popover = usePopover();


  return (
    <>
      <Popover.Delegate>
        <Box display='flex' flexDirection='column'>
          <Box>'HIGH'</Box>
          <Box>'NORMAL'</Box>
          <Box>'LOW'</Box>
        </Box>
      </Popover.Delegate>
      <TaskCell id={row.id + "/Priority"} name={<IconButton onClick={Popover.onClick}><AssistantPhotoTwoToneIcon sx={{ fontSize: 'medium', color }} /></IconButton>} />

    </>
  );
}

const FormattedCell: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  def: client.Group
}> = ({ row, def }) => {

  return (<Styles.TableCell width="50px" sx={getPriority(def)}><Priority row={row} def={def}/></Styles.TableCell>);
}

export default FormattedCell;

