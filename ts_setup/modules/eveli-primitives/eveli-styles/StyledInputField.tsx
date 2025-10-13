import React from 'react';
import {
  TextField, FormControl,
  FormControlProps, InputLabel, Typography, Button, styled
} from '@mui/material';
import { Search as SearchIcon } from '@mui/icons-material';
import { Upload as UploadIcon } from '@mui/icons-material';

import { FormattedMessage } from 'react-intl';

interface StyledInputFieldProps<T> {
  label: string;
  disabled?: boolean;
  helperText?: string;
  value: T | undefined;
  placeholder?: T | string;
  required?: boolean;
  error?: boolean;
  errorMessage?: string;
  onChange: (newValue: T) => void;
  onEnter?: () => void;
}


const StyledFormControl = styled(FormControl)<FormControlProps>(({ theme }) => ({
  marginTop: theme.spacing(1),
  color: theme.palette.primary.contrastText,
  backgroundColor: theme.palette.background.paper,
}));

export const StyledInputLabel = styled(InputLabel)(({ theme }) => ({
  marginTop: theme.spacing(2),
  color: theme.palette.text.primary,
  fontWeight: theme.typography.h1.fontWeight,
  paddingLeft: theme.spacing(2),
}));

const StyledBottomText = styled(Typography)(({ theme }) => ({
  marginTop: theme.spacing(1),
  paddingLeft: theme.spacing(2),
}));

const StyledUploadButton = styled(Button)(({ theme }) => ({


}));

const BottomText: React.FC<{
  helperText: string | undefined,
  errorMessage: string | undefined,
  error: boolean | undefined
}> = ({ helperText, errorMessage, error }) => {
  if (error) {
    return (
      <StyledBottomText color="error" variant='caption'>
        {errorMessage ? <FormattedMessage id={errorMessage} /> : errorMessage}
      </StyledBottomText>
    );
  }
  return (
    <StyledBottomText variant='caption'>
      {helperText ? <FormattedMessage id={helperText} /> : helperText}
    </StyledBottomText >
  );
}

const StyledFileField: React.FC<StyledInputFieldProps<string>> = (props) => {
  const { onChange, label, helperText, disabled, errorMessage, error } = props;
  const inputFile = React.useRef<HTMLInputElement>(null);

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file: File = (e.target as any).files[0];
    const enc = new TextDecoder("utf-8");
    file.arrayBuffer().then(d => onChange(enc.decode(d)));
  }

  return (
    <>
      <StyledInputLabel><FormattedMessage id={label} /></StyledInputLabel>
      <StyledFormControl variant="outlined">
        <input type="file" hidden ref={inputFile} accept="json" onChange={handleFileChange} />

        <StyledUploadButton
          variant="outlined"
          disabled={disabled}
          startIcon={<UploadIcon />}
          onClick={() => inputFile.current?.click()}
        >
          <FormattedMessage id='Upload' />
        </StyledUploadButton>
      </StyledFormControl>
      <BottomText helperText={helperText} error={error} errorMessage={errorMessage} />
    </>
  );
}

const StyledTextField: React.FC<StyledInputFieldProps<string> & { type?: React.InputHTMLAttributes<unknown>['type'] }> = (props) => {
  const { onChange, onEnter, label, value, required, placeholder, helperText, disabled, errorMessage, error, type } = props;
  return (
    <>
      <StyledInputLabel><FormattedMessage id={label} /></StyledInputLabel>
      <TextField
        fullWidth
        type={type}
        disabled={disabled}
        variant="outlined"
        required={required}
        placeholder={placeholder + ""}
        value={value}
        error={error}
        onChange={({ target }) => onChange(target.value)}
        onKeyDown={(event) => {
          event.stopPropagation();
          if (onEnter && event.key === 'Enter') {
            onEnter();
          }
        }}
      />

      <BottomText helperText={helperText} errorMessage={errorMessage} error={error} />
    </>
  );
}

const StyledNumberField: React.FC<StyledInputFieldProps<number>> = (props) => {
  const { onChange, onEnter, label, value, required, placeholder, helperText, disabled, error, errorMessage } = props;
  return (
    <>
      <StyledInputLabel><FormattedMessage id={label} /></StyledInputLabel>
      <TextField
        fullWidth
        disabled={disabled}
        variant="outlined"
        required={required}
        type={"number"}
        placeholder={placeholder + ""}
        value={value}
        error={error}
        onChange={({ target }) => onChange(target.value as any)}
        onKeyPress={onEnter ? (event) => {
          const key = event.key;
          if (key === 'Enter') {
            onEnter();
          }
        } : undefined}
      />
      <BottomText helperText={helperText} errorMessage={errorMessage} error={error} />
    </>
  );
}

const StyledDateField: React.FC<StyledInputFieldProps<string>> = (props) => {
  const { onChange, onEnter, label, value, required, placeholder, helperText, disabled, error, errorMessage } = props;
  const placeholderColor = value ? 'text.primary' : 'text.secondary';
  return (
    <>
      <StyledInputLabel><FormattedMessage id={label} /></StyledInputLabel>
      <TextField
        fullWidth
        disabled={disabled}
        variant="outlined"
        required={required}
        type={"date"}
        placeholder={placeholder + ""}
        value={value}
        error={error}
        onChange={({ target }) => onChange(target.value as any)}
        onKeyPress={onEnter ? (event) => {
          const key = event.key;
          if (key === 'Enter') {
            onEnter();
          }
        } : undefined}
      />
      <BottomText helperText={helperText} errorMessage={errorMessage} error={error} />
    </>
  );
}

const StyledDateTimeField: React.FC<StyledInputFieldProps<string>> = (props) => {
  const { onChange, onEnter, label, value, required, placeholder, helperText, disabled, error, errorMessage } = props;
  return (
    <>
      <StyledInputLabel><FormattedMessage id={label} /></StyledInputLabel>
      <TextField
        fullWidth
        disabled={disabled}
        variant="outlined"
        required={required}
        type={"datetime-local"}
        placeholder={placeholder + ""}
        value={value}
        error={error}
        onChange={({ target }) => onChange(target.value as any)}
        onKeyPress={onEnter ? (event) => {
          const key = event.key;
          if (key === 'Enter') {
            onEnter();
          }
        } : undefined}
      />
      <BottomText helperText={helperText} errorMessage={errorMessage} error={error} />
    </>
  );
}

const StyledSearchField: React.FC<StyledInputFieldProps<string>> = (props) => {
  const { onChange, onEnter, label, value, required, placeholder, helperText, disabled, error, errorMessage } = props;
  return (
    <>
      <StyledInputLabel><FormattedMessage id={label} /></StyledInputLabel>
      <TextField
        fullWidth
        disabled={disabled}
        variant="outlined"
        required={required}
        type={"string"}
        placeholder={placeholder + ""}
        value={value}
        error={error}
        onChange={({ target }) => onChange(target.value as any)}
        onKeyPress={onEnter ? (event) => {
          const key = event.key;
          if (key === 'Enter') {
            onEnter();
          }
        } : undefined}
        InputProps={{
          endAdornment: (
            <SearchIcon />
          ),
        }}
      />
      <BottomText helperText={helperText} errorMessage={errorMessage} error={error} />
    </>
  );
}

export type { StyledInputFieldProps }
export { StyledTextField, StyledNumberField, StyledFileField, StyledSearchField, StyledDateField, StyledDateTimeField }

