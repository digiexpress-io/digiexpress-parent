import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { TextField } from '@mui/material';
import React from "react";

export const TableDateFilter = (props:any) => {
  const [date, setDate] = React.useState<Date|null>(null);

  return (
      <DatePicker
        format="dd.MM.yyyy"
        value={date}
        onChange={(event:any) => {
          setDate(event);
          props.onFilterChanged(props.columnDef.tableData.id, event);
        }}
        slots={{textField: textFieldProps => {return (<TextField
          {...textFieldProps}
          sx={{ m: "normal" }}
          id="table-date-filter-dialog"
          />)}}}
      />
  );
};
