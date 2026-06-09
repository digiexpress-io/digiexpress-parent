import React from 'react';
import { Typography, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentSelectSingle } from '../fs-dirent-select-single';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';
import { FsIcon, FsIcons } from '../fs-theme';
import { useUtilityClasses, FsDirentPrintoutPageRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentPrintoutPageCreateProps } from './FsDirentPrintoutPageProps';

export const FsDirentPrintoutPageCreate: React.FC<FsDirentPrintoutPageCreateProps> = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

  return (
    <FsDirentPrintoutPageRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printoutPage.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printoutPage.serviceIdField.label' })}</Typography>
        <FsDirentSelectSingle options={ownerState.printoutOptions} value={ownerState.serviceId} onChange={ownerState.onChangeServiceId} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printoutPage.localeIdField.label' })}</Typography>
        <FsDirentSelectSingle
          options={ownerState.localeOptions}
          value={ownerState.localeId}
          onChange={ownerState.onChangeLocaleId}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printoutPage.contentField.label' })}</Typography>
        <FsDirentTextField multiline minRows={4}
          placeholder={intl.formatMessage({ id: 'fs.dirent.printoutPage.contentField.placeholder' })}
          value={ownerState.content}
          onChange={ownerState.onChangeContent}
        />

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.expandToggle.hide' : 'fs.dirent.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.descriptionField.label' })}</Typography>
            <FsDirentTextField multiline minRows={2} maxRows={5}
              placeholder={intl.formatMessage({ id: 'fs.dirent.descriptionField.placeholder' })}
              value={ownerState.assetDescription.text}
              onChange={ownerState.onChangeDescription}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.labelsField.label' })}</Typography>
            <FsDirentTextFieldAutocomplete
              options={ownerState.labelOptions}
              value={ownerState.labels}
              onChange={ownerState.onChangeLabels}
              placeholder={intl.formatMessage({ id: 'fs.dirent.labelsField.placeholder' })}
            />
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel onClick={ownerState.onCancel} />
          <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isChanged} />
        </div>

      </div>
    </FsDirentPrintoutPageRoot>
  );
};
