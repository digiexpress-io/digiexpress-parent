import React from 'react';
import { TableRow as MTableRow } from '@mui/material';

import { SxProps } from '@mui/system';

import { cyan_mud } from 'components-colors';
import { TaskDescriptor } from 'descriptor-task';

import { ColumnName } from '../TableContext';
import CellMenu from '../TableCells/CellMenu';
import CellTitle from '../TableCells/CellTitle';
import CellAssignees from '../TableCells/CellAssignees';
import CellDueDate from '../TableCells/CellDueDate';
import CellPriority from '../TableCells/CellPriority';
import CellStatus from '../TableCells/CellStatus';
import CellRoles from '../TableCells/CellRoles';


function getRowBackgroundColor(index: number): SxProps {
  const isOdd = index % 2 === 1;

  if (isOdd) {
    return { backgroundColor: cyan_mud };
  }
  return { backgroundColor: 'background.paper' };
}


interface RowProps {
  rowId: number;
  row: TaskDescriptor;
  columns: ColumnName[];
}

export const TableRow: React.FC<RowProps> = React.memo((props) => {

  const [hoverItemsActive, setHoverItemsActive] = React.useState(false);
  function handleEndHover() {
    setHoverItemsActive(false);
  }
  function handleStartHover() {
    setHoverItemsActive(true);
  }
  const sx = getRowBackgroundColor(props.rowId);

  const cells = React.useMemo(() => {
    return (<>
    {props.columns.includes("title") && <CellTitle rowId={props.rowId} row={props.row} />}
    {props.columns.includes("assignees") && <CellAssignees rowId={props.rowId} row={props.row} />}
    {props.columns.includes("dueDate") && <CellDueDate rowId={props.rowId} row={props.row} />}
    {props.columns.includes("priority") && <CellPriority rowId={props.rowId} row={props.row} />}
    {props.columns.includes("roles") && <CellRoles rowId={props.rowId} row={props.row} />}
    {props.columns.includes("status") && <CellStatus rowId={props.rowId} row={props.row} />}
    </>);
  }, [props]);

  return (<MTableRow sx={sx} hover tabIndex={-1} key={props.row.id} onMouseEnter={handleStartHover} onMouseLeave={handleEndHover}>
    {cells}
    <CellMenu {...props} active={hoverItemsActive} setDisabled={handleEndHover} />
  </MTableRow>);
},
(prevProps: Readonly<RowProps>, nextProps: Readonly<RowProps>) => {
  return (
    prevProps.rowId == nextProps.rowId &&
    prevProps.columns.filter(x => !nextProps.columns.includes(x)).length === 0 &&
    prevProps.columns.length === nextProps.columns.length &&
    prevProps.row.equals(nextProps.row))
  }
)