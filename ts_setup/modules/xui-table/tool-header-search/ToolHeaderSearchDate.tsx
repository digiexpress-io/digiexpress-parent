import React from 'react';

import { FormControl, FormControlLabel, FormLabel, Radio, RadioGroup } from '@mui/material';

import { Header } from '@tanstack/react-table';
import { useIntl } from 'react-intl';

import { FilterByStringSlot, useUtilityClasses } from './useUtilityClasses';
import { DatePicker } from '@dxs-ts/xui-datetime';
import { DateTime } from 'luxon';
import { TableDateFilter } from '../table-api';

const isDateEqualToDate = (date1: Date | null | undefined, date2: Date | null | undefined): boolean => {
  // Both null or undefined means equal
  if ((date1 === null || date1 === undefined) && (date2 === null || date2 === undefined)) {
    return true;
  }
  
  // One is null/undefined, the other isn't
  if (!date1 || !date2) {
    return false;
  }
  
  const normalizedDate1 = new Date(date1.getFullYear(), date1.getMonth(), date1.getDate());
  const normalizedDate2 = new Date(date2.getFullYear(), date2.getMonth(), date2.getDate());
  
  return normalizedDate1.getTime() === normalizedDate2.getTime();
}

function parseDate(raw: any): Date | null {
  if(!raw) {
    return null;
  }

  if(raw.date) {
    const filterDate = new Date(raw.date);
    if (isNaN(filterDate.getTime())) {
      return null;
    }
    
    return filterDate;
  }

  try {
    const filterDate = new Date(raw);
    if (isNaN(filterDate.getTime())) {
      return null;
    }
    return filterDate;
  } catch (e) {
    return null;
  }


}

function getFilterValue(raw: any): TableDateFilter {
  const type = (raw ? raw['type'] : undefined) ?? 'GTE';
  const date = parseDate(raw);
  
  return { date, type };
}

export interface ToolHeaderSearchDateProps {
  header: Header<unknown, unknown>;
}

export const ToolHeaderSearchDate: React.FC<ToolHeaderSearchDateProps> = ({ header }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const currentFilter = getFilterValue(header.column.getFilterValue());
  
  const [currentType, setCurrentType] = React.useState(currentFilter?.type ?? 'GTE');
  const [date, setDate] = React.useState(() => currentFilter?.date);

  function handleDateChange(newValue: Date | null) {
    if(!isDateEqualToDate(newValue, currentFilter?.date)) {
      header.column.setFilterValue({ date: newValue, type: currentType });
    }
    setDate(newValue);
  }

  function handleDateType(type: TableDateFilter['type']) {
    setCurrentType(type);
    header.column.setFilterValue({ date, type });
  }

  if (header.column.getCanFilter() === false) {
    return (<></>) // hide filter icon to prevent opening the menu popover
  }

  return (
    <FilterByStringSlot className={classes.filterByString}>
      <FormControl>
        <FormLabel>{intl.formatMessage({ id: 'eveli.table.menu.filter.title', defaultMessage: 'Date filtering' })}</FormLabel>
        <RadioGroup value={currentType}>
          <FormControlLabel value="EQUAL" control={<Radio onClick={() => handleDateType('EQUAL')} />} label={intl.formatMessage({ id: 'eveli.table.menu.filter.filterByDateEqual', defaultMessage: 'Date is equal' })} />
          <FormControlLabel value="LT" control={<Radio onClick={() => handleDateType('LT')}/>} label={intl.formatMessage({ id: 'eveli.table.menu.filter.filterByDateLt', defaultMessage: 'Date is before' })} />
          <FormControlLabel value="GTE" control={<Radio onClick={() => handleDateType('GTE')} />} label={intl.formatMessage({ id: 'eveli.table.menu.filter.filterByDateGte', defaultMessage: 'Date is greater than or equal' })} />
        </RadioGroup>
      </FormControl>

      <DatePicker value={date} onChange={handleDateChange} sx={{ mx: 4, my: 2 }} />
    </FilterByStringSlot>
  );
}

