import React from 'react';
import { Typography, Divider, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentSelectSingle } from '../fs-dirent-select-single';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentServiceUpdateProps } from './FsDirentServiceProps';
import { useUtilityClasses, FsDirentServiceRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';


export const FsDirentServiceUpdate: React.FC<FsDirentServiceUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { selectOptions, getConfigOptionsForType } = useFsDirent();
  const dialobForms = selectOptions.dialobs;
  const flows = selectOptions.flows;
  const articles = selectOptions.articles;
  const configOptions = getConfigOptionsForType('service');
  const dialobTags = selectOptions.collectDialobTags(ownerState.dialobFormName);

  return (
    <FsDirentServiceRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.service.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.nameField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.name}
          placeholder={intl.formatMessage({ id: 'fs.dirent.service.nameField.placeholder' })}
          onChange={ownerState.onChangeName}
        />

        <Divider />

        <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.dirent.service.sectionTitle.dialobForm' })}</Typography>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.dialobFormField.label' })}</Typography>
        <FsDirentSelectSingle options={dialobForms} value={ownerState.dialobFormName} onChange={ownerState.onChangeDialobFormName} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.dialobTagField.label' })}</Typography>
        <FsDirentSelectSingle options={dialobTags} value={ownerState.dialobFormTag} onChange={ownerState.onChangeDialobFormTag} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.flowField.label' })}</Typography>
        <FsDirentSelectSingle options={flows} value={ownerState.flowName} onChange={ownerState.onChangeFlowName} />

        <Divider />

        <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.dirent.service.sectionTitle.createLocaleLabels' })}</Typography>

        {ownerState.locales.map((locale) => (
          <div key={locale} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: `fs.dirent.service.labelField.${locale}.label` })}</Typography>
            <FsDirentTextField
              value={ownerState.intlValues[locale] ?? ''}
              placeholder={intl.formatMessage({ id: 'fs.dirent.service.labelField.placeholder' })}
              onChange={(value) => ownerState.onChangeIntlValues(locale, value)}
            />
          </div>
        ))}

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.service.expandToggle.hide' : 'fs.dirent.service.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.validityStartField.label' })}</Typography>
            <FsDirentTextField
              value={ownerState.validityStart}
              placeholder={intl.formatMessage({ id: 'fs.dirent.service.validityStartField.placeholder' })}
              onChange={ownerState.onChangeValidityStart}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.validityEndField.label' })}</Typography>
            <FsDirentTextField
              value={ownerState.validityEnd}
              placeholder={intl.formatMessage({ id: 'fs.dirent.service.validityEndField.placeholder' })}
              onChange={ownerState.onChangeValidityEnd}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.articlesField.label' })}</Typography>
            <FsDirentSelectMulti options={articles} value={ownerState.articles} onChange={ownerState.onChangeArticles} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.configOptionsField.label' })}</Typography>
            <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentServiceRoot>
  );
};
