import React from 'react';
import { Typography, Divider, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import { DatePicker } from '@dxs-ts/xui-datetime';
import { useFsDirent, useFsu } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentButtonDelete } from '../fs-dirent-button-delete';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentSelectSingle } from '../fs-dirent-select-single';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';
import { FsDirentWorkflowUpdateProps } from './FsDirentWorkflowProps';
import { useUtilityClasses, FsDirentWorkflowRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';


export const FsDirentWorkflowUpdate: React.FC<FsDirentWorkflowUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { selectOptions, getConfigOptionsForType, getDirentName } = useFsDirent();
  const { push } = useFsu();
  const dialobForms = selectOptions.dialobs;
  const flows = selectOptions.flows;
  const articles = selectOptions.articles.map(item => ({ value: item.value, label: getDirentName(item.value) ?? item.label }));
  const configOptions = getConfigOptionsForType('ARTICLE_WORKFLOW');
  const dialobTags = selectOptions.collectDialobTags(ownerState.dialobFormName);

  return (
    <FsDirentWorkflowRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.service.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.nameField.label' })}</Typography>
        <FsDirentTextField required
          value={ownerState.name}
          placeholder={intl.formatMessage({ id: 'fs.dirent.service.nameField.placeholder' })}
          onChange={ownerState.onChangeName}
          onBlur={ownerState.onBlurName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.dialobFormField.label' })}</Typography>
        <FsDirentSelectSingle options={dialobForms} value={ownerState.dialobFormName} onChange={ownerState.onChangeDialobFormName} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.dialobTagField.label' })}</Typography>
        <FsDirentSelectSingle options={dialobTags} value={ownerState.dialobFormTag} onChange={ownerState.onChangeDialobFormTag} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.flowField.label' })}</Typography>
        <FsDirentSelectSingle options={flows} value={ownerState.flowName} onChange={ownerState.onChangeFlowName} />

        <Divider />

        <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.dirent.service.sectionTitle.createLocaleLabels' })}</Typography>

        {ownerState.locales.map((locale) => (
          <div key={locale.value} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: 'fs.dirent.locales.labelField' }, { localeCode: locale.label })}</Typography>
            <FsDirentTextField value={ownerState.intlValues[locale.value] ?? ''}
              onChange={(value) => ownerState.onChangeIntlValues(locale.value, value)}
              onBlur={() => ownerState.onBlurIntlValues(locale.value)}
            />
          </div>
        ))}

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.expandToggle.hide' : 'fs.dirent.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.descriptionField.label' })}</Typography>
            <FsDirentTextField
              value={ownerState.description}
              placeholder={intl.formatMessage({ id: 'fs.dirent.descriptionField.placeholder' })}
              onChange={ownerState.onChangeDescription}
              onBlur={ownerState.onBlurDescription}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.validityStartField.label' })}</Typography>
            <DatePicker value={ownerState.validityStart ? new Date(ownerState.validityStart) : null} onChange={(d) => ownerState.onChangeValidityStart(d ?? undefined)} fullWidth size='small' />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.validityEndField.label' })}</Typography>
            <DatePicker value={ownerState.validityEnd ? new Date(ownerState.validityEnd) : null} onChange={(d) => ownerState.onChangeValidityEnd(d ?? undefined)} fullWidth size='small' />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.labelsField.label' })}</Typography>
            <FsDirentTextFieldAutocomplete options={selectOptions.labels} value={ownerState.tagLabels} onChange={ownerState.onChangeLabels} placeholder={intl.formatMessage({ id: 'fs.dirent.labelsField.placeholder' })} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.articlesField.label' })}</Typography>
            <FsDirentSelectMulti options={articles} value={ownerState.articles} onChange={ownerState.onChangeArticles} />

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
    </FsDirentWorkflowRoot>
  );
};
