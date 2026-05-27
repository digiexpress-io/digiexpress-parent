import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent, useFsu } from '@dxs-ts/fs-api';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentButtonDelete } from '../fs-dirent-button-delete';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentLocaleRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentLocaleUpdateProps } from './FsDirentLocaleProps';


export const FsDirentLocaleUpdate: React.FC<FsDirentLocaleUpdateProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const ownerState = useUpdateOwnerState(props);

  const { getConfigOptionsForType } = useFsDirent();
  const { push } = useFsu();
  const configOptions = getConfigOptionsForType('LOCALE');

  return (
    <FsDirentLocaleRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.language.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.localeCode}
          placeholder={intl.formatMessage({ id: 'fs.dirent.language.localeCodeField.placeholder' })}
          disabled
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.language.descriptionField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.description}
          placeholder={intl.formatMessage({ id: 'fs.dirent.language.descriptionField.placeholder' })}
          onChange={ownerState.onChangeDescription}
          onBlur={ownerState.onBlurDescription}
          multiline minRows={2} maxRows={4}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}</Typography>
        <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />

        <div className={classes.buttonContainer}>
          <FsDirentButtonDelete assetId={props.direntId} />
          <FsDirentButtonCancel onClick={ownerState.onCancel} />
          <FsDirentButtonSave onClick={() => push(ownerState.id)} />
        </div>

      </div>
    </FsDirentLocaleRoot>
  );
};
