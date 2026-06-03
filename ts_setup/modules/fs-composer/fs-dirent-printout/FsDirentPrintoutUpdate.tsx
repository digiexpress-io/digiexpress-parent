import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsu } from '@dxs-ts/fs-api';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentSelectSingle } from '../fs-dirent-select-single';
import { useUtilityClasses, FsDirentPrintoutRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentPrintoutUpdateProps } from './FsDirentPrintoutProps';

export const FsDirentPrintoutUpdate: React.FC<FsDirentPrintoutUpdateProps> = ({ direntId }) => {
  const intl = useIntl();
  const { push } = useFsu();
  const ownerState = useUpdateOwnerState({ direntId });
  const classes = useUtilityClasses();

  return (
    <FsDirentPrintoutRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printout.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.nameField.label' })}</Typography>
        <FsDirentTextField required
          placeholder={intl.formatMessage({ id: 'fs.dirent.printout.nameField.placeholder' })}
          value={ownerState.serviceName}
          onChange={ownerState.onChangeServiceName}
          onBlur={ownerState.onBlurServiceName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printout.orchestratorNameField.label' })}</Typography>
        <FsDirentSelectSingle
          options={ownerState.flows}
          value={ownerState.orchestratorName}
          onChange={ownerState.onChangeOrchestratorName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.descriptionField.label' })}</Typography>
        <FsDirentTextField
          multiline minRows={2} maxRows={5}
          placeholder={intl.formatMessage({ id: 'fs.dirent.descriptionField.placeholder' })}
          value={ownerState.description}
          onChange={ownerState.onChangeDescription}
          onBlur={ownerState.onBlurDescription}
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

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel onClick={ownerState.onCancel} />
          <FsDirentButtonSave onClick={() => push(direntId)} disabled={!ownerState.isChanged} />
        </div>

      </div>
    </FsDirentPrintoutRoot>
  );
};
