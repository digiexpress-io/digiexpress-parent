import React from 'react';
import { Table } from '@tanstack/react-table';

import { DndColumns } from './DndColumns';


export interface ToolColumnConfigProps {
  table: Table<any>;
}

export const ToolColumnConfig: React.FC<ToolColumnConfigProps> = ({ table }) => {
  return (<DndColumns table={table}/>)
}
