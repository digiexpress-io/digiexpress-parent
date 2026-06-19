import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentFormField, FsDirentSelectSingle, FsDirentTextField } from '../fs-utilities';
import { useUtilityClasses, FsDirentPrintoutRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentPrintoutProps } from './FsDirentPrintoutProps';

export const FsDirentPrintoutUpdate: React.FC<FsDirentPrintoutProps> = ({ direntId }) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState({ direntId });
  const classes = useUtilityClasses();

  return (
    <FsDirentPrintoutRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printout.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.nameField.label' })}>
          <FsDirentTextField required placeholder={intl.formatMessage({ id: 'fs.dirent.printout.nameField.placeholder' })}
            value={ownerState.serviceName}
            onChange={ownerState.onChangeServiceName}
          />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.printout.orchestratorNameField.label' })}>
          <FsDirentSelectSingle
            options={ownerState.flows}
            value={ownerState.orchestratorName}
            onChange={ownerState.onChangeOrchestratorName}
          />
        </FsDirentFormField>

        {ownerState.locales.map((locale) => (
          <FsDirentFormField key={locale.value} label={intl.formatMessage({ id: 'fs.dirent.printout.locales.labelField' }, { localeCode: locale.label })}>
            <FsDirentTextField value={ownerState.intlValues[locale.value] ?? ''} onChange={(value) => ownerState.onChangeIntlValue(locale.value, value)} />
          </FsDirentFormField>
        ))}

      </div>
    </FsDirentPrintoutRoot>
  );
};
