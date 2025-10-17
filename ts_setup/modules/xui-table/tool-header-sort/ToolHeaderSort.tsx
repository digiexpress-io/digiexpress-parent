import React from 'react';
import { Divider, IconButton, ListItemIcon, MenuItem } from '@mui/material';
import { MoreVert as MoreVertIcon } from '@mui/icons-material';
import { ArrowUpward as ArrowUpwardIcon } from '@mui/icons-material';
import { ArrowDownward as ArrowDownwardIcon } from '@mui/icons-material';
import { TableChartOutlined as TableChartOutlinedIcon } from '@mui/icons-material';
import { RestartAlt as RestartAltIcon } from '@mui/icons-material';
import { NotInterested as NotInterestedIcon } from '@mui/icons-material';

import { useIntl } from 'react-intl';
import { Header, Table } from '@tanstack/react-table';
import { MenuSlot, Root, useUtilityClasses } from './useUtilityClasses';

export interface ToolHeaderSortProps {
  header: Header<any, any>;
  table: Table<any>
  onChooseCols: () => void;
}

export const ToolHeaderSort: React.FC<ToolHeaderSortProps> = ({ table, header, onChooseCols }) => {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const intl = useIntl();
  const column = header.column;
  
  function handleClick(event: React.MouseEvent<HTMLButtonElement>) {
    setAnchorEl(event.currentTarget);
  };
  function handleClose() {
    setAnchorEl(null);
  };

  function handleChooseCols() {
    onChooseCols();
    handleClose();
  }

  function handleSortAsc() {
    column.toggleSorting(false)
    handleClose();
  }

  function handleSortDesc() {
    column.toggleSorting(true)
    handleClose();
  }

  function handleClearSorting() {
    column.clearSorting()
    handleClose();
  }

  function handleClearColVisibility() {
    table.resetColumnVisibility();
    handleClose();
  }

  const classes = useUtilityClasses();
  return (
    <Root className={classes.root}>
      <IconButton onClick={handleClick} disableRipple disableFocusRipple>
        <MoreVertIcon />
      </IconButton>

      <MenuSlot anchorEl={anchorEl} open={open} onClose={handleClose}>

        <MenuItem onClick={handleSortAsc}>
          <ListItemIcon><ArrowUpwardIcon className='menu-icon' /></ListItemIcon>
          {intl.formatMessage({ id: 'eveli.table.menu.sort.ascending', defaultMessage: 'Sort ascending' })}
        </MenuItem>

        <MenuItem onClick={handleSortDesc}>
          <ListItemIcon><ArrowDownwardIcon className='menu-icon' /></ListItemIcon>
          {intl.formatMessage({ id: 'eveli.table.menu.sort.descending', defaultMessage: 'Sort descending' })}
        </MenuItem>

        <MenuItem onClick={handleClearSorting}>
          <ListItemIcon><NotInterestedIcon className='menu-icon' /></ListItemIcon>
          {intl.formatMessage({ id: 'eveli.table.menu.sort.clearSorting', defaultMessage: 'Clear sorting' })}
        </MenuItem>

        <Divider />

        <MenuItem onClick={handleChooseCols}>
          <ListItemIcon><TableChartOutlinedIcon className='menu-icon' /></ListItemIcon>
          {intl.formatMessage({ id: 'eveli.table.menu.sort.chooseCols', defaultMessage: 'Choose columns' })}

        </MenuItem>
        <MenuItem onClick={handleClearColVisibility}>
          <ListItemIcon><RestartAltIcon className='menu-icon' /></ListItemIcon>
          {intl.formatMessage({ id: 'eveli.table.menu.sort.colsReset', defaultMessage: 'Reset columns' })}
        </MenuItem>
      </MenuSlot>
    </Root>
  );
}
