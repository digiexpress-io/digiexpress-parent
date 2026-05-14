import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentLanguageRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentLanguageCreateProps } from './FsDirentLanguageProps';


export const FsDirentLanguageCreate: React.FC<FsDirentLanguageCreateProps> = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();
  const { getConfigOptionsForType } = useFsDirent();
  const configOptions = getConfigOptionsForType('LOCALE');
  const [localeCode, setLocaleCode] = React.useState('');
  const [selectedConfigOptions, setSelectedConfigOptions] = React.useState<string[]>([]);

  return (
    <FsDirentLanguageRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.language.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.placeholder' })} required value={localeCode} onChange={setLocaleCode} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.language.descriptionField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.language.descriptionField.placeholder' })}
          multiline minRows={2} maxRows={4}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}</Typography>
        <FsDirentSelectMulti options={configOptions} value={selectedConfigOptions} onChange={setSelectedConfigOptions} />

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentLanguageRoot>
  );
};
