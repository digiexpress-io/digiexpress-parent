import React from 'react';
import { Table } from '@tanstack/react-table';


export function useColumnState(table: Table<any>) {
  const allColumns = React.useMemo(() => table.getAllColumns().filter(col => col.getCanHide()), []);

  const [draggedIndex, setDraggedIndex] = React.useState<number | undefined>(undefined);
  const [columnsOrder, setColumnsOrder] = React.useState(() => table.getState().columnOrder);
  const [hoveredIndex, setHoveredIndex] = React.useState<number | undefined>(undefined);
  const [originalColsOrder] = React.useState(() => allColumns.map(c => c.id));

  function onDrop(e: React.DragEvent<HTMLDivElement>, dropIndex: number) {
    e.stopPropagation();
    if (draggedIndex === undefined || draggedIndex === dropIndex) {
      return
    };

    // new state
    const newOrder = [...columnsOrder];
    const [moved] = newOrder.splice(draggedIndex, 1);
    newOrder.splice(dropIndex, 0, moved);

    // apply new state
    setColumnsOrder(newOrder);
    setDraggedIndex(undefined);
    setHoveredIndex(undefined);

    // delegate state to tanstack
    table.setColumnOrder(newOrder);
  };

  function onDragStart(e: React.DragEvent<HTMLDivElement>, index: number) {
    e.stopPropagation();
    setDraggedIndex(index)
  }

  function onDragOver(e: React.DragEvent<HTMLDivElement>, index: number) {
    e.stopPropagation();
    e.preventDefault();
    setHoveredIndex(index)
  }

  function onDragLeave(e: React.DragEvent<HTMLDivElement>, index: number) {
    e.stopPropagation();
    setHoveredIndex(undefined);
  }

  function onResetSortingAndVisibility() {
    table.setColumnOrder(originalColsOrder);
    table.resetColumnVisibility();
    setColumnsOrder(originalColsOrder);
  }

  function onResetSorting() {
    table.resetSorting();
  }

  const currentOrder = columnsOrder;
  const allVisible = table.getState().columnVisibility ? Object.values(table.getState().columnVisibility).every(v => v === true) : true;
  const orderUnchanged = currentOrder.join(',') === originalColsOrder.join(',');
  const isResetSortingAndVisibilityEnabled = !(allVisible && orderUnchanged);
  const isResetSortingEnabled = table.getState().sorting.length > 0;


  const columns = columnsOrder
    .map(colId => {
      const orig = allColumns.find(c => c.id === colId)!;
      return { ...orig, isVisible: table.getColumn(colId)?.getIsVisible() ?? true };
    });

  return { onDragStart, onDragOver, onDragLeave, onDrop, onResetSortingAndVisibility, onResetSorting, 
    columns, hoveredIndex, isResetSortingAndVisibilityEnabled, isResetSortingEnabled };
}