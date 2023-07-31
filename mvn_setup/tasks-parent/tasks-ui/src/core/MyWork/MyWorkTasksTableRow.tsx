import React from 'react';
import { TableRow, SxProps, Box } from '@mui/material';

import client from '@taskclient';
import Styles from '@styles';
import * as Cells from './MyWorkTasksTableCells';
import { HoverOptions, HoverMenu } from './HoverOptions';

function getStatus(def: client.Group): SxProps | undefined {
  if (!def.color) {
    return undefined;
  }
  if (def.type === 'status') {
    const backgroundColor = def.color;
    return { backgroundColor, borderWidth: 0, color: 'primary.contrastText' }
  }
  return undefined;
}

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


function getAssignees(def: client.Group): SxProps | undefined {
  if (!def.color) {
    return undefined;
  }
  return undefined;
}


const DescriptorTableRow: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  def: client.Group
}> = ({ row, def }) => {
  
  const [hoverItemsActive, setHoverItemsActive] = React.useState(false);

  return (<TableRow hover tabIndex={-1} key={row.id} onMouseEnter={() => setHoverItemsActive(true)} onMouseLeave={() => setHoverItemsActive(false)}>
    <Styles.TaskTable.TableCell width="500px">
      <Box width='500px' justifyContent='left' display='flex'>
        <Cells.Subject maxWidth="500px" row={row} def={def} />
        {hoverItemsActive && <HoverOptions active={hoverItemsActive} />}
      </Box>
    </Styles.TaskTable.TableCell>
    <Styles.TaskTable.TableCell width="150px" sx={getAssignees(def)}><Cells.Assignees row={row} def={def} /></Styles.TaskTable.TableCell>
    <Styles.TaskTable.TableCell width="100px"><Cells.DueDate row={row} def={def} /></Styles.TaskTable.TableCell>
    <Styles.TaskTable.TableCell width="50px" sx={getPriority(def)}><Cells.Priority row={row} def={def} color={def?.color} /></Styles.TaskTable.TableCell>
    <Styles.TaskTable.TableCell width="100px" sx={getStatus(def)}><Cells.Status row={row} def={def} /></Styles.TaskTable.TableCell>
    <Styles.TaskTable.TableCell width="35px">
      <Box width="35px" justifyContent='center'> {/* Box is needed to prevent table cell resize on hover */}
        {hoverItemsActive && <><Cells.Menu row={row} def={def} /><HoverMenu /></>}
      </Box>
    </Styles.TaskTable.TableCell>
  </TableRow>);
}



export default DescriptorTableRow;

