
import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentFormField } from '../fs-utilities';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentLocaleSelect } from '../fs-dirent-locale-select';
import { useUtilityClasses, FsDirentLocaleRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';


export const FsDirentLocaleCreate: React.FC = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

    return (
      <FsDirentLocaleRoot className={classes.root}>
        <div className={classes.titleRow}>
          <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.language.sectionTitle.createNew' })}</Typography>
          <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isDirty} />
        </div>
        <div className={classes.formContainer}>

          <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.label' })}>
            <FsDirentLocaleSelect value={ownerState.locale} onChange={ownerState.onChangeLocale} />
          </FsDirentFormField>

        </div>
      </FsDirentLocaleRoot>
    );
  };