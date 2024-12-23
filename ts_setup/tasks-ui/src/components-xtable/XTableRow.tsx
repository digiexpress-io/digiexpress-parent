import React from 'react';
import { TableRow } from '@mui/material';
import { cyan_mud, grey_light_2 } from 'components-colors';
import { useXTableRow } from './XTableRowContext';
import { useXTableBody } from './XTableBodyContext';
import { XTableBodyCell } from './XTableBodyCell';
import { XCollpase } from './XPander';

export interface XTableRowProps {
  children: React.ReactNode;
  variant?: 'secondary';

}

function getRowBackgroundColor(index: number): string {
  const isOdd = index % 2 === 1;

  if (isOdd) {
    return cyan_mud;
  }
  return 'background.paper';
}

export const XTableRow: React.FC<XTableRowProps> = ({ children, variant }) => {
  const { alternate } = useXTableBody();
  const { rowId, onStartHover, onEndHover } = useXTableRow();
  let backgroundColor: string | undefined = alternate ? getRowBackgroundColor(rowId) : undefined;
  if (variant === 'secondary') {
    backgroundColor = grey_light_2;
  }

  return (
    <TableRow hover onMouseEnter={onStartHover} onMouseLeave={onEndHover} sx={{ backgroundColor }}>
      {children}
    </TableRow>);
}

export const XTableRowCollapse: React.FC<XTableRowProps> = ({ children, variant }) => {

  return (
    <XTableRow variant={variant}>
      <XTableBodyCell colSpan='all' id="collapsable" maxWidth='100%' width='100%'>
        <XCollpase>
          {children}
        </XCollpase>
      </XTableBodyCell>
    </XTableRow>);
}
