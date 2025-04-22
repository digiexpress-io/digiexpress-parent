import React from 'react';
import { EveliTableRowRoot, useUtilityClasses } from './useUtilityClasses';
import { flexRender, Row } from '@tanstack/react-table';
import { EveliTableCell } from './EveliTableCell';

export const EveliTableRow: React.FC<{ children: Row<unknown> }> = ({ children }) => {
  const classes = useUtilityClasses();

  return (
    <EveliTableRowRoot className={classes.root}>
      {children.getVisibleCells().map(cell => (<EveliTableCell key={cell.id} cell={cell} children={flexRender(cell.column.columnDef.cell, cell.getContext())} />))}
    </EveliTableRowRoot>
  )
}