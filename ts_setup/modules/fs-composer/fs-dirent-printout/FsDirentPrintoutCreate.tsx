import React from 'react';
import { Typography, Divider } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentPrintoutRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentPrintoutCreateProps } from './FsDirentPrintoutProps';


export const FsDirentPrintoutCreate: React.FC<FsDirentPrintoutCreateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState(props);
  const classes = useUtilityClasses();
  const [name, setName] = React.useState('');

  return (
    <FsDirentPrintoutRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printout.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printout.nameField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.printout.nameField.placeholder' })} required value={name} onChange={setName} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printout.printoutServiceNameField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.printout.printoutServiceNameField.placeholder' })} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printout.orchestratorNameField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.printout.orchestratorNameField.placeholder' })} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printout.descriptionField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.printout.descriptionField.placeholder' })} multiline minRows={2} maxRows={5} />

        <Divider />
        <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.dirent.printout.sectionTitle.localeLabels' })}</Typography>

        {ownerState.locales.map((locale) => (
          <div key={locale} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: 'fs.dirent.printout.labelField.label' }, { locale })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.printout.labelField.placeholder' })} />
          </div>
        ))}

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentPrintoutRoot>
  );
};
