import React from 'react';

import ClearIcon from '@mui/icons-material/Clear';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import { IconButton, InputAdornment, TextField } from '@mui/material';
import { useIntl } from "react-intl";
import { GInputTimeProps } from "./GInputTime";
import { InputHidden } from './InputHidden';
import { GInputTimeInput, useUtilityClasses } from './useUtilityClasses';

function parseInit(value: string | undefined) {
  return value ?? null;
}

export const TimeAndCalendar: React.FC<GInputTimeProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses(props.id, props.variant);
  const [value, setValue] = React.useState<string | null>(parseInit(props.value));

  const { format = 'HH:mm' } = props;
  const ownerState = { variant: props.variant ?? 'time' };

  const step = /s/.test(format) ? 1 : 60;

  return (
    <GInputTimeInput ownerState={ownerState} className={classes.input}>
      <InputHidden time={value} onChange={props.onChange} id={props.id} />

      <TextField
        type="time"
        fullWidth
        size="small"
        value={value ?? ''}
        disabled={props.disabled}
        onChange={(e) => {
          const next = e.target.value || null;
          setValue(next);
        }}
        inputProps={{
          step,
        }}
        InputProps={{
          endAdornment: (
            <InputAdornment position="end">
              <IconButton
                size="small"
                onClick={() => setValue(null)}
                disabled={!value || props.disabled}
                aria-label={intl.formatMessage({ id: 'common.clear', defaultMessage: 'Clear' })}
                edge="end"
              >
                <ClearIcon fontSize="small" />
              </IconButton>
              <AccessTimeIcon fontSize="small" sx={{ ml: 0.5 }} />
            </InputAdornment>
          ),
        }}
        className="MuiInputBase-root"
      />
    </GInputTimeInput>
  );
};
