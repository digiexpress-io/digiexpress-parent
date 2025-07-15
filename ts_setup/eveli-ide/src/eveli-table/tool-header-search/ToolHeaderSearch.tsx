import React from 'react';
import { Badge, IconButton } from '@mui/material';
import FilterListIcon from '@mui/icons-material/FilterList';

import { Header } from '@tanstack/react-table';

import { MenuSlot, Root, useUtilityClasses } from './useUtilityClasses';
import { useAnchor } from './useAnchor';
import { useHeaderSearchState } from './useHeaderSearchState';
import { ToolHeaderSearchString } from './ToolHeaderSearchString';
import { ToolHeaderSearchDate } from './ToolHeaderSearchDate';


export interface EveliTableFilterAndSearchProps {
  header: Header<unknown, unknown>;
}

export const ToolHeaderSearch: React.FC<EveliTableFilterAndSearchProps> = ({ header }) => {
  const classes = useUtilityClasses();
  const anchor = useAnchor();
  const search = useHeaderSearchState(header);

  if (header.column.getCanFilter() === false) {
    return (<></>) // hide filter icon to prevent opening the menu popover
  }

  const isDate = header.column.columnDef.meta?.enableDateGTE;

  return (
    <Root className={classes.root}>
      <IconButton onClick={anchor.handleClick} disableRipple disableFocusRipple>
        {search.isApplied ? <Badge overlap='circular' variant='dot'><FilterListIcon /></Badge> : <FilterListIcon />}
      </IconButton>

      <MenuSlot className={classes.menu} anchorEl={anchor.anchorEl} open={anchor.open} onClose={anchor.handleClose} action={() => { }}>
        {isDate ? <ToolHeaderSearchDate header={header}/> : <ToolHeaderSearchString header={header}/>}
      </MenuSlot>
    </Root>
  );
}

