import React from 'react';
import { Autocomplete, TextField } from '@mui/material';
import { FsDirentLocaleSelectProps } from './FsDirentLocaleSelectProps';
import { FsDirentLocaleSelectReadOnly, FsDirentLocaleSelectRoot, useUtilityClasses } from './useUtilityClasses';

interface LocaleOption {
  code: string;
  label: string;
}

const LOCALE_OPTIONS: LocaleOption[] = [
  { code: 'ar', label: 'Arabic' },
  { code: 'bs', label: 'Bosnian' },
  { code: 'bg', label: 'Bulgarian' },
  { code: 'zh', label: 'Chinese' },
  { code: 'hr', label: 'Croatian' },
  { code: 'cs', label: 'Czech' },
  { code: 'da', label: 'Danish' },
  { code: 'nl', label: 'Dutch' },
  { code: 'en', label: 'English' },
  { code: 'et', label: 'Estonian' },
  { code: 'fi', label: 'Finnish' },
  { code: 'fr', label: 'French' },
  { code: 'de', label: 'German' },
  { code: 'hu', label: 'Hungarian' },
  { code: 'is', label: 'Icelandic' },
  { code: 'it', label: 'Italian' },
  { code: 'ja', label: 'Japanese' },
  { code: 'ko', label: 'Korean' },
  { code: 'lv', label: 'Latvian' },
  { code: 'lt', label: 'Lithuanian' },
  { code: 'ms', label: 'Malay' },
  { code: 'nb', label: 'Norwegian' },
  { code: 'pl', label: 'Polish' },
  { code: 'pt', label: 'Portuguese' },
  { code: 'ro', label: 'Romanian' },
  { code: 'ru', label: 'Russian' },
  { code: 'sk', label: 'Slovak' },
  { code: 'es', label: 'Spanish' },
  { code: 'sv', label: 'Swedish' },
  { code: 'uk', label: 'Ukrainian' },
];

export const FsDirentLocaleSelect: React.FC<FsDirentLocaleSelectProps> = (props) => {
  const classes = useUtilityClasses();

  const options = props.allowedCodes
    ? LOCALE_OPTIONS.filter((opt) => props.allowedCodes?.includes(opt.code))
    : LOCALE_OPTIONS;

  if (props.disabled) {
    const selectedOption = options.find((option) => option.code === props.value);
    return (
      <FsDirentLocaleSelectRoot className={classes.root}>
        <FsDirentLocaleSelectReadOnly>{selectedOption?.label ?? props.value}</FsDirentLocaleSelectReadOnly>
      </FsDirentLocaleSelectRoot>
    );
  }

  const selectedOption = options.find((option) => option.code === props.value) ?? null; // null required by MUI Autocomplete controlled value contract

  return (
    <FsDirentLocaleSelectRoot className={classes.root}>
      <Autocomplete
        options={options}
        value={selectedOption}
        disabled={props.disabled}
        getOptionLabel={(option) => option.label}
        onChange={(_event, newValue) => {
          if (newValue) {
            props.onChange?.(newValue.code);
          } else {
            props.onChange?.('');
          }
        }}
        renderInput={(params) => <TextField {...params} size='small' placeholder={props.placeholder ?? '--'} />}
      />
    </FsDirentLocaleSelectRoot>
  );
};
