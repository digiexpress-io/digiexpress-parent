import React from 'react';
import { Select, MenuItem, Chip, OutlinedInput } from '@mui/material';
import { FsDirentMultiSelectProps } from './FsDirentMultiSelectProps';
import { useUtilityClasses, FsDirentMultiSelectRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentMultiSelect: React.FC<FsDirentMultiSelectProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentMultiSelectRoot className={classes.root} ownerState={ownerState} fullWidth size='small'>
      <Select className={classes.select} multiple
        value={props.value}
        onChange={(e) => props.onChange(e.target.value as string[])}
        input={<OutlinedInput />}
        renderValue={(selected) => (
          <div className={classes.chipContainer}>
            {(selected as string[]).map((value) => {
              const option = props.options.find(opt => opt.value === value);
              return (
                <Chip key={value}
                  className={classes.chip}
                  label={option?.label}
                  size='small'
                  onDelete={() => props.onChange(props.value.filter(v => v !== value))}
                  onMouseDown={(e) => e.stopPropagation()}
                />
              );
            })}
          </div>
        )}
      >
        {props.options.map((option) => (
          <MenuItem key={option.value} value={option.value} className={classes.menuItem}>
            {option.label}
          </MenuItem>
        ))}
      </Select>
    </FsDirentMultiSelectRoot>
  );
};
