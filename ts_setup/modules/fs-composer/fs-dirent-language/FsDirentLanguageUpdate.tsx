import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentData } from '@dxs-ts/fs-api';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentLanguageRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentLanguageUpdateProps } from './FsDirentLanguageProps';


const configOptions = FsDirentData.getConfigOptionsForType('language');

export const FsDirentLanguageUpdate: React.FC<FsDirentLanguageUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentLanguageRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.language.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.localeCode}
          placeholder={intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.placeholder' })}
          onChange={ownerState.onChangeLocaleCode}
          disabled
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.language.descriptionField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.description}
          placeholder={intl.formatMessage({ id: 'fs.dirent.language.descriptionField.placeholder' })}
          onChange={ownerState.onChangeDescription}
          multiline minRows={2} maxRows={4}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.language.configOptionsField.label' })}</Typography>
        <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentLanguageRoot>
  );
};
