import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import MonacoReact from '@monaco-editor/react';
import { useUtilityClasses, FsDirentArticleTemplateRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentArticleTemplateUpdateProps } from './FsDirentArticleTemplateProps';


export const FsDirentArticleTemplateUpdate: React.FC<FsDirentArticleTemplateUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentArticleTemplateRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.template.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.template.contentField.label' })}</Typography>
        <div className={classes.editor}>
          <MonacoReact
            height="100%"
            value={ownerState.content}
            defaultLanguage="html"
            onChange={(value) => ownerState.onChangeContent(value ?? '')}
            options={{
              wordBasedSuggestions: 'off',
              minimap: { enabled: false },
            }}
          />
        </div>

      </div>
    </FsDirentArticleTemplateRoot>
  );
};
