import React from 'react';
import MonacoReact, { OnChange } from '@monaco-editor/react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentFormField } from '../fs-utilities';
import { useUtilityClasses, FsDirentPrintoutPageRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentPrintoutPageProps } from './FsDirentPrintoutPageProps';

export const FsDirentPrintoutPageUpdate: React.FC<FsDirentPrintoutPageProps> = ({ direntId }) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState({ direntId });
  const classes = useUtilityClasses();

  const handleChange: OnChange = (value) => {
    ownerState.onChangeContent(value ?? '');
  };

  return (
    <FsDirentPrintoutPageRoot className={classes.root}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printoutPage.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.printoutPage.contentField.label' })}>
          <MonacoReact
            height='100vh'
            value={ownerState.content}
            defaultLanguage='yaml'
            theme={'vs'}
            onChange={handleChange}
            options={{
              wordBasedSuggestions: 'off',
              minimap: { enabled: false },
            }}
          />
        </FsDirentFormField>

      </div>
    </FsDirentPrintoutPageRoot>
  );
};
