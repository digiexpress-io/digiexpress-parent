import React from 'react';
import { Clear as ClearIcon } from '@mui/icons-material';
import { AccessTime as AccessTimeIcon } from '@mui/icons-material';
import { IconButton, InputAdornment, TextField } from '@mui/material';
import { useIntl } from 'react-intl';
import { GInputTimeProps } from './GInputTime';
import { InputHidden } from './InputHidden';
import { useUtilityClasses, GInputTimeInputContainer } from './useUtilityClasses';

function parseInit(value: string | undefined) {
  return value ?? null;
}

export const ReadOnlyTime: React.FC<GInputTimeProps> = (props) => {
  const classes = useUtilityClasses(props.id, props.variant);
  const ownerState = { variant: props.variant ?? 'time' };
  return (
    <GInputTimeInputContainer ownerState={ownerState} className={classes.inputContainer}>
      <TextField type="time" fullWidth value={props.value ?? ''} slotProps={{ input: { readOnly: true } }} />
    </GInputTimeInputContainer>
  );
}

export const TimeAndCalendar: React.FC<GInputTimeProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses(props.id, props.variant);
  const [value, setValue] = React.useState<string | null>(parseInit(props.value));

  const { format = 'HH:mm' } = props;
  const ownerState = { variant: props.variant ?? 'time' };
  const inputRef = React.useRef<HTMLInputElement | null>(null);
  const step = /s/.test(format) ? 1 : 60;

  const openNativePicker = () => {
    const el = inputRef.current;
    if (!el) return;
    if (typeof (el as any).showPicker === 'function') {
      requestAnimationFrame(() => (el as any).showPicker());
      return;
    }
    el.focus();
    el.click();
  };

  return (
    <GInputTimeInputContainer ownerState={ownerState} className={classes.inputContainer}>
      <InputHidden time={value} onChange={props.onChange} id={props.id} />

      <TextField
        type="time"
        fullWidth
        value={value ?? ''}
        disabled={props.disabled}
        inputRef={inputRef}
        onChange={(e) => {
          const next = e.target.value || null;
          setValue(next);
        }}
        inputProps={{ step, className: classes.input }}
        slotProps={{
          input: {
            endAdornment: (
              <InputAdornment position="end" className={classes.endAdornment}>
                <IconButton
                  size="small"
                  onClick={() => setValue(null)}
                  disabled={!value || props.disabled}
                  aria-label={intl.formatMessage({ id: 'common.clear', defaultMessage: 'Clear' })}
                  edge="end"
                  className={classes.clearButton}
                >
                  <ClearIcon fontSize="small" />
                </IconButton>
                <IconButton
                  size="small"
                  onClick={openNativePicker}
                  disabled={props.disabled}
                  aria-label={intl.formatMessage({ id: 'gamut.openTimePicker', defaultMessage: 'Open time picker' })}
                  edge="end"
                  className={classes.timeButton}
                >
                  <AccessTimeIcon fontSize="small" />
                </IconButton>
              </InputAdornment>
            ),
          }
        }}
      />
    </GInputTimeInputContainer>
  );
};
