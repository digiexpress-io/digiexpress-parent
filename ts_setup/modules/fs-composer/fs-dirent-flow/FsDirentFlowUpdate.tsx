import React from 'react';
import { Typography, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import MonacoReact from '@monaco-editor/react';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentButtonDelete } from '../fs-dirent-button-delete';
import { useUtilityClasses, FsDirentFlowRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentFlowUpdateProps } from './FsDirentFlowProps';
import { useFsu, useFsDirent } from '@dxs-ts/fs-api';



export const FsDirentFlowUpdate: React.FC<FsDirentFlowUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { push } = useFsu();
  const { selectOptions, getConfigOptionsForType } = useFsDirent();
  const configOptions = getConfigOptionsForType('FLOW');

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

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.expandToggle.hide' : 'fs.dirent.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.descriptionField.label' })}</Typography>
            <FsDirentTextField
              value={ownerState.assetDescription.text}
              placeholder={intl.formatMessage({ id: 'fs.dirent.descriptionField.placeholder' })}
              onChange={ownerState.onChangeDescription}
              multiline minRows={2} maxRows={5}
              onBlur={ownerState.onBlurDescription}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.labelsField.label' })}</Typography>
            <FsDirentTextFieldAutocomplete options={selectOptions.labels} value={ownerState.tagLabels} onChange={ownerState.onChangeLabels} placeholder={intl.formatMessage({ id: 'fs.dirent.labelsField.placeholder' })} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}</Typography>
            <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonDelete assetId={props.direntId} />
          <FsDirentButtonCancel onClick={ownerState.onCancel} />
          <FsDirentButtonSave onClick={() => push(ownerState.id)} />
        </div>

      </div>
    </FsDirentFlowRoot>
  );
};
