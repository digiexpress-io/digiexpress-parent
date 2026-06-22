import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import { useFetch } from '@dxs-ts/envir-fetch';
import { DialobFormsProvider } from '@dxs-ts/eveli-api';

import { FsDirentFormField, FsDirentTextField } from '../fs-utilities';
import { FsDirentButtonSave } from '../fs-dirent-button-save';

import { useUtilityClasses, FsDirentDialobRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';

export const FsDirentDialobCreate: React.FC = (_props) => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();
  const { dialobUrl } = useFetch('dialob.GET', {});

  return (
    <DialobFormsProvider dialobApiUrl={dialobUrl}>
      <FsDirentDialobRoot className={classes.root} ownerState={ownerState}>
        <div className={classes.titleRow}>
          <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.dialob.sectionTitle.createNew' })}</Typography>
          <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isDirty} />
        </div>
        <div className={classes.formContainer}>

          <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.dialob.technicalNameField.label' })}>
            <FsDirentTextField required
              placeholder={intl.formatMessage({ id: 'fs.dirent.dialob.technicalNameField.placeholder' })}
              value={ownerState.formTechnicalId}
              onChange={ownerState.onChangeFormTechnicalId}
            />
          </FsDirentFormField>

          <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.dialob.formNameField.label' })}>
            <FsDirentTextField required
              placeholder={intl.formatMessage({ id: 'fs.dirent.dialob.formNameField.placeholder' })}
              value={ownerState.formName}
              onChange={ownerState.onChangeFormName}
            />
          </FsDirentFormField>

        </div>
      </FsDirentDialobRoot>
    </DialobFormsProvider>
  );
}
