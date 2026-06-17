
import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentFormField } from '../fs-dirent-form-field';
import { useUtilityClasses, FsDirentLocaleRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';


export const FsDirentLocaleCreate: React.FC = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

    return (
      <FsDirentLocaleRoot className={classes.root} ownerState={ownerState}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.language.sectionTitle.createNew' })}</Typography>
        <div className={classes.formContainer}>

          <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.label' })}>
            <FsDirentTextField required value={ownerState.locale}
              placeholder={intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.placeholder' })}
              onChange={ownerState.onChangeLocale}
            />
          </FsDirentFormField>

        </div>
      </FsDirentLocaleRoot>
    );
  };