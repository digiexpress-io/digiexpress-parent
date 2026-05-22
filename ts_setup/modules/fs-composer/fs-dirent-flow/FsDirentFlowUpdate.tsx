import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import MonacoReact from '@monaco-editor/react';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentButtonDelete } from '../fs-dirent-button-delete';
import { useUtilityClasses, FsDirentFlowRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentFlowUpdateProps } from './FsDirentFlowProps';



export const FsDirentFlowUpdate: React.FC<FsDirentFlowUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentFlowRoot className={classes.root} ownerState={ownerState}>
      <div className={classes.formContainer}>
        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.flow.contentField.label' })}</Typography>
        <div className={classes.editor}>
          <MonacoReact
            height="100%"
            value={ownerState.content}
            defaultLanguage="yaml"
            onChange={(value) => ownerState.onChangeContent(value ?? '')}
            options={{
              wordBasedSuggestions: 'off',
              minimap: { enabled: false },
            }}
          />
        </div>

        <div className={classes.buttonContainer}>
          <FsDirentButtonDelete assetId={props.direntId} />
          <FsDirentButtonCancel />
          <FsDirentButtonSave />
        </div>

      </div>
    </FsDirentFlowRoot>
  );
};
