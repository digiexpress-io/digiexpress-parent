import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import MonacoReact from '@monaco-editor/react';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentButtonDelete } from '../fs-dirent-button-delete';
import { useUtilityClasses, FsDirentFlowTaskRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentFlowTaskUpdateProps } from './FsDirentFlowTaskProps';
import { useFsu } from '@dxs-ts/fs-api';


export const FsDirentFlowTaskUpdate: React.FC<FsDirentFlowTaskUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { push } = useFsu();

  return (
    <FsDirentFlowTaskRoot className={classes.root} ownerState={ownerState}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.flow_task.taskValueField.label' })}</Typography>
        <div className={classes.editor}>
          <MonacoReact
            height="100%"
            defaultLanguage="java"
            value={ownerState.taskValue}
            onChange={(v) => ownerState.onChangeTaskValue(v ?? '')}
            options={{
              wordBasedSuggestions: 'off',
              minimap: { enabled: true },
            }}
          />
        </div>

        <div className={classes.buttonContainer}>
          <FsDirentButtonDelete assetId={props.direntId} />
          <FsDirentButtonCancel onClick={ownerState.onCancel} />
          <FsDirentButtonSave onClick={() => push(ownerState.id)} />
        </div>


    </FsDirentFlowTaskRoot>
  );
};
