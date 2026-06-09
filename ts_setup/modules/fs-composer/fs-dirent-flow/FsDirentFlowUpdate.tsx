import React from 'react';
import { Typography, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import MonacoReact from '@monaco-editor/react';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { useUtilityClasses, FsDirentFlowRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentFlowUpdateProps } from './FsDirentFlowProps';
import { useFsDirent } from '@dxs-ts/fs-api';


export const FsDirentFlowUpdate: React.FC<FsDirentFlowUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { getConfigOptionsForType } = useFsDirent();
  const configOptions = getConfigOptionsForType('FLOW');

  return (
    <FsDirentFlowRoot className={classes.root} ownerState={ownerState}>
      <div className={classes.formContainer}>
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

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.expandToggle.hide' : 'fs.dirent.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}</Typography>
            <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />
          </div>
        </Collapse>


      </div>
    </FsDirentFlowRoot>
  );
};
