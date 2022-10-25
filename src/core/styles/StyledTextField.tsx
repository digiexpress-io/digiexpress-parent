import React from 'react';
import { TextField, TextFieldProps, FormControl, FormControlProps, InputLabel, Input, InputProps, Box, FormHelperText, InputAdornment } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';

import { styled } from "@mui/material/styles";
import { FormattedMessage } from 'react-intl';


const TextFieldRoot = styled(TextField)<TextFieldProps>(({ theme }) => ({
  marginTop: theme.spacing(2),
  color: theme.palette.primary.contrastText,
  backgroundColor: theme.palette.background.paper,
  '& .MuiOutlinedInput-root': {
    '&.Mui-focused fieldset': {
      borderColor: theme.palette.uiElements.main,
    },
  },
}));

const StyledFormControl = styled(FormControl)<FormControlProps>(({ theme }) => ({
  marginTop: theme.spacing(2),
  color: theme.palette.primary.contrastText,
  backgroundColor: theme.palette.background.paper,
  '& .MuiOutlinedInput-root': {
    '&.Mui-focused fieldset': {
      borderColor: theme.palette.uiElements.main,
    },
  },
}));

const StyledInputProps = styled(Input)<InputProps>(({ theme }) => ({
  marginTop: theme.spacing(2),
  color: theme.palette.primary.contrastText,
  backgroundColor: theme.palette.background.paper,
  '& .MuiOutlinedInput-root': {
    '&.Mui-focused fieldset': {
      borderColor: theme.palette.uiElements.main,
    },
  },
}));



interface StyledTextFieldProps<T> {
  label: string;
  disabled?: boolean;
  helperText?: string;
  value: T;
  placeholder?: T;
  required?: boolean;
  error?: boolean;
  onChange: (newValue: T) => void;
  onEnter?: () => void;
}

const StyledFileField: React.FC<StyledTextFieldProps<string>> = ({ onChange, label, value, required, placeholder, helperText, disabled }) => {
  const title = <FormattedMessage id={label} />;
  return (
    <Box sx={{ mt: 2 }}>
      <InputLabel sx={{ fontWeight: "bold" }}>{title}</InputLabel>
      <StyledFormControl variant="outlined" fullWidth>
        <StyledInputProps
          fullWidth
          disableUnderline
          type="file"
          disabled={disabled}
          onChange={(e) => {
            const file: File = (e.target as any).files[0];
            const enc = new TextDecoder("utf-8");
            file.arrayBuffer().then(d => onChange(enc.decode(d)));
          }}
        />
      </StyledFormControl>
      {helperText ? <FormHelperText><FormattedMessage id={helperText} /></FormHelperText> : null}
    </Box>
  );
}

const StyledTextField: React.FC<StyledTextFieldProps<string>> = ({ onChange, onEnter, label, value, required, placeholder, helperText, disabled, error }) => {
  const helperTextLocalized = helperText ? <FormattedMessage id={helperText} /> : helperText;
  return (
    <TextFieldRoot fullWidth
      disabled={disabled}
      variant="outlined"
      label={<FormattedMessage id={label} />}
      required={required}
      helperText={helperTextLocalized}
      placeholder={placeholder !== null && placeholder !== undefined ? placeholder : ''}
      value={value}
      error={error}
      onChange={({ target }) => onChange(target.value)}
      onKeyPress={onEnter ? (event) => {
        const key = event.key;
        if (key === 'Enter') {
          onEnter();
        }
      } : undefined} />
  );
}

const StyledNumberField: React.FC<StyledTextFieldProps<number>> = ({ onChange, onEnter, label, value, required, placeholder, helperText, disabled, error }) => {
  const helperTextLocalized = helperText ? <FormattedMessage id={helperText} /> : helperText;
  return (
    <TextFieldRoot fullWidth
      disabled={disabled}
      variant="outlined"
      label={<FormattedMessage id={label} />}
      required={required}
      type={"number"}
      helperText={helperTextLocalized}
      placeholder={placeholder + ""}
      value={value}
      error={error}
      onChange={({ target }) => onChange(target.value as any)}
      onKeyPress={onEnter ? (event) => {
        const key = event.key;
        if (key === 'Enter') {
          onEnter();
        }
      } : undefined} />
  );
}

const StyledDateField: React.FC<StyledTextFieldProps<string>> = ({ onChange, onEnter, label, value, required, placeholder, helperText, disabled, error }) => {
  const helperTextLocalized = helperText ? <FormattedMessage id={helperText} /> : helperText;
  return (
    <TextFieldRoot fullWidth
      disabled={disabled}
      variant="outlined"
      label={<FormattedMessage id={label} />}
      required={required}
      type={"date"}
      helperText={helperTextLocalized}
      placeholder={placeholder + ""}
      value={value}
      error={error}
      onChange={({ target }) => onChange(target.value as any)}
      onKeyPress={onEnter ? (event) => {
        const key = event.key;
        if (key === 'Enter') {
          onEnter();
        }
      } : undefined} />
  );
}

const StyledDateTimeField: React.FC<StyledTextFieldProps<string>> = ({ onChange, onEnter, label, value, required, placeholder, helperText, disabled, error }) => {
  const helperTextLocalized = helperText ? <FormattedMessage id={helperText} /> : helperText;
  return (
    <TextFieldRoot fullWidth
      disabled={disabled}
      variant="outlined"
      label={<FormattedMessage id={label} />}
      required={required}
      type={"datetime-local"}
      helperText={helperTextLocalized}
      placeholder={placeholder + ""}
      value={value}
      error={error}
      onChange={({ target }) => onChange(target.value as any)}
      onKeyPress={onEnter ? (event) => {
        const key = event.key;
        if (key === 'Enter') {
          onEnter();
        }
      } : undefined} />
  );
}

const StyledSearchField: React.FC<StyledTextFieldProps<string>> = ({ onChange, onEnter, label, value, required, placeholder, helperText, disabled, error }) => {
  const helperTextLocalized = helperText ? <FormattedMessage id={helperText} /> : '';

  return (
    <TextFieldRoot fullWidth
      disabled={disabled}
      variant="outlined"
      label={<FormattedMessage id={label} />}
      required={required}
      type={"string"}
      helperText={helperTextLocalized}
      placeholder={placeholder !== undefined && placeholder !== null ? placeholder + '' : ''}
      value={value}
      error={error}
      onChange={({ target }) => onChange(target.value as any)}
      onKeyPress={onEnter ? (event) => {
        const key = event.key;
        if (key === 'Enter') {
          onEnter();
        }
      } : undefined}
      sx={{ maxWidth: "400px" }}
      InputProps={{
        startAdornment: (
          <InputAdornment position="start">
            <SearchIcon sx={{ color: 'uiElements.main' }} />
          </InputAdornment>
        ),
      }} />
  );
}

export type { StyledTextFieldProps }
export { StyledTextField, StyledNumberField, StyledFileField, StyledSearchField, StyledDateField, StyledDateTimeField }

