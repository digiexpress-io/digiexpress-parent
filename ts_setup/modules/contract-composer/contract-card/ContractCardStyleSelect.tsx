import React from 'react';
import { useIntl } from 'react-intl';
import { FormControl, InputLabel, MenuItem, Select, SelectChangeEvent } from '@mui/material';
import { ContractCardStyleKey } from './CardConfigContext';



export interface ContractCardStylerProps {
  value: ContractCardStyleKey;
  onChange: (value: ContractCardStyleKey) => void;
}

export const ContractCardStyleSelect: React.FC<ContractCardStylerProps> = ({ value, onChange }) => {
  const intl = useIntl();
  const handleChange = (event: SelectChangeEvent) => {
    onChange(event.target.value as ContractCardStyleKey);
  };

  return (
    <FormControl fullWidth sx={{ mb: 2, maxWidth: 300 }}>
      <InputLabel>
        {intl.formatMessage({ id: 'contractcard.cardStyle', defaultMessage: 'Card Style' })}
      </InputLabel>
      <Select
        value={value}
        label={intl.formatMessage({ id: 'contractcard.cardStyle', defaultMessage: 'Card Style' })}
        onChange={handleChange}
      >
        <MenuItem value="COMPACT">
          {intl.formatMessage({ id: 'contractcard.style.COMPACT' })}
        </MenuItem>
        <MenuItem value="DEFAULT">
          {intl.formatMessage({ id: 'contractcard.style.DEFAULT' })}
        </MenuItem>
        <MenuItem value="LARGE">
          {intl.formatMessage({ id: 'contractcard.style.LARGE' })}
        </MenuItem>
      </Select>
    </FormControl>
  );
};