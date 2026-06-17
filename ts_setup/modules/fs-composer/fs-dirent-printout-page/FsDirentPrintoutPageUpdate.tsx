import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentFormField } from '../fs-dirent-form-field';
import { useUtilityClasses, FsDirentPrintoutPageRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentPrintoutPageProps } from './FsDirentPrintoutPageProps';

export const FsDirentPrintoutPageUpdate: React.FC<FsDirentPrintoutPageProps> = ({ direntId }) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState({ direntId });
  const classes = useUtilityClasses();

  return (
    <FsDirentPrintoutPageRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printoutPage.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.printoutPage.contentField.label' })}>
          <FsDirentTextField multiline minRows={4}
            placeholder={intl.formatMessage({ id: 'fs.dirent.printoutPage.contentField.placeholder' })}
            value={ownerState.content}
            onChange={ownerState.onChangeContent}
          />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.printoutPage.printoutResourcesField.label' })}>
          {ownerState.connectedResourceNames.length === 0 ? (
            <Typography variant='body2' color='textSecondary'>
              {intl.formatMessage({ id: 'fs.dirent.printoutPage.printoutResourcesField.empty' })}
            </Typography>
          ) : (
            <div className={classes.resourceList}>
              {ownerState.connectedResourceNames.map((name, index) => (
                <Typography key={index} variant='body2'>{name}</Typography>
              ))}
            </div>
          )}
        </FsDirentFormField>

      </div>
    </FsDirentPrintoutPageRoot>
  );
};
