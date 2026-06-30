import React from 'react';
import { Radio, RadioGroup, FormControlLabel, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentFormField, FsDirentTextField } from '../fs-utilities';
import { useUtilityClasses, FsDirentLocaleRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentLocaleProps } from './FsDirentLocaleProps';


export const FsDirentLocaleUpdate: React.FC<FsDirentLocaleProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const ownerState = useUpdateOwnerState(props);

  return (
    <FsDirentLocaleRoot className={classes.root}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.language.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.label' })}>
          <FsDirentTextField
            value={ownerState.localeCode}
            placeholder={intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.placeholder' })}
            disabled
          />
        </FsDirentFormField>

        <FsDirentFormField
          label={intl.formatMessage({ id: 'fs.dirent.language.enabledField.label' })}
          helperText={ownerState.enabled
            ? intl.formatMessage({ id: 'fs.dirent.language.enabled.note' })
            : intl.formatMessage({ id: 'fs.dirent.language.disabled.note' })}
        >
          <RadioGroup
            row
            value={ownerState.enabled ? 'enabled' : 'disabled'}
            onChange={(event) => ownerState.onChangeEnabled(event.target.value === 'enabled')}
          >
            <FormControlLabel value='enabled' control={<Radio />} label={intl.formatMessage({ id: 'fs.dirent.language.enabled' })} />
            <FormControlLabel value='disabled' control={<Radio />} label={intl.formatMessage({ id: 'fs.dirent.language.disabled' })} />
          </RadioGroup>
        </FsDirentFormField>

      </div>
    </FsDirentLocaleRoot>
  );
};
