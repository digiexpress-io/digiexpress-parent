import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentFormField, FsDirentSelectSingle, FsDirentTextField } from '../fs-utilities';
import { useUtilityClasses, FsDirentPrintoutPageRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';

export const FsDirentPrintoutPageCreate: React.FC = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

  return (
    <FsDirentPrintoutPageRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printoutPage.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.printoutPage.serviceIdField.label' })}>
          <FsDirentSelectSingle options={ownerState.printoutOptions} value={ownerState.serviceId} onChange={ownerState.onChangeServiceId} />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.printoutPage.localeIdField.label' })}>
          <FsDirentSelectSingle
            options={ownerState.localeOptions}
            value={ownerState.localeId}
            onChange={ownerState.onChangeLocaleId}
          />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.printoutPage.contentField.label' })}>
          <FsDirentTextField multiline minRows={4}
            placeholder={intl.formatMessage({ id: 'fs.dirent.printoutPage.contentField.placeholder' })}
            value={ownerState.content}
            onChange={ownerState.onChangeContent}
          />
        </FsDirentFormField>

      </div>
    </FsDirentPrintoutPageRoot>
  );
};
