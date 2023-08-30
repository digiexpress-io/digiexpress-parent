import React from 'react';
import { SxProps } from '@mui/material';
import { useIntl } from 'react-intl';

import client from '@taskclient';
import TaskCell from './TaskCell';
import { usePopover } from './CellPopover';
import { CellProps } from './task-table-types';
import { StyledTableCell } from './StyledTable';



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

const Status: React.FC<CellProps> = ({ row }) => {
  const intl = useIntl();
  const value = intl.formatMessage({ id: `tasktable.header.spotlight.status.${row.status}` }).toUpperCase();
  return (<TaskCell id={row.id + "/Status"} name={value} />);
}


const FormattedCell: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  def: client.Group
}> = ({ row, def }) => {

  return (
    <StyledTableCell width="100px" sx={getStatus(def)}><Status row={row} def={def} /></StyledTableCell>
  );
}

export default FormattedCell;

