import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import MDEditor from '@uiw/react-md-editor';
import { FsDirentSelectMulti, FsDirentSelectSingle, FsDirentFormField } from '../fs-utilities';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { useUtilityClasses, FsDirentArticlePageRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';


export const FsDirentArticlePageCreate: React.FC = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

  return (
    <FsDirentArticlePageRoot className={classes.root} ownerState={ownerState}>
      <div className={classes.titleRow}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.page.sectionTitle.createNew' })}</Typography>
        <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isDirty} />
      </div>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.page.contentField.label' })}>
          <div data-color-mode={ownerState.isDarkMode ? 'dark' : 'light'}>
            <MDEditor preview="edit" value={ownerState.content} onChange={(val) => ownerState.onChangeContent(val ?? '')} />
          </div>
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.page.articleField.label' })}>
          <FsDirentSelectSingle options={ownerState.articleOptions} value={ownerState.articleId} onChange={ownerState.onChangeArticle} />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.page.localeField.label' })}>
          <FsDirentSelectSingle options={ownerState.localeOptions} value={ownerState.locale} onChange={ownerState.onChangeLocale} />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}>
          <FsDirentSelectMulti options={ownerState.availableConfigOptions} value={ownerState.configOptions as string[]} onChange={ownerState.onChangeConfigOptions} />
        </FsDirentFormField>

      </div>
    </FsDirentArticlePageRoot>
  );
};
