import React from 'react';
import { Autocomplete, Checkbox, TextField } from '@mui/material';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';


const options = ['cat', 'camel', 'dog', 'fish', 'parrot', 'papaya', 'lizard']

// single choice
export const GInputAutoComplete1: React.FC = () => {


  return (<Autocomplete
    disablePortal
    options={options}
    value={options[1]}
    renderInput={(params) => <TextField {...params} hiddenLabel />}
  />)
}





const icon = <CheckBoxOutlineBlankIcon fontSize="small" />;
const checkedIcon = <CheckBoxIcon fontSize="small" />;

// multi-choice
export const GInputAutoComplete: React.FC = () => {
  return (<Autocomplete
    disablePortal
    options={options}
    disableCloseOnSelect
    multiple={true}
    renderInput={(params) => <TextField {...params} hiddenLabel />}
    renderOption={(props, option, { selected }) => {
      const { key, ...optionProps } = props;
      return (
        <li key={key} {...optionProps}>
          <Checkbox
            icon={icon}
            checkedIcon={checkedIcon}
            style={{ marginRight: 8 }}
            checked={selected}
          />
          {option}
        </li>
      );
    }}
  />)
}


