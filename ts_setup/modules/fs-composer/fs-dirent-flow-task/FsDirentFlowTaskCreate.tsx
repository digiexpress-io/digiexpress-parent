import React from 'react';
import { Typography, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentFlowTaskRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentFlowTaskCreateProps } from './FsDirentFlowTaskProps';


export const FsDirentFlowTaskCreate: React.FC<FsDirentFlowTaskCreateProps> = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();
  const { selectOptions } = useFsDirent();

  return (
    <FsDirentFlowTaskRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.flow_task.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.flow_task.taskNameField.label' })}</Typography>
        <FsDirentTextField
          required
          value={ownerState.name}
          placeholder={intl.formatMessage({ id: 'fs.dirent.flow_task.taskNameField.placeholder' })}
          onChange={ownerState.onChangeName}
          onBlur={ownerState.onBlurName}
        />

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.expandToggle.hide' : 'fs.dirent.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.labelsField.label' })}</Typography>
            <FsDirentTextFieldAutocomplete
              options={selectOptions.labels}
              value={ownerState.tagLabels}
              onChange={ownerState.onChangeLabels}
              placeholder={intl.formatMessage({ id: 'fs.dirent.labelsField.placeholder' })}
            />
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel onClick={ownerState.onCancel} />
          <FsDirentButtonSave onClick={ownerState.onSave} />
        </div>

      </div>
    </FsDirentFlowTaskRoot>
  );
};
