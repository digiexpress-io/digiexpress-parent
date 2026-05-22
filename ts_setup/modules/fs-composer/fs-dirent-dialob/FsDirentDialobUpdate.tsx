import React from 'react';
import { Typography, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentButtonDelete } from '../fs-dirent-button-delete';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';
import { useUtilityClasses, FsDirentDialobRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentDialobUpdateProps } from './FsDirentDialobProps';

export const FsDirentDialobUpdate: React.FC<FsDirentDialobUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { selectOptions } = useFsDirent();
  const [labels, setLabels] = React.useState<string[]>((ownerState.dirent?.props?.labels ?? []).map(l => l.value));

  return (
    <FsDirentDialobRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.dialob.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.dialob.technicalNameField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.technicalName}
          placeholder={intl.formatMessage({ id: 'fs.dirent.dialob.technicalNameField.placeholder' })}
          onChange={ownerState.onChangeTechnicalName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.dialob.formNameField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.formName}
          placeholder={intl.formatMessage({ id: 'fs.dirent.dialob.formNameField.placeholder' })}
          onChange={ownerState.onChangeFormName}
        />

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.expandToggle.hide' : 'fs.dirent.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.dialob.descriptionField.label' })}</Typography>
            <FsDirentTextField
              value={ownerState.description}
              placeholder={intl.formatMessage({ id: 'fs.dirent.dialob.descriptionField.placeholder' })}
              onChange={ownerState.onChangeDescription}
              multiline minRows={2} maxRows={5}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.dialob.labelsField.label' })}</Typography>
            <FsDirentTextFieldAutocomplete
              options={selectOptions.labels}
              value={labels}
              onChange={setLabels}
              placeholder={intl.formatMessage({ id: 'fs.dirent.dialob.labelsField.placeholder' })}
            />
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonDelete assetId={props.direntId} />
          <FsDirentButtonCancel />
          <FsDirentButtonSave />
        </div>

      </div>
    </FsDirentDialobRoot>
  );
};
