import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import { useFetch } from '@dxs-ts/envir-fetch';
import { DialobFormsProvider } from '@dxs-ts/eveli-api';

import { FsDirentTextField } from '../fs-utilities';

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
        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.dialob.sectionTitle.createNew' })}</Typography>
        <div className={classes.formContainer}>

          <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.dialob.technicalNameField.label' })}</Typography>
          <FsDirentTextField required placeholder={intl.formatMessage({ id: 'fs.dirent.dialob.technicalNameField.placeholder' })} />

          <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.dialob.formNameField.label' })}</Typography>
          <FsDirentTextField required placeholder={intl.formatMessage({ id: 'fs.dirent.dialob.formNameField.placeholder' })} />

        </div>
      </FsDirentDialobRoot>
    </DialobFormsProvider>
  );
}





