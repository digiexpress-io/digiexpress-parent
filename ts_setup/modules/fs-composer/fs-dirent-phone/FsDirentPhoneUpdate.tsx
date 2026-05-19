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
import { useUtilityClasses, FsDirentPhoneRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentPhoneUpdateProps } from './FsDirentPhoneProps';


export const FsDirentPhoneUpdate: React.FC<FsDirentPhoneUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { selectOptions, getConfigOptionsForType } = useFsDirent();
  const articles = selectOptions.articles;
  const configOptions = getConfigOptionsForType('ARTICLE_LINK');
  //TODO phoneValue
  return (
    <FsDirentPhoneRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.phone.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.phone.phoneValueField.label' })}</Typography>
        <FsDirentTextField required
          value={/*ownerState.phoneValue*/ undefined} placeholder={intl.formatMessage({ id: 'fs.dirent.phone.phoneValueField.placeholder' })}
          onChange={ownerState.onChangePhoneValue}
        />

        {ownerState.locales.map((locale) => (
          <div key={locale.value} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: `fs.dirent.phone.labelField.${locale}.label` })}</Typography>
            <FsDirentTextField
              value={ownerState.intlValues[locale.label] ?? ''}
              placeholder={intl.formatMessage({ id: 'fs.dirent.phone.labelField.placeholder' })}
              onChange={(value) => ownerState.onChangeIntlValue(locale.label, value)}
            />
          </div>
        ))}

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.expandToggle.hide' : 'fs.dirent.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.phone.descriptionField.label' })}</Typography>
            <FsDirentTextField
              value={ownerState.description}
              placeholder={intl.formatMessage({ id: 'fs.dirent.phone.descriptionField.placeholder' })}
              onChange={ownerState.onChangeDescription}
              multiline minRows={2} maxRows={5}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.phone.articlesField.label' })}</Typography>
            <FsDirentSelectMulti options={articles} value={[]} onChange={(_value) => { }} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}</Typography>
            <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonDelete assetId={props.direntId} />
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentPhoneRoot>
  );
};
