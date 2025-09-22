import * as React from 'react';
import { FormControl, FormLabel } from '@mui/material';
import { DatePicker as XuiDatePicker } from '@dxs-ts/xui-datetime';

type XuiProps = React.ComponentProps<typeof XuiDatePicker>;

export type CustomDatePickerProps = Omit<XuiProps, 'value' | 'onChange'> & {
  /** Allow undefined at call sites; normalize to null for XuiDatePicker */
  value?: Date | null;
  onChange?: (d: Date | null) => void;

  /** Legacy ergonomics from old wrapper */
  label?: React.ReactNode;
  fullWidth?: boolean;
  readonly?: boolean;

  /** Old API: called when user clears the date */
  handleDateClear?: () => void;
  /** Old API: (sometimes used) external error state */
  error?: boolean;
  /** Old API: default small height for compact tables/forms */
  size?: 'small' | 'medium';
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
  ...rest
}) => {
  const normalized = value ?? null;

  const wrappedOnChange = (d: Date | null) => {
    if (d == null) {
      handleDateClear?.();
    }
    onChange?.(d);
  };

  return (
    <FormControl
      sx={{
        minHeight: 72,
        width: fullWidth ? '100%' : 'auto',
        opacity: readonly ? 0.7 : 1,
      }}
    >
      {label ? <FormLabel>{label}</FormLabel> : null}
      <XuiDatePicker
        variant="mui-like"            // opt-in; keeps other screens unchanged by default
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
