import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import MDEditor from '@uiw/react-md-editor';
import { FsDirentFormField, FsDirentTextField } from '../fs-utilities';
import { useUtilityClasses, FsDirentArticleTemplateRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentArticleTemplateProps } from './FsDirentArticleTemplateProps';


export const FsDirentArticleTemplateUpdate: React.FC<FsDirentArticleTemplateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentArticleTemplateRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.template.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.direntCreate.nameField.placeholder' })}>
          <FsDirentTextField required value={ownerState.name} onChange={ownerState.onChangeName} />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.template.contentField.label' })}>
          <MDEditor data-color-mode="light" height={600} preview="edit" value={ownerState.content} onChange={(value) => ownerState.onChangeContent(value ?? '')} />
        </FsDirentFormField>

      </div>
    </FsDirentArticleTemplateRoot>
  );
};
