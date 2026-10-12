import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentFormField, FsDirentSelectSingle, FsDirentTextField } from '../fs-utilities';
import { FsDirentLocaleSelect } from '../fs-dirent-locale-select';
import { useUtilityClasses, FsDirentPrintoutRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentPrintoutProps } from './FsDirentPrintoutProps';

export const FsDirentPrintoutUpdate: React.FC<FsDirentPrintoutProps> = ({ direntId }) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState({ direntId });
  const classes = useUtilityClasses();

  return (
    <FsDirentPrintoutRoot className={classes.root}>
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

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.printout.sectionTitle.localeLabels' })}>
          {ownerState.localeEntries.map((entry, index) => (
            <div key={index} className={classes.localeRow}>
              <div className={classes.localeSelectContainer}>
                <FsDirentLocaleSelect
                  value={entry.localeCode}
                  allowedCodes={ownerState.availableCodes(index)}
                  onChange={(code) => ownerState.onChangeLocaleEntry(index, code)}
                />
              </div>
              {entry.localeCode !== '' && (
                <FsDirentTextField
                  value={entry.labelValue}
                  required
                  onChange={(value) => ownerState.onChangeLabelEntry(index, value)}
                />
              )}
            </div>
          ))}
        </FsDirentFormField>

      </div>
    </FsDirentPrintoutRoot>
  );
};
