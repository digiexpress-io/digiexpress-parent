import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentTextField } from '../fs-utilities';
import { useUtilityClasses, FsDirentDialobRoot } from './useUtilityClasses';
import { UpdateOwnerState, useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentDialobProps } from './FsDirentDialobProps';
import { useFetch } from '@dxs-ts/envir-fetch';
import { DialobFormsProvider, useDialobForms } from '@dxs-ts/eveli-api';
import { FsDirentButtonOpen } from '../fs-dirent-button-save';

export const FsDirentDialobUpdate: React.FC<FsDirentDialobProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { dialobUrl } = useFetch('dialob.GET', {});

  return (
    <DialobFormsProvider dialobApiUrl={dialobUrl}>
      <FsDirentDialobRoot className={classes.root}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.dialob.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>
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

        </div>

        <OpenForm ownerState={ownerState} />
      </FsDirentDialobRoot>
    </DialobFormsProvider>
  );
}

const OpenForm: React.FC<{ ownerState: UpdateOwnerState }> = ({ ownerState }) => {
  const { openForm } = useDialobForms();

  function handleOnClick(event: React.MouseEvent) {
    event.preventDefault()
    openForm({ id: ownerState.dirent!.id } as any);
  }

  return (<FsDirentButtonOpen onClick={handleOnClick} />)
}
