import React from 'react';
import { TableRow, SxProps } from '@mui/material';

import client from '@taskclient';
import Styles from '@styles';
import * as Cells from './TasksTableCells';



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

function getRoles(def: client.Group): SxProps | undefined {
  if (!def.color) {
    return undefined;
  }

  return undefined;
}

function getOwners(def: client.Group): SxProps | undefined {
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

  return (<TableRow hover tabIndex={-1} key={row.id}>
    <Styles.TaskTable.TableCell width="300px"><Cells.Subject maxWidth="300px" row={row} def={def}/></Styles.TaskTable.TableCell>
    <Styles.TaskTable.TableCell width="150px" sx={getPriority(def)}><Cells.Priority row={row} def={def}/></Styles.TaskTable.TableCell>
    <Styles.TaskTable.TableCell width="200px" sx={getStatus(def)}><Cells.Status row={row} def={def}/></Styles.TaskTable.TableCell>
    <Styles.TaskTable.TableCell width="150px" sx={getOwners(def)}><Cells.Owners row={row} def={def}/></Styles.TaskTable.TableCell>
    <Styles.TaskTable.TableCell sx={getRoles(def)}><Cells.Roles row={row} def={def}/></Styles.TaskTable.TableCell>
    <Styles.TaskTable.TableCell><Cells.DueDate row={row} def={def}/></Styles.TaskTable.TableCell>
  </TableRow>);
}

const Rows: React.FC<{
  content: client.TablePagination<client.TaskDescriptor>,
  def: client.Group,
  loading: boolean
}> = ({ content, def, loading }) => {
  return (
    <Styles.TaskTable.TableBody>
      {content.entries.map((row, rowId) => (<DescriptorTableRow key={row.id} rowId={rowId} row={row} def={def} />))}
      <Styles.TaskTable.TableRowEmpty content={content} loading={loading} plusColSpan={5} />
    </Styles.TaskTable.TableBody>)
}

export default Rows;

