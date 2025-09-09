import React from 'react';
import { Table } from '@tanstack/react-table';

import { ToolColumnVisibility } from './ToolColumnVisibility';


export interface ToolColumnVisibilitySelectionProps {
  table: Table<any>;
}



export const ToolColumnVisibilitySelection: React.FC<ToolColumnVisibilitySelectionProps> = ({ table }) => {
  const allColumns = table.getAllColumns().filter(col => col.getCanHide());;

  return (
    <ToolColumnVisibility
      table={table}
      slotProps={{
        columns: allColumns.map((col) => ({
          colTitle: col.columnDef.header?.toString() || col.id,
          isVisible: col.getIsVisible(),
          onToggle: (newValue) => col.toggleVisibility(newValue)
        }))
      }} />
  )
}
