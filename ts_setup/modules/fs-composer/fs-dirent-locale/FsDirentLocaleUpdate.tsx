import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { createWidget } from '../fs-factory';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentTextField } from '../fs-dirent-text-field';
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

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.localeCode}
          placeholder={intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.placeholder' })}
          disabled
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}</Typography>
        <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />

      </div>
    </FsDirentLocaleRoot>
  );
};
