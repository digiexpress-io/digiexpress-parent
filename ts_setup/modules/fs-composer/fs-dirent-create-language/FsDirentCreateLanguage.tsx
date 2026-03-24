import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { getConfigOptionsForType } from '@dxs-ts/fs-api';
import { FsDirentMultiSelect } from '../fs-dirent-multi-select';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentCreateLanguageProps } from './FsDirentCreateLanguageProps';
import { useUtilityClasses, FsDirentCreateLanguageRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';


export const FsDirentCreateLanguage: React.FC<FsDirentCreateLanguageProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();
  const configOptions = getConfigOptionsForType('language');
  const [selectedConfigOptions, setSelectedConfigOptions] = React.useState<string[]>([]);

  return (
    <FsDirentCreateLanguageRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntCreate.language.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.language.localeCodeField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.language.localeCodeField.placeholder' })} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.language.descriptionField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.language.descriptionField.placeholder' })}
          multiline minRows={2} maxRows={5}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.language.configOptionsField.label' })}</Typography>
        <FsDirentMultiSelect options={configOptions} value={selectedConfigOptions} onChange={setSelectedConfigOptions} />

        {selectedConfigOptions.includes('disabledMode') && (
          <Typography className={classes.configOptionDescription}>{intl.formatMessage({ id: 'fs.direntCreate.language.configOption.disabledMode.description' })}</Typography>
        )}

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentCreateLanguageRoot>
  );
};

