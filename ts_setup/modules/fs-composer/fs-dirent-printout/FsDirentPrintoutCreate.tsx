import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentFormField, FsDirentSelectSingle, FsDirentTextField } from '../fs-utilities';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentLocaleSelect } from '../fs-dirent-locale-select';
import { useUtilityClasses, FsDirentPrintoutRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';

export const FsDirentPrintoutCreate: React.FC = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

  return (
    <FsDirentPrintoutRoot className={classes.root}>
      <div className={classes.titleRow}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printout.sectionTitle.createNew' })}</Typography>
        <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isDirty || ownerState.isLocaleEntryIncomplete} />
      </div>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.nameField.label' })}>
          <FsDirentTextField required placeholder={intl.formatMessage({ id: 'fs.dirent.printout.nameField.placeholder' })}
            value={ownerState.serviceName}
            onChange={ownerState.onChangeServiceName}
          />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.printout.orchestratorNameField.label' })}>
          <FsDirentSelectSingle required
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
