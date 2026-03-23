import React from 'react';
import { Select, MenuItem, OutlinedInput } from '@mui/material';
import { FsDirentSingleSelectProps } from './FsDirentSingleSelectProps';
import { useUtilityClasses, FsDirentSingleSelectRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentSingleSelect: React.FC<FsDirentSingleSelectProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentSingleSelectRoot className={classes.root} ownerState={ownerState} fullWidth size='small'>
      <Select className={classes.select}
        value={props.value}
        onChange={(e) => props.onChange(e.target.value as string)}
        input={<OutlinedInput />}
      >
        {props.options.map((option) => (
          <MenuItem key={option.value} value={option.value}>
            {option.label}
          </MenuItem>
        ))}
      </Select>
    </FsDirentSingleSelectRoot>
  );
};
