import React from 'react';
import { Typography, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentButtonDelete } from '../fs-dirent-button-delete';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentLinkRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentLinkUpdateProps } from './FsDirentLinkProps';

export const FsDirentLinkUpdate: React.FC<FsDirentLinkUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { selectOptions, getConfigOptionsForType } = useFsDirent();
  const articles = selectOptions.articles;
  const configOptions = getConfigOptionsForType('ARTICLE_LINK');

  return (
    <FsDirentLinkRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.link.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.link.urlValueField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.urlValue}
          placeholder={intl.formatMessage({ id: 'fs.dirent.link.urlValueField.placeholder' })}
          onChange={ownerState.onChangeUrlValue}
        />

        {ownerState.locales.map((locale) => (
          <div key={locale.label} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: `fs.dirent.link.labelField.${locale}.label` })}</Typography>
            <FsDirentTextField
              value={ownerState.intlValues[locale.label] ?? ''}
              placeholder={intl.formatMessage({ id: 'fs.dirent.link.labelField.placeholder' })}
              onChange={(value) => ownerState.onChangeIntlValue(locale.label, value)}
            />
          </div>
        ))}

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.link.expandToggle.hide' : 'fs.dirent.link.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.link.descriptionField.label' })}</Typography>
            <FsDirentTextField
              value={ownerState.description}
              placeholder={intl.formatMessage({ id: 'fs.dirent.link.descriptionField.placeholder' })}
              onChange={ownerState.onChangeDescription}
              multiline minRows={2} maxRows={5}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.link.articlesField.label' })}</Typography>
            <FsDirentSelectMulti options={articles} value={[]} onChange={(_value) => { }} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.link.configOptionsField.label' })}</Typography>
            <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonDelete assetId={props.direntId} />
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentLinkRoot>
  );
};
