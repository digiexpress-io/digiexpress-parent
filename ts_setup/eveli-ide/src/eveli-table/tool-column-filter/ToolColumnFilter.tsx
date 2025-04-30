import React from 'react';
import { Box, generateUtilityClass, styled, Typography } from '@mui/material';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';

import composeClasses from '@mui/utils/composeClasses';
import { Column, Table } from '@tanstack/react-table';
import { useIntl } from 'react-intl';
import { useHeaderItems } from '../tool-header-search';


export interface ToolColumnFilterProps {
  table: Table<any>;
}


export const ToolColumnFilter: React.FC<ToolColumnFilterProps> = (props) => {
  const classes = useUtilityClasses();

  const [expandedId, setExpandedId] = React.useState<string | undefined>(undefined);

  function toggleExpanded(columnId: string) {
    setExpandedId(prev => (prev === props.table.getColumn(columnId)?.id ? undefined : props.table.getColumn(columnId)?.id))
  }

  function handleClearFilters(event: React.MouseEvent<HTMLElement>, columnId: string) {
    event.stopPropagation();
    const col = props.table.getColumn(columnId);
    if (col) {
      col.setFilterValue(undefined);
    }
  }


  function toggleFilter(event: React.MouseEvent<HTMLElement>, col: Column<any, unknown>, value: string) {
    event.stopPropagation();
    const prev = col.getFilterValue() as string[] ?? [];
    const next = prev.includes(value)
      ? prev.filter(v => v !== value)
      : [...prev, value];
    col.setFilterValue(next.length > 0 ? next : undefined);
  }

  return (
    <Root className={classes.root}>
      {props.table.getAllFlatColumns()
        .filter(col => col.columnDef.meta?.enableSelection)
        .map(col => <ColumnFilter
          key={col.id}
          col={col}
          enabled={expandedId === col.id}
          onToggleFilter={(event, value) => toggleFilter(event, col, value)}
          onClearAll={(event) => handleClearFilters(event, col.id)}
          onExpandToggle={() => toggleExpanded(col.id)} />)}
    </Root>
  )
}


const ColumnFilter: React.FC<{
  col: Column<any, unknown>,
  enabled: boolean,
  onExpandToggle: () => void,
  onClearAll: (event: React.MouseEvent<HTMLElement>) => void,
  onToggleFilter: (event: React.MouseEvent<HTMLElement>, value: string) => void,
}> = ({
  col, enabled, onExpandToggle, onClearAll, onToggleFilter
}) => {

  const intl = useIntl();
  const filterTitle = col.columnDef.header?.toString() ? col.columnDef.header.toString() : 'No header';

  const uniqueValues = useHeaderItems(col);
  const currentFilter = col.getFilterValue();
  const classes = useUtilityClasses();

  return (
    <ColumnFilterSlot className={classes.columnFilter} onClick={onExpandToggle}>
      <Box className='filters-icon-alignment'>
        <KeyboardArrowDownIcon className='filters-select-checkmark-icon' />
        <Typography className='filters-title-typography'>{filterTitle}</Typography>
      </Box>
      {enabled && (
        <ColumnFilterSelectionSlot className={classes.columnFilterSelection}>
          <Box display='flex' onClick={onClearAll}>
            {currentFilter === undefined ? <CheckBoxIcon className='filters-select-checkmark-icon' /> : <CheckBoxOutlineBlankIcon className='filters-select-checkmark-icon' />}
            {intl.formatMessage({ id: 'eveli.table.menu.filter.showAllItems', defaultMessage: 'Show all items ' })}
          </Box>

          {uniqueValues.map((value, index) => (
            <Box className='filters-icon-alignment' onClick={(event: React.MouseEvent<HTMLElement>) => onToggleFilter(event, value)} key={index}>
              {Array.isArray(currentFilter) && currentFilter.includes(value) ? <CheckBoxIcon className='filters-select-checkmark-icon' /> : <CheckBoxOutlineBlankIcon className='filters-select-checkmark-icon' />}
              {value}
            </Box>
          ))}
        </ColumnFilterSelectionSlot>
      )}
    </ColumnFilterSlot>
  )
}

const FiltersRootClassName = 'EveliTableDrawerFilters';


const Root = styled('div', {
  name: FiltersRootClassName,
  slot: 'DrawerFiltersSelect',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {
    width: '100%',
    padding: theme.spacing(1),
    gap: theme.spacing(1),
    display: 'flex',
    alignItems: 'left',
    flexDirection: 'column'
  };
});


const ColumnFilterSelectionSlot = styled('div', {
  name: FiltersRootClassName,
  slot: 'ColumnFilterSelection',
})(({ theme }) => {

  return {
    display: 'flex',
    flexDirection: 'column',
    padding: theme.spacing(1),
    marginLeft: theme.spacing(1),
  };

});


const ColumnFilterSlot = styled('div', {
  name: FiltersRootClassName,
  slot: 'ColumnFilter',

})(({ theme }) => {

  return {
    marginLeft: theme.spacing(1),
    '.filters-select-checkmark-icon': {
      marginRight: theme.spacing(2),
      color: theme.palette.primary.main,
      fontSize: 'medium',
    },

    '.filters-icon-alignment': {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'flex-start'
    },

    '.filters-title-typography': {
      fontSize: '10pt'
    },

    ':hover': {
      cursor: 'pointer'
    },

  };
});

const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    columnFilter: ['columnFilter'],
    columnFilterSelection: ['columnFilterSelection']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(FiltersRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}
