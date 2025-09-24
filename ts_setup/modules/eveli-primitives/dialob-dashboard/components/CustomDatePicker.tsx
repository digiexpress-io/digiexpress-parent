import * as React from 'react';
import { FormControl, FormLabel } from '@mui/material';
import { DateTime } from 'luxon';
import { DatePicker as XuiDatePicker } from '@dxs-ts/xui-datetime';

type XuiProps = React.ComponentProps<typeof XuiDatePicker>;

export type CustomDatePickerProps = Omit<XuiProps, 'value' | 'onChange'> & {
  value?: string | Date | null;
  onChange?: (d: Date | null) => void;

  label?: React.ReactNode;
  fullWidth?: boolean;
  readonly?: boolean;

  handleDateClear?: () => void;
  error?: boolean;
  size?: 'small' | 'medium';

  onKeyDown?: React.KeyboardEventHandler;
};

export const CustomDatePicker: React.FC<CustomDatePickerProps> = ({
  label,
  fullWidth = true,
  size = 'small',
  value,
  onChange,
  readonly,
  handleDateClear,
  error,
  onKeyDown,
  ...rest
}) => {
  const normalized: Date | null =
    typeof value === 'string'
      ? (value ? DateTime.fromISO(value).toJSDate() : null)
      : (value ?? null);

  const wrappedOnChange = (d: Date | null) => {
    if (d == null) handleDateClear?.();
    onChange?.(d);
  };

  return (
    <FormControl
      sx={{
        minHeight: 72,
        width: fullWidth ? '100%' : 'auto',
        opacity: readonly ? 0.7 : 1,
      }}
      onKeyDown={onKeyDown}
    >
      {label ? <FormLabel>{label}</FormLabel> : null}
      <XuiDatePicker
        variant="mui-like"
        fullWidth={fullWidth}
        size={size}
        value={normalized}
        error={error}
        onChange={readonly ? () => {} : wrappedOnChange}
        sx={{ pointerEvents: readonly ? 'none' : 'auto' }}
        {...rest}
      />
    </FormControl>
  );
};

export default CustomDatePicker;
