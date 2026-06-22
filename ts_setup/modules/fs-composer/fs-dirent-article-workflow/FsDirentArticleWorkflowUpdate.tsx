import React from 'react';
import { Typography, Divider } from '@mui/material';
import { useIntl } from 'react-intl';
import { DatePicker } from '@dxs-ts/xui-datetime';
import { useFsDirent } from '@dxs-ts/fs-api';
import { createWidget } from '../fs-factory';
import { FsDirentFormField, FsDirentSelectMulti, FsDirentSelectSingle, FsDirentTextField } from '../fs-utilities';
import { FsDirentArticleWorkflowProps } from './FsDirentArticleWorkflowProps';
import { useUtilityClasses, FsDirentArticleWorkflowRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';


export const FsDirentArticleWorkflowUpdate: React.FC<FsDirentArticleWorkflowProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { selectOptions, getDirentName } = useFsDirent();
  const dialobForms = selectOptions.dialobs;
  const flows = selectOptions.flows;
  const articles = selectOptions.articles.map(item => ({ value: item.value, label: getDirentName(item.value) ?? item.label }));
  const configOptions = createWidget({ type: 'ARTICLE_WORKFLOW' }).meta.configOptions.map(opt => ({
    value: opt,
    label: intl.formatMessage({ id: `fs.dirent.configOption.${opt}` }),
  }));
  const dialobTags = selectOptions.collectDialobTags(ownerState.dialobFormName);

  return (
    <FsDirentArticleWorkflowRoot className={classes.root}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.service.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.nameField.label' })}>
          <FsDirentTextField required value={ownerState.flowName} placeholder={intl.formatMessage({ id: 'fs.dirent.service.nameField.placeholder' })}
            onChange={ownerState.onChangeName}
          />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.service.dialobFormField.label' })}>
          <FsDirentSelectSingle options={dialobForms} value={ownerState.dialobFormName} onChange={ownerState.onChangeDialobFormName} />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.service.dialobTagField.label' })}>
          <FsDirentSelectSingle options={dialobTags} value={ownerState.dialobFormTag} onChange={ownerState.onChangeDialobFormTag} />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.service.flowField.label' })}>
          <FsDirentSelectSingle options={flows} value={ownerState.flowName} onChange={ownerState.onChangeFlowName} />
        </FsDirentFormField>


        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.service.articlesField.label' })}>
          <FsDirentSelectMulti options={articles} value={ownerState.articles} onChange={ownerState.onChangeArticles} />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}>
          <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />
        </FsDirentFormField>
      </div>

      {ownerState.locales.map((locale) => (
        <div key={locale.value} className={classes.localeRow}>
          <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.locales.labelField' }, { localeCode: locale.label })} >
            <FsDirentTextField value={ownerState.intlValues[locale.value] ?? ''}
              onChange={(value) => ownerState.onChangeIntlValues(locale.value, value)}
            />
          </FsDirentFormField>
        </div>
      ))}

      <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.service.validityStartField.label' })}>
        <DatePicker value={ownerState.validityStart ? new Date(ownerState.validityStart) : null} onChange={(d) => ownerState.onChangeValidityStart(d ?? undefined)} fullWidth size='small' />
      </FsDirentFormField>

      <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.service.validityEndField.label' })}>
        <DatePicker value={ownerState.validityEnd ? new Date(ownerState.validityEnd) : null} onChange={(d) => ownerState.onChangeValidityEnd(d ?? undefined)} fullWidth size='small' />
      </FsDirentFormField>


    </FsDirentArticleWorkflowRoot>
  );
};
