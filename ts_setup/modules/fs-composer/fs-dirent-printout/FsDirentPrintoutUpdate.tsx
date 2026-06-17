import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentSelectSingle } from '../fs-dirent-select-single';
import { FsDirentFormField } from '../fs-dirent-form-field';
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

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.printout.pagesSection.label' })}>
          {ownerState.connectedPages.length === 0 ? (
            <Typography variant='body2' color='textSecondary'>
              {intl.formatMessage({ id: 'fs.dirent.printout.pagesSection.empty' })}
            </Typography>
          ) : (
            <div className={classes.pageList}>
              {ownerState.connectedPages.map(page => (
                <Typography key={page.id} variant='body2'>{page.localeName}</Typography>
              ))}
            </div>
          )}
        </FsDirentFormField>


      </div>
    </FsDirentPrintoutRoot>
  );
};
