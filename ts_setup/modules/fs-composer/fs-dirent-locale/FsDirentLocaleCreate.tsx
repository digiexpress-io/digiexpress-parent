
import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentLocaleRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentLocaleCreateProps } from './FsDirentLocaleProps';


export const FsDirentLocaleCreate: React.FC<FsDirentLocaleCreateProps> = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

    return (
      <FsDirentLocaleRoot className={classes.root} ownerState={ownerState}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.language.sectionTitle.createNew' })}</Typography>
        <div className={classes.formContainer}>

          <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.label' })}</Typography>
          <FsDirentTextField
            required
            value={ownerState.locale}
            placeholder={intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.placeholder' })}
            onChange={ownerState.onChangeLocale}
            onBlur={ownerState.onBlurLocale}
          />

          <div className={classes.buttonContainer}>
            <FsDirentButtonCancel onClick={ownerState.onCancel} />
            <FsDirentButtonSave onClick={ownerState.onSave} />
          </div>

        </div>
      </FsDirentLocaleRoot>
    );
  };