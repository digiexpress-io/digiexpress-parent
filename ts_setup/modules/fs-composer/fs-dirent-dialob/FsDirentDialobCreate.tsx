import React from 'react';
import { Typography, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';
import { useUtilityClasses, FsDirentDialobRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentDialobCreateProps } from './FsDirentDialobProps';

export const FsDirentDialobCreate: React.FC<FsDirentDialobCreateProps> = (_props) => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();
  const { selectOptions } = useFsDirent();
  const [labels, setLabels] = React.useState<string[]>([]);

  return (
    <FsDirentDialobRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.dialob.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.dialob.technicalNameField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.dialob.technicalNameField.placeholder' })} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.dialob.formNameField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.dialob.formNameField.placeholder' })} />

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.dialob.expandToggle.hide' : 'fs.dirent.dialob.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.dialob.descriptionField.label' })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.dialob.descriptionField.placeholder' })}
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
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentDialobRoot>
  );
};
