import React from 'react';
import { Typography, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsu } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
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

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.expandToggle.hide' : 'fs.dirent.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            {ownerState.locales.map(locale => (
              <React.Fragment key={locale.value}>
                <Typography className={classes.label}>{locale.label}</Typography>
                <FsDirentTextField
                  value={ownerState.intlValues[locale.value] ?? ''}
                  onChange={(v) => ownerState.onChangeIntlValues(locale.value, v)}
                  onBlur={() => ownerState.onBlurIntlValues(locale.value)}
                />
              </React.Fragment>
            ))}
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel onClick={ownerState.onCancel} />
          <FsDirentButtonSave onClick={() => push(direntId)} disabled={!ownerState.isChanged} />
        </div>

      </div>
    </FsDirentPrintoutRoot>
  );
};
