import React from 'react';
import { TableRow } from '@mui/material';

import Styles from '@styles';
import Client from '@taskclient';

import * as Cells from './MyWorkTableCells';




const DescriptorTableRow: React.FC<{
  rowId: number,
  row: Client.TaskDescriptor
}> = ({ row }) => {

  return (<TableRow hover tabIndex={-1} key={row.id}>
    <Styles.TaskTable.TableCell width="50px"><Cells.Tools maxWidth="100px" row={row}/></Styles.TaskTable.TableCell>
    <Styles.TaskTable.TableCell width="300px"><Cells.Subject maxWidth="300px" row={row}/></Styles.TaskTable.TableCell>
    <Styles.TaskTable.TableCell width="450px"><Cells.Desc maxWidth="450px" row={row} /></Styles.TaskTable.TableCell>
    <Styles.TaskTable.TableCell rowtype={row.uploads.length === 0 ? undefined : 'large'}><Cells.DueDate row={row}/></Styles.TaskTable.TableCell>
  </TableRow>);
}



const Rows: React.FC<{
  content: Client.TablePagination<Client.TaskDescriptor>,
  loading: boolean
}> = ({ content, loading }) => {
  return (
    <Styles.TaskTable.TableBody>
      {content.entries.map((row, rowId) => (<DescriptorTableRow key={row.id} rowId={rowId} row={row} />))}
      <Styles.TaskTable.TableRowEmpty content={content} loading={loading} plusColSpan={3} />
    </Styles.TaskTable.TableBody>)
}


export default Rows;

