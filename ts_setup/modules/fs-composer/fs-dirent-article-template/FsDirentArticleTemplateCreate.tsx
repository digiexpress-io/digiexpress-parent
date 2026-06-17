import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import MonacoReact from '@monaco-editor/react';
import { FsDirentFormField } from '../fs-dirent-form-field';
import { useUtilityClasses, FsDirentArticleTemplateRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';


export const FsDirentArticleTemplateCreate: React.FC = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

  return (
    <FsDirentArticleTemplateRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.template.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.template.contentField.label' })}>
          <div className={classes.editor}>
            <MonacoReact height="100%" defaultLanguage="html"
              options={{
                wordBasedSuggestions: 'off',
                minimap: { enabled: false },
              }}
            />
          </div>
        </FsDirentFormField>

      </div>
    </FsDirentArticleTemplateRoot>
  );
};
