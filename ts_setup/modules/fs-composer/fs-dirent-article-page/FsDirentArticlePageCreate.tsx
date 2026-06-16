import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import MDEditor from '@uiw/react-md-editor';
import { FsDirentSelectSingle } from '../fs-dirent-select-single';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { useUtilityClasses, FsDirentArticlePageRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';


export const FsDirentArticlePageCreate: React.FC = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

  return (
    <FsDirentArticlePageRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.page.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.contentField.label' })}</Typography>
        <div data-color-mode={ownerState.isDarkMode ? 'dark' : 'light'}>
          <MDEditor preview="edit" value={ownerState.content} onChange={(val) => ownerState.onChangeContent(val ?? '')} />
        </div>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.articleField.label' })}</Typography>
        <FsDirentSelectSingle options={ownerState.articleOptions} value={ownerState.articleId} onChange={ownerState.onChangeArticle} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.localeField.label' })}</Typography>
        <FsDirentSelectSingle options={ownerState.localeOptions} value={ownerState.locale} onChange={ownerState.onChangeLocale} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}</Typography>
        <FsDirentSelectMulti options={ownerState.availableConfigOptions} value={ownerState.configOptions as string[]} onChange={ownerState.onChangeConfigOptions} />

      </div>
    </FsDirentArticlePageRoot>
  );
};
