import React from 'react';
import { InputAdornment, ListItemIcon, MenuItem, TextField, Typography } from '@mui/material';
import { CheckBoxOutlineBlank as CheckBoxOutlineBlankIcon } from '@mui/icons-material';
import { CheckBox as CheckBoxIcon } from '@mui/icons-material';
import { Search as SearchIcon } from '@mui/icons-material';

import { Header } from '@tanstack/react-table';
import { useIntl } from 'react-intl';

import { FilterByStringSlot, useUtilityClasses } from './useUtilityClasses';
import { useHeaderSearchState } from './useHeaderSearchState';
import { useHeaderItems } from './useHeaderItems';


export interface ToolHeaderSearchStringProps {
  header: Header<unknown, unknown>;
}

export const ToolHeaderSearchString: React.FC<ToolHeaderSearchStringProps> = ({ header }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const search = useHeaderSearchState(header);
  const items = useHeaderItems(header.column);

  const title: string = header.column.columnDef.header?.toString().toLowerCase() ?? '';
  
  function handleClearFilters() {
    header.column.setFilterValue(undefined);
  }

  function handleStringChange(e: React.ChangeEvent<HTMLInputElement>) {
    e.preventDefault();
    header.column.setFilterValue(e.target.value);
  }

  function handleArrayChange(selected: string) {
    header.column.setFilterValue(search.nextState(selected));
  }

  function _disableMaterialUIFocusOnUl(event: React.KeyboardEvent<HTMLInputElement>) {
    event.stopPropagation();
  }
  const currentFilter = header.column.getFilterValue();
  const isShowAllChecked = currentFilter === undefined || currentFilter === '' || (Array.isArray(currentFilter) && currentFilter.length === 0);

  if (header.column.getCanFilter() === false) {
    return (<></>) // hide filter icon to prevent opening the menu popover
  }



  return (
    <>
      <FilterByStringSlot className={classes.filterByString}>
        <Typography>{intl.formatMessage({ id: 'eveli.table.menu.filter.filterBy' }, { title })}</Typography>
        <TextField placeholder='Search' value={search.valueAsString ?? ''} onChange={handleStringChange} onKeyDown={_disableMaterialUIFocusOnUl}
          slotProps={{
            input: {
              startAdornment: <InputAdornment position="start">
                <SearchIcon className='filters-adornment-icon' />
              </InputAdornment>
            }
          }}>
          {search.valueAsString ?? ''}
        </TextField>
      </FilterByStringSlot>

      <MenuItem onClick={handleClearFilters}>
        <ListItemIcon>
          {isShowAllChecked ? <CheckBoxIcon className='filters-icon' /> : <CheckBoxOutlineBlankIcon className='filters-icon' />}
        </ListItemIcon>
        {intl.formatMessage({ id: 'eveli.table.menu.filter.showAllItems', defaultMessage: 'Show all items ' })}
      </MenuItem>

      {header.column.columnDef.meta?.enableSelection && items.map((item, index) => <React.Fragment key={index}>
        <MenuItem onClick={() => handleArrayChange(item)}>
          <ListItemIcon>
            {search.valueAsArray.includes(item) ? <CheckBoxIcon className='filters-icon' /> : <CheckBoxOutlineBlankIcon className='filters-icon' />}
          </ListItemIcon>
          {item}
        </MenuItem>
      </React.Fragment>
      )}
    </>
  );
}

