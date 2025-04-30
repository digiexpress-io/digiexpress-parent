import * as React from 'react';
import { Badge, IconButton, InputAdornment, ListItemIcon, MenuItem, TextField, Typography } from '@mui/material';
import FilterListIcon from '@mui/icons-material/FilterList';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import SearchIcon from '@mui/icons-material/Search';

import { Header } from '@tanstack/react-table';
import { useIntl } from 'react-intl';

import { FilterByStringSlot, MenuSlot, Root, useUtilityClasses } from './useUtilityClasses';
import { useAnchor } from './useAnchor';
import { useHeaderSearchState } from './useHeaderSearchState';
import { useHeaderItems } from './useHeaderItems';


export interface EveliTableFilterAndSearchProps {
  header: Header<unknown, unknown>;
}

export const ToolHeaderSearch: React.FC<EveliTableFilterAndSearchProps> = ({ header }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const anchor = useAnchor();
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

  return (
    <Root className={classes.root}>
      <IconButton onClick={anchor.handleClick} disableRipple disableFocusRipple>
        {search.isApplied ? <Badge overlap="circular" variant="dot"><FilterListIcon /></Badge> : <FilterListIcon />}
      </IconButton>

      <MenuSlot className={classes.menu} anchorEl={anchor.anchorEl} open={anchor.open} onClose={anchor.handleClose} action={() => { }}>
        <FilterByStringSlot className={classes.filterByString}>
          <Typography>{intl.formatMessage({ id: 'eveli.table.menu.filter.filterBy', defaultMessage: 'Filter by ' })}{title}</Typography>
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


        <MenuItem onClick={handleClearFilters} >
          <ListItemIcon>
            {header.column.getFilterValue() === undefined ? <CheckBoxIcon className='filters-icon' /> : <CheckBoxOutlineBlankIcon className='filters-icon' />}
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


      </MenuSlot>
    </Root>
  );
}

