import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentSelectSingle } from '../fs-dirent-select-single';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';
import { useUtilityClasses, FsDirentPrintoutRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentPrintoutUpdateProps } from './FsDirentPrintoutProps';

export const FsDirentPrintoutUpdate: React.FC<FsDirentPrintoutUpdateProps> = ({ direntId }) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState({ direntId });
  const classes = useUtilityClasses();

  return (
    <FsDirentPrintoutRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printout.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.nameField.label' })}</Typography>
        <FsDirentTextField required placeholder={intl.formatMessage({ id: 'fs.dirent.printout.nameField.placeholder' })}
          value={ownerState.serviceName}
          onChange={ownerState.onChangeServiceName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printout.orchestratorNameField.label' })}</Typography>
        <FsDirentSelectSingle
          options={ownerState.flows}
          value={ownerState.orchestratorName}
          onChange={ownerState.onChangeOrchestratorName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.descriptionField.label' })}</Typography>
        <FsDirentTextField multiline minRows={2} maxRows={5}
          placeholder={intl.formatMessage({ id: 'fs.dirent.descriptionField.placeholder' })}
          value={ownerState.assetDescription}
          onChange={ownerState.onChangeDescription}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.labelsField.label' })}</Typography>
        <FsDirentTextFieldAutocomplete options={ownerState.labelOptions}
          value={ownerState.labels}
          onChange={ownerState.onChangeLabels}
          placeholder={intl.formatMessage({ id: 'fs.dirent.labelsField.placeholder' })}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printout.pagesSection.label' })}</Typography>
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


      </div>
    </FsDirentPrintoutRoot>
  );
};
