import React from 'react';
import { Select, MenuItem, OutlinedInput, FormControl, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentSelectSingleProps } from './FsDirentSelectSingleProps';
import { useUtilityClasses, FsDirentSelectSingleRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentSelectSingle: React.FC<FsDirentSelectSingleProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  const value = props.options.some(o => o.value === props.value) ? props.value : '';

  return (
    <FsDirentSelectSingleRoot className={classes.root} ownerState={ownerState}>
      <Select className={classes.select}
        value={value}
        displayEmpty
        onChange={(e) => props.onChange(e.target.value as string)}
        input={<OutlinedInput />}
      >
        {props.allowNone && <MenuItem value=''>—</MenuItem>}
        {props.options.map((option) => (
          <MenuItem key={option.value} value={option.value}>
            {option.label}
          </MenuItem>
        ))}
      </Select>
      {ownerState.showRequiredError && (
        <Typography className={classes.requiredMessage}>
          {intl.formatMessage({ id: 'fs.direntSelectSingle.required' })}
        </Typography>
      )}
    </FsDirentSelectSingleRoot>
  );
};
