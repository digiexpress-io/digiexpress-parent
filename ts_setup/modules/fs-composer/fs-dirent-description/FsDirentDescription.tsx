import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentTextField } from '../fs-utilities';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentDescriptionProps } from './FsDirentDescriptionProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsDirentDescriptionRoot } from './useUtilityClasses';


export const FsDirentDescription: React.FC<FsDirentDescriptionProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses(); 


  return (
    <FsDirentDescriptionRoot className={classes.root} ownerState={ownerState}>
      <div className={classes.titleRow}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.descriptionField.label' })}</Typography>
        <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isDirty} />
      </div>

      <FsDirentTextField multiline minRows={25} maxRows={40}
        value={ownerState.description}
        onChange={(value) => ownerState.onChangeDescription(value)}
        placeholder={intl.formatMessage({ id: 'fs.dirent.descriptionField.placeholder' })}
      />
    </FsDirentDescriptionRoot>
  );
};
