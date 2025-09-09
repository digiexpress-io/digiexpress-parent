import React from 'react';
import { alpha, Box, Button, Typography, useTheme } from '@mui/material';
import { Root, ColumnSlot, useUtilityClasses } from './useUtilityClasses';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import DragIndicatorIcon from '@mui/icons-material/DragIndicator';
import { Table } from '@tanstack/react-table';
import { useIntl } from 'react-intl';

export interface ToolColumnVisibilityColumnsSlotProps {
  colId: string;
  colTitle: string;
}

export const ToolColumnVisibility: React.FC<{
  table: Table<any>;
  slotProps: {
    columns: ToolColumnVisibilityColumnsSlotProps[];
  };
}> = ({ table, slotProps }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const theme = useTheme();

  const [draggedIndex, setDraggedIndex] = React.useState<number | undefined>(undefined);
  const [columnsOrder, setColumnsOrder] = React.useState(slotProps.columns.map(c => c.colId));
  const [hoveredIndex, setHoveredIndex] = React.useState<number | undefined>(undefined);
  const [originalColsOrder] = React.useState(slotProps.columns.map(c => c.colId));

  const columns = columnsOrder
    .map(colId => {
      const orig = slotProps.columns.find(c => c.colId === colId)!;
      return { ...orig, isVisible: table.getColumn(colId)?.getIsVisible() ?? true };
    });

  function handleDrop(dropIndex: number) {
    if (draggedIndex === undefined || draggedIndex === dropIndex) {
      return
    };

    const newOrder = [...columnsOrder];
    const [moved] = newOrder.splice(draggedIndex, 1);
    newOrder.splice(dropIndex, 0, moved);
    setColumnsOrder(newOrder);
    table.setColumnOrder(newOrder);
    setDraggedIndex(undefined);
    setHoveredIndex(undefined);
  };

  function handleDragStart(index: number) {
    setDraggedIndex(index)
  };

  function handleDragOver(e: React.DragEvent<HTMLDivElement>, index: number) {
    e.preventDefault();
    setHoveredIndex(index)
  };

  function handleResetOriginalCols() {
    table.setColumnOrder(originalColsOrder);
    table.resetColumnVisibility();
    setColumnsOrder(originalColsOrder);
  }

  const currentOrder = columnsOrder;
  const allVisible = table.getState().columnVisibility ? Object.values(table.getState().columnVisibility).every(v => v === true) : true;
  const orderUnchanged = currentOrder.join(',') === originalColsOrder.join(',');
  const resetColsDisabled = allVisible && orderUnchanged;

  return (
    <Root className={classes.root}>
      {columns.map((col, index) => (
        <div draggable
          key={col.colId}
          onDragStart={(e) => {
            e.stopPropagation();
            handleDragStart(index);
          }}
          onDragOver={(e) => {
            e.stopPropagation();
            handleDragOver(e, index)
          }}
          onDragLeave={(e) => {
            e.stopPropagation();
            setHoveredIndex(undefined)
          }}
          onDrop={(e) => {
            e.stopPropagation();
            handleDrop(index)
          }}
          style={{
            border: hoveredIndex === index ? `2px dashed ${theme.palette.primary.main}` : 'none',
            boxShadow: hoveredIndex === index ? `0 0 6px 3px ${alpha(theme.palette.divider, 0.5)}` : undefined,
            borderRadius: theme.shape.borderRadius,
            transform: hoveredIndex === index ? 'scale(0.97)' : 'scale(1)',
            transition: 'transform 0.2s ease, box-shadow 0.2s ease, border 0.1s ease',
          }}
        >
          <Visibility col={col} table={table} isVisible={col.isVisible} />
        </div>
      ))}

      <Box mt={2} />
      <Button
        onClick={handleResetOriginalCols}
        disabled={resetColsDisabled}
      >
        {intl.formatMessage({ id: 'eveli.table.resetColumns', defaultMessage: 'Reset columns' })}
      </Button>
    </Root>
    );
  };

const Visibility: React.FC<{
  col: ToolColumnVisibilityColumnsSlotProps;
  table: Table<any>;
  isVisible: boolean;
}> = ({ col, table, isVisible }) => {
  const classes = useUtilityClasses();

  function handleToggle(e: React.MouseEvent) {
    e.stopPropagation();
    const column = table.getColumn(col.colId);
    if (column) {
      column.toggleVisibility()
    };
  };

  return (
    <ColumnSlot className={classes.columnSlot} onClick={handleToggle}>
      {isVisible ? (
        <CheckBoxIcon className='cols-select-checkmark-icon' />
      ) : (
        <CheckBoxOutlineBlankIcon className='cols-select-checkmark-icon' />
        )}
        <Typography>{col.colTitle}</Typography>
        <Box flex={1} />
      <Box>
          <DragIndicatorIcon sx={{ cursor: 'grab' }} color='primary' />
        </Box>
      </ColumnSlot>
    );
  };
