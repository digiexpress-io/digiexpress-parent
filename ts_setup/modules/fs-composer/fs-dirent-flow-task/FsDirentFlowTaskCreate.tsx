import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import MonacoReact from '@monaco-editor/react';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentFlowTaskRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentFlowTaskCreateProps } from './FsDirentFlowTaskProps';


export const FsDirentFlowTaskCreate: React.FC<FsDirentFlowTaskCreateProps> = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

  const [taskName, setTaskName] = React.useState('');

  return (
    <FsDirentFlowTaskRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.flow_task.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.flow_task.taskNameField.label' })}</Typography>
        <FsDirentTextField
          placeholder={intl.formatMessage({ id: 'fs.dirent.flow_task.taskNameField.placeholder' })}
          required
          value={taskName}
          onChange={setTaskName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.flow_task.taskValueField.label' })}</Typography>
        <div className={classes.editor}>
          <MonacoReact
            height="100%"
            defaultLanguage="yaml"
            options={{
              wordBasedSuggestions: 'off',
              minimap: { enabled: false },
            }}
          />
        </div>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonSave />
        </div>

      </div>
    </FsDirentFlowTaskRoot>
  );
};
