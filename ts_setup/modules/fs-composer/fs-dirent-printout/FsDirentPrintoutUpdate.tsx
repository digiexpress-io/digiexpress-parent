import React from 'react';
import { Typography, Divider } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentButtonDelete } from '../fs-dirent-button-delete';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentPrintoutRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentPrintoutUpdateProps } from './FsDirentPrintoutProps';


export const FsDirentPrintoutUpdate: React.FC<FsDirentPrintoutUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentPrintoutRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printout.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printout.nameField.label' })}</Typography>
        <FsDirentTextField value={ownerState.name} placeholder={intl.formatMessage({ id: 'fs.dirent.printout.nameField.placeholder' })}
          onChange={ownerState.onChangeName}
          required
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printout.printoutServiceNameField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.printoutServiceName}
          placeholder={intl.formatMessage({ id: 'fs.dirent.printout.printoutServiceNameField.placeholder' })}
          onChange={ownerState.onChangePrintoutServiceName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printout.orchestratorNameField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.orchestratorName}
          placeholder={intl.formatMessage({ id: 'fs.dirent.printout.orchestratorNameField.placeholder' })}
          onChange={ownerState.onChangeOrchestratorName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printout.descriptionField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.description}
          placeholder={intl.formatMessage({ id: 'fs.dirent.printout.descriptionField.placeholder' })}
          onChange={ownerState.onChangeDescription}
          multiline minRows={2} maxRows={5}
        />
      <Divider />

        <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.dirent.printout.sectionTitle.localeLabels' })}</Typography>
        {ownerState.locales.map((locale) => (
          <div key={locale} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: 'fs.dirent.printout.labelField.label' }, { locale })}</Typography>
            <FsDirentTextField
              value={ownerState.intlValues[locale] ?? ''}
              placeholder={intl.formatMessage({ id: 'fs.dirent.printout.labelField.placeholder' })}
              onChange={(value) => ownerState.onChangeIntlValue(locale, value)}
            />
          </div>
        ))}

        <div className={classes.buttonContainer}>
          <FsDirentButtonDelete assetId={props.direntId} />
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentPrintoutRoot>
  );
};
