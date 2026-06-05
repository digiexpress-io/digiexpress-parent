import React from 'react';
import { Typography, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import MDEditor from '@uiw/react-md-editor';

import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentSelectSingle } from '../fs-dirent-select-single';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentButtonDelete } from '../fs-dirent-button-delete';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';
import { useFsu } from '@dxs-ts/fs-api';
import { FsDirentLoader } from '../fs-dirent-loader';
import { useUtilityClasses, FsDirentArticlePageRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentArticlePageUpdateProps } from './FsDirentArticlePageProps';


export const FsDirentArticlePageUpdate: React.FC<FsDirentArticlePageUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { push } = useFsu();

  const [isLoading, setIsLoading] = React.useState(true);
  React.useEffect(() => {
    const timer = setTimeout(() => setIsLoading(false), 300);
    return () => clearTimeout(timer);
  }, []);

  if (ownerState.isLoading || isLoading) {
    return <FsDirentLoader />;
  }

  return (
    <FsDirentArticlePageRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.page.sectionTitle.edit' })}{": "}{ownerState.dirent?.name}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.contentField.label' })}</Typography>
        <div data-color-mode={ownerState.isDarkMode ? 'dark' : 'light'}>
          <MDEditor preview="edit" value={ownerState.content} onChange={(val) => ownerState.onChangeContent(val ?? '')} textareaProps={{ onBlur: ownerState.onBlurContent }} />
        </div>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.articleField.label' })}</Typography>
        <FsDirentTextField disabled value={ownerState.articleName} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.localeField.label' })}</Typography>
        <FsDirentSelectSingle options={ownerState.localeOptions} value={ownerState.locale} onChange={ownerState.onChangeLocale} />

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
              onBlur={ownerState.onBlurDescription}
              multiline
              minRows={2} maxRows={4}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}</Typography>
            <FsDirentSelectMulti options={ownerState.availableConfigOptions} value={ownerState.configOptions as string[]} onChange={ownerState.onChangeConfigOptions} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.labelsField.label' })}</Typography>
            <FsDirentTextFieldAutocomplete
              options={ownerState.labelOptions}
              value={ownerState.labels}
              onChange={ownerState.onChangeLabels}
              placeholder={intl.formatMessage({ id: 'fs.dirent.labelsField.placeholder' })}
            />
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonDelete assetId={props.direntId} />
          <FsDirentButtonCancel onClick={ownerState.onCancel} />
          <FsDirentButtonSave onClick={() => push(ownerState.id)} />
        </div>

      </div>
    </FsDirentArticlePageRoot>
  );
};
