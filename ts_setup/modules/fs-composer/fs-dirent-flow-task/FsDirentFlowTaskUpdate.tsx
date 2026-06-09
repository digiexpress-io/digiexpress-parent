import React from 'react';
import { Typography, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import MonacoReact from '@monaco-editor/react';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';
import { useUtilityClasses, FsDirentFlowTaskRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentFlowTaskUpdateProps } from './FsDirentFlowTaskProps';


export const FsDirentFlowTaskUpdate: React.FC<FsDirentFlowTaskUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { selectOptions } = useFsDirent();

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

      <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
        {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.expandToggle.hide' : 'fs.dirent.expandToggle.show' })}
        <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
      </div>

      <Collapse in={ownerState.isExpanded}>
        <div className={classes.optionalFields}>
          <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.labelsField.label' })}</Typography>
          <FsDirentTextFieldAutocomplete
            options={selectOptions.labels}
            value={ownerState.tagLabels}
            onChange={ownerState.onChangeLabels}
            placeholder={intl.formatMessage({ id: 'fs.dirent.labelsField.placeholder' })}
          />
        </div>
      </Collapse>


    </FsDirentFlowTaskRoot>
  );
};
