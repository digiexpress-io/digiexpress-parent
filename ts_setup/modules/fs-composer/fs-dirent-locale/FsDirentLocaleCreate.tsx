
import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentFormField, FsDirentTextField } from '../fs-utilities';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { useUtilityClasses, FsDirentLocaleRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';


export const FsDirentLocaleCreate: React.FC = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

    return (
      <FsDirentLocaleRoot className={classes.root} ownerState={ownerState}>
        <div className={classes.titleRow}>
          <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.language.sectionTitle.createNew' })}</Typography>
          <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isDirty} />
        </div>
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