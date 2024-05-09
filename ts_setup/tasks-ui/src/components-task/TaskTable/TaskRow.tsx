import React from 'react';
import { TaskDescriptor } from 'descriptor-task';
import { XTableRow } from 'components-xtable';


export interface TaskRowProps {
  rowId: number;
  row: TaskDescriptor;
  visibleColumns?: string[];
  children: React.ReactNode;
  variant?: 'secondary'
}

export const TaskRow: React.FC<TaskRowProps> = React.memo((props) => {
  return (<XTableRow variant={props.variant}>{props.children}</XTableRow>);
},
(prevProps: Readonly<TaskRowProps>, nextProps: Readonly<TaskRowProps>) => {
  return (
    prevProps.rowId == nextProps.rowId &&
    prevProps.visibleColumns?.filter(x => !nextProps.visibleColumns?.includes(x)).length === 0 &&
    prevProps.visibleColumns.length === nextProps.visibleColumns?.length &&
    prevProps.row.equals(nextProps.row))
  }
)