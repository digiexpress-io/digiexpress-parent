import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { createWidget } from '../fs-factory';
import { FsDirentSelectMulti, FsDirentTextField, FsDirentFormField, FsDirentSelectSingle } from '../fs-utilities';
import { useUtilityClasses, FsDirentArticleLinkRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentArticleLinkProps } from './FsDirentArticleLinkProps';

export const FsDirentArticleLinkUpdate: React.FC<FsDirentArticleLinkProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { selectOptions, getDirentName } = useFsDirent();
  const articles = selectOptions.articles.map(item => ({ value: item.value, label: getDirentName(item.value) ?? item.label }));
  const configOptions = createWidget({ type: 'ARTICLE_LINK' }).meta.configOptions.map(opt => ({
    value: opt,
    label: intl.formatMessage({ id: `fs.dirent.configOption.${opt}` }),
  }));
  const linkTypeOptions = selectOptions.linkTypes.map(v => ({
    value: v,
    label: intl.formatMessage({ id: `fs.dirent.link.contentType.${v}` }),
  }));



  return (
    <FsDirentArticleLinkRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.link.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.link.urlValueField.label' })}>
          <FsDirentTextField
            value={ownerState.urlValue}
            placeholder={intl.formatMessage({ id: 'fs.dirent.link.urlValueField.placeholder' })}
            onChange={ownerState.onChangeUrlValue}
          />
        </FsDirentFormField>

        <FsDirentFormField
          label={intl.formatMessage({ id: 'fs.dirent.link.contentTypeField.label' })}
          helperText={intl.formatMessage({ id: `fs.dirent.link.contentType.${ownerState.contentType}.desc` })}
        >
          <FsDirentSelectSingle options={linkTypeOptions} value={ownerState.contentType} onChange={ownerState.onChangeContentType} />
        </FsDirentFormField>


        {ownerState.locales.map((locale) => (
          <div key={locale.value} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: 'fs.dirent.locales.labelField' }, { localeCode: locale.label })}</Typography>
            <FsDirentTextField value={ownerState.intlValues[locale.value] ?? ''} onChange={(value) => ownerState.onChangeIntlValue(locale.value, value)} />
          </div>
        ))}

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.link.articlesField.label' })}>
          <FsDirentSelectMulti options={articles} value={ownerState.articles} onChange={ownerState.onChangeArticles} />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}>
          <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />
        </FsDirentFormField>

      </div>
    </FsDirentArticleLinkRoot>
  );
};
