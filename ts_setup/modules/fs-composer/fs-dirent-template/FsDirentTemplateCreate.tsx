import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import MonacoReact from '@monaco-editor/react';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { useUtilityClasses, FsDirentTemplateRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentTemplateCreateProps } from './FsDirentTemplateProps';


export const FsDirentTemplateCreate: React.FC<FsDirentTemplateCreateProps> = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

  return (
    <FsDirentTemplateRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.template.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.template.contentField.label' })}</Typography>
        <div className={classes.editor}>
          <MonacoReact height="100%" defaultLanguage="html"
            options={{
              wordBasedSuggestions: 'off',
              minimap: { enabled: false },
            }}
          />
        </div>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentTemplateRoot>
  );
};
