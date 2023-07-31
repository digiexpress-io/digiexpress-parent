import React from 'react';
import { SxProps } from '@mui/material';
import { useIntl } from 'react-intl';

import client from '@taskclient';
import Styles from '@styles';
import TaskCell from './TaskCell';
import { usePopover } from './CellPopover';
import { CellProps } from './task-table-types';



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
    <Styles.TableCell width="100px" sx={getStatus(def)}><Status row={row} def={def} /></Styles.TableCell>
  );
}

export default FormattedCell;

