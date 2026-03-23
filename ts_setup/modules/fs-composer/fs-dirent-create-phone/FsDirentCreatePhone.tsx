import React from 'react';
import { TextField, Typography, Divider } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentCreatePhoneProps } from './FsDirentCreatePhoneProps';
import { useUtilityClasses, FsDirentCreatePhoneRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentCreatePhone: React.FC<FsDirentCreatePhoneProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentCreatePhoneRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntCreate.phone.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.phone.phoneValueField.label' })}</Typography>
        <TextField className={classes.textField}
          placeholder={intl.formatMessage({ id: 'fs.direntCreate.phone.phoneValueField.placeholder' })}
          size='small' fullWidth
        />

        <Divider />

        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntCreate.phone.sectionTitle.createLocaleLabels' })}</Typography>

        {ownerState.locales.map((locale) => (
          <div key={locale} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: `fs.direntCreate.phone.labelField.${locale}.label` })}</Typography>
            <TextField className={classes.textField}
              placeholder={intl.formatMessage({ id: 'fs.direntCreate.phone.labelField.placeholder' })}
              size='small' fullWidth
            />
          </div>
        ))}

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentCreatePhoneRoot>
  );
};
