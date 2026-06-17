import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { createWidget } from '../fs-factory';
import { FsDirentFormField, FsDirentSelectMulti, FsDirentTextField } from '../fs-utilities';
import { useUtilityClasses, FsDirentLocaleRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentLocaleProps } from './FsDirentLocaleProps';


export const FsDirentLocaleUpdate: React.FC<FsDirentLocaleProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const ownerState = useUpdateOwnerState(props);

  const configOptions = createWidget({ type: 'LOCALE' }).meta.configOptions.map(opt => ({
    value: opt,
    label: intl.formatMessage({ id: `fs.dirent.configOption.${opt}` }),
  }));

  return (
    <FsDirentLocaleRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.language.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.label' })}>
          <FsDirentTextField
            value={ownerState.localeCode}
            placeholder={intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.placeholder' })}
            disabled
          />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}>
          <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />
        </FsDirentFormField>

      </div>
    </FsDirentLocaleRoot>
  );
};
