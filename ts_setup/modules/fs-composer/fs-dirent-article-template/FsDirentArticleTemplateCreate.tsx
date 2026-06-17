import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import MDEditor from '@uiw/react-md-editor';
import { FsDirentFormField, FsDirentTextField } from '../fs-utilities';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { useUtilityClasses, FsDirentArticleTemplateRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';


export const FsDirentArticleTemplateCreate: React.FC = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

  return (
    <FsDirentArticleTemplateRoot className={classes.root} ownerState={ownerState}>
      <div className={classes.titleRow}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.template.sectionTitle.createNew' })}</Typography>
        <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isDirty} />
      </div>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.direntCreate.nameField.placeholder' })}>
          <FsDirentTextField required value={ownerState.name} onChange={ownerState.onChangeName} />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.template.contentField.label' })}>
          <MDEditor height={600}
            data-color-mode="light"
            preview="edit"
            value={ownerState.content}
            onChange={(value) => ownerState.onChangeContent(value ?? '')}
          />
        </FsDirentFormField>

      </div>
    </FsDirentArticleTemplateRoot>
  );
};
