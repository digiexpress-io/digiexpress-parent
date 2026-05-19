import React from 'react';
import { Typography, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentPhoneRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentPhoneCreateProps } from './FsDirentPhoneProps';


export const FsDirentPhoneCreate: React.FC<FsDirentPhoneCreateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();
  const { selectOptions, getConfigOptionsForType } = useFsDirent();

  const articles = selectOptions.articles;
  const configOptions = getConfigOptionsForType('ARTICLE_LINK');

  const [phoneValue, setPhoneValue] = React.useState('');
  const [selectedArticles, setSelectedArticles] = React.useState<string[]>([]);
  const [selectedConfigOptions, setSelectedConfigOptions] = React.useState<string[]>([]);

  return (
    <FsDirentPhoneRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.phone.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.phone.phoneValueField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.phone.phoneValueField.placeholder' })} required value={phoneValue} onChange={setPhoneValue} />

        {ownerState.locales.map((locale) => (
          <div key={locale.label} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: `fs.dirent.phone.labelField.${locale}.label` })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.phone.labelField.placeholder' })} />
          </div>
        ))}

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.expandToggle.hide' : 'fs.dirent.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.phone.descriptionField.label' })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.phone.descriptionField.placeholder' })}
              multiline minRows={2} maxRows={5}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.phone.articlesField.label' })}</Typography>
            <FsDirentSelectMulti options={articles} value={selectedArticles} onChange={setSelectedArticles} />
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}</Typography>
            <FsDirentSelectMulti options={configOptions} value={selectedConfigOptions} onChange={setSelectedConfigOptions} />
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentPhoneRoot>
  );
};
