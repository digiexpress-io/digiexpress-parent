import React from 'react';

import { Typography } from '@mui/material';

import { Header } from '@tanstack/react-table';
import { useIntl } from 'react-intl';

import { FilterByStringSlot, useUtilityClasses } from './useUtilityClasses';
import { useHeaderSearchState } from './useHeaderSearchState';
import { DatePicker } from '@/date-picker';
import { DateTime } from 'luxon';



function getFilterValue(value: any) {
  try {
    if(!value) {
      return null;
    }

    const result = new Date(value);
    if(DateTime.fromJSDate(result).isValid) {
      return result;
    }
    return null;
  } catch(e) {
    return null;
  }
}

export interface ToolHeaderSearchDateProps {
  header: Header<unknown, unknown>;
}

export const ToolHeaderSearchDate: React.FC<ToolHeaderSearchDateProps> = ({ header }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const search = useHeaderSearchState(header);
  const title: string = header.column.columnDef.header?.toString().toLowerCase() ?? '';
  

  function handleDateChange(newValue: Date | null) {
    header.column.setFilterValue(newValue);
  }

  const currentFilter = getFilterValue(header.column.getFilterValue());
  
  if (header.column.getCanFilter() === false) {
    return (<></>) // hide filter icon to prevent opening the menu popover
  }

  return (
    <>
      <FilterByStringSlot className={classes.filterByString}>
        <Typography>{intl.formatMessage({ id: 'eveli.table.menu.filter.filterByDateGte', defaultMessage: 'Items after or on ' })}{title}</Typography>
        <DatePicker value={currentFilter} onChange={handleDateChange} inline/>
      </FilterByStringSlot>
    </>
  );
}

