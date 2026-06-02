import React from 'react';
import { Typography, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsu } from '@dxs-ts/fs-api';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';
import { FsIcon, FsIcons } from '../fs-theme';
import { useUtilityClasses, FsDirentPrintoutPageRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentPrintoutPageUpdateProps } from './FsDirentPrintoutPageProps';

export const FsDirentPrintoutPageUpdate: React.FC<FsDirentPrintoutPageUpdateProps> = ({ direntId }) => {
  const intl = useIntl();
  const { push } = useFsu();
  const ownerState = useUpdateOwnerState({ direntId });
  const classes = useUtilityClasses();

  return (
    <FsDirentPrintoutPageRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printoutPage.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printoutPage.contentField.label' })}</Typography>
        <FsDirentTextField
          multiline
          minRows={4}
          placeholder={intl.formatMessage({ id: 'fs.dirent.printoutPage.contentField.placeholder' })}
          value={ownerState.content}
          onChange={ownerState.onChangeContent}
          onBlur={ownerState.onBlurContent}
        />

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.expandToggle.hide' : 'fs.dirent.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.descriptionField.label' })}</Typography>
            <FsDirentTextField
              multiline
              minRows={2}
              maxRows={5}
              placeholder={intl.formatMessage({ id: 'fs.dirent.descriptionField.placeholder' })}
              value={ownerState.description}
              onChange={ownerState.onChangeDescription}
              onBlur={ownerState.onBlurDescription}
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
          <FsDirentButtonSave onClick={() => push(direntId)} disabled={!ownerState.isChanged} />
        </div>

      </div>
    </FsDirentPrintoutPageRoot>
  );
};
