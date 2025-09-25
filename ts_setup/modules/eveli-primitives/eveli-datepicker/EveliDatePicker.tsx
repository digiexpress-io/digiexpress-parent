import * as React from 'react';
import { FormControl, FormLabel } from '@mui/material';
import { DateTime } from 'luxon';
import { DatePicker as XuiDatePicker } from '@dxs-ts/xui-datetime';

type XuiProps = React.ComponentProps<typeof XuiDatePicker>;

export type EveliDatePickerProps = Omit<XuiProps, 'value' | 'onChange'> & {
  /** Slot contract: allow string | Date | null | undefined */
  value?: string | Date | null;
  onChange?: (d: Date | null) => void;

  /** Legacy ergonomics from old wrapper */
  label?: React.ReactNode;
  fullWidth?: boolean;
  readonly?: boolean;

  /** Slots often pass this through; accept it to satisfy ElementType signature */
  onKeyDown?: React.KeyboardEventHandler;
};

export const EveliDatePicker: React.FC<EveliDatePickerProps> = ({
  label,
  fullWidth = true,
  size = 'small',
  value,
  onChange,
  readonly,
  onKeyDown,
  ...rest
}) => {
  // Normalize to Date | null for xui picker
  const normalized: Date | null =
    typeof value === 'string'
      ? (value ? DateTime.fromISO(value).toJSDate() : null)
      : (value ?? null);

  return (
    <FormControl
      sx={{ minHeight: 72, width: fullWidth ? '100%' : 'auto', opacity: readonly ? 0.7 : 1 }}
      onKeyDown={onKeyDown}
    >
      {label ? <FormLabel>{label}</FormLabel> : null}
      <XuiDatePicker
        fullWidth={fullWidth}
        size={size}
        value={normalized}
        onChange={readonly ? () => {} : (d) => onChange?.(d)}
        sx={{ pointerEvents: readonly ? 'none' : 'auto' }}
        {...rest}
      />
    </FormControl>
  );
};

export default EveliDatePicker;
