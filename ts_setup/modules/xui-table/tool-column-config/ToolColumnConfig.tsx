import React from 'react';
import { alpha, Box, Button, useTheme } from '@mui/material';
import { Root, useUtilityClasses } from './useUtilityClasses';

import { Table } from '@tanstack/react-table';
import { useIntl } from 'react-intl';
import { Column } from './Column';
import { useColumnState } from './useColumnsState';


export const ToolColumnConfig: React.FC<{
  table: Table<any>;
}> = ({ table }) => {

    const classes = useUtilityClasses();
    const intl = useIntl();
    const theme = useTheme();
    const {
      onDragStart, onDragOver, onDragLeave, onDrop,
      onResetSorting, onResetSortingAndVisibility,
      isResetSortingAndVisibilityEnabled, isResetSortingEnabled,
      columns, hoveredIndex,
    } = useColumnState(table);

    return (
      <Root className={classes.root}>
        {columns.filter(col => !!col.id).map((col, index) => (
          <div draggable key={index}
            onDragStart={(e) => onDragStart(e, index)}
            onDragOver={(e) => onDragOver(e, index)}
            onDragLeave={(e) => onDragLeave(e, index)}
            onDrop={(e) => onDrop(e, index)}
            style={{
              border: hoveredIndex === index ? `2px dashed ${theme.palette.primary.main}` : 'none',
              boxShadow: hoveredIndex === index ? `0 0 6px 3px ${alpha(theme.palette.divider, 0.5)}` : undefined,
              borderRadius: theme.shape.borderRadius,
              transform: hoveredIndex === index ? 'scale(0.97)' : 'scale(1)',
              transition: 'transform 0.2s ease, box-shadow 0.2s ease, border 0.1s ease',
            }}
          >
            <Column table={table} column={col} />
          </div>
        ))}

        <Box mt={2} display='flex' flexDirection='column' gap={1}>
          <Button onClick={onResetSortingAndVisibility} disabled={!isResetSortingAndVisibilityEnabled}>
            {intl.formatMessage({ id: 'eveli.table.resetColumnVisbility', defaultMessage: 'Reset column order and visibility' })}
          </Button>
          <Button onClick={onResetSorting} disabled={!isResetSortingEnabled}>
            {intl.formatMessage({ id: 'eveli.table.resetColumnSorting', defaultMessage: 'Reset column sorting' })}
          </Button>
        </Box>
      </Root>
    );
  };

