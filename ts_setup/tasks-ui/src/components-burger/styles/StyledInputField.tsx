import React from 'react';
import {
  TextField, TextFieldProps, FormControl,
  FormControlProps, InputLabel, Typography, Button
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import UploadIcon from '@mui/icons-material/Upload';

import { styled } from "@mui/material/styles";
import { FormattedMessage } from 'react-intl';
import { cyan } from 'components-colors';

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

const TextFieldRoot = styled(TextField)<TextFieldProps>(({ theme }) => ({
  marginTop: theme.spacing(1),
  color: theme.palette.primary.contrastText,
  backgroundColor: theme.palette.background.paper,
  '& .MuiInputBase-input': {
    padding: theme.spacing(2)
  },
  '& .MuiOutlinedInput-root': {
    '&.Mui-focused fieldset': {
      borderColor: cyan,
    },
  },
}));

const StyledFormControl = styled(FormControl)<FormControlProps>(({ theme }) => ({
  marginTop: theme.spacing(1),
  color: theme.palette.primary.contrastText,
  backgroundColor: theme.palette.background.paper,
}));

const StyledInputLabel = styled(InputLabel)(({ theme }) => ({
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
  padding: theme.spacing(2),
  color: theme.palette.text.primary,
  backgroundColor: cyan,
  borderRadius: theme.spacing(0.5),
  borderColor: theme.palette.info.contrastText,
  width: 'fit-content',
  ':hover': {
    borderColor: theme.palette.warning.contrastText,
  }
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
      <StyledFormControl variant="outlined" fullWidth>
        <input
          type="file"
          hidden
          ref={inputFile}
          onChange={(e) => handleFileChange}
        />
        <StyledUploadButton
          variant="outlined"
          disabled={disabled}
          startIcon={<UploadIcon />}
          onClick={() => inputFile.current?.click()}
        >
          <Typography><FormattedMessage id='Upload' /></Typography>
        </StyledUploadButton>
      </StyledFormControl>
      <BottomText helperText={helperText} error={error} errorMessage={errorMessage} />
    </>
  );
}

const StyledTextField: React.FC<StyledInputFieldProps<string>> = (props) => {
  const { onChange, onEnter, label, value, required, placeholder, helperText, disabled, errorMessage, error } = props;
  return (
    <>
      <StyledInputLabel><FormattedMessage id={label} />{required && " *"}</StyledInputLabel>
      <TextFieldRoot
        fullWidth
        disabled={disabled}
        variant="outlined"
        placeholder={placeholder + ""}
        value={value}
        error={error}
        onChange={({ target }) => onChange(target.value)}
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

const StyledNumberField: React.FC<StyledInputFieldProps<number>> = (props) => {
  const { onChange, onEnter, label, value, required, placeholder, helperText, disabled, error, errorMessage } = props;
  return (
    <>
      <StyledInputLabel><FormattedMessage id={label} /></StyledInputLabel>
      <TextFieldRoot
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
      <TextFieldRoot
        fullWidth
        disabled={disabled}
        variant="outlined"
        required={required}
        type={"date"}
        placeholder={placeholder + ""}
        value={value}
        error={error}
        sx={{
          '& .MuiInputBase-input': {
            color: placeholderColor,
          }
        }}
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
  const placeholderColor = value ? 'text.primary' : 'text.secondary';
  return (
    <>
      <StyledInputLabel><FormattedMessage id={label} /></StyledInputLabel>
      <TextFieldRoot
        fullWidth
        disabled={disabled}
        variant="outlined"
        required={required}
        type={"datetime-local"}
        placeholder={placeholder + ""}
        value={value}
        error={error}
        sx={{
          '& .MuiInputBase-input': {
            color: placeholderColor,
          }
        }}
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
      <TextFieldRoot
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
            <SearchIcon sx={{ color: cyan }} />
          ),
        }}
      />
      <BottomText helperText={helperText} errorMessage={errorMessage} error={error} />
    </>
  );
}

export type { StyledInputFieldProps }
export { StyledTextField, StyledNumberField, StyledFileField, StyledSearchField, StyledDateField, StyledDateTimeField }

