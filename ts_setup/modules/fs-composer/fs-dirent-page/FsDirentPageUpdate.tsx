import React from 'react';
import { Typography, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import MDEditor from '@uiw/react-md-editor';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentSelectSingle } from '../fs-dirent-select-single';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentButtonDelete } from '../fs-dirent-button-delete';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentPageRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentPageUpdateProps } from './FsDirentPageProps';


export const FsDirentPageUpdate: React.FC<FsDirentPageUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { updateDirent } = useFsDirent();

  return (
    <FsDirentPageRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.page.sectionTitle.edit' })}{": "}{ownerState.dirent?.name}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.contentField.label' })}</Typography>
        <div data-color-mode={ownerState.isDarkMode ? 'dark' : 'light'}>
          <MDEditor preview="edit" value={ownerState.content} onChange={(val) => updateDirent(props.direntId, { props: { content: val ?? '' } as Fs.PageProps })} />
        </div>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.articleField.label' })}</Typography>
        <FsDirentTextField disabled value={ownerState.articleName} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.localeField.label' })}</Typography>
        <FsDirentSelectSingle options={ownerState.localeOptions} value={ownerState.localeCode} onChange={ownerState.onChangeLocaleCode} />

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.expandToggle.hide' : 'fs.dirent.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.descriptionField.label' })}</Typography>
            <FsDirentTextField
              value={ownerState.description}
              placeholder={intl.formatMessage({ id: 'fs.dirent.page.descriptionField.placeholder' })}
              onChange={ownerState.onChangeDescription}
              multiline
              minRows={2} maxRows={4}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}</Typography>
            <FsDirentSelectMulti options={ownerState.availableConfigOptions} value={ownerState.configOptions as string[]} onChange={ownerState.onChangeConfigOptions} />
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonDelete assetId={props.direntId} />
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentPageRoot>
  );
};
