import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import MDEditor from '@uiw/react-md-editor';
import { FsDirentSelectSingle } from '../fs-dirent-select-single';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { useUtilityClasses, FsDirentPageRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentPageCreateProps } from './FsDirentPageProps';


export const FsDirentPageCreate: React.FC<FsDirentPageCreateProps> = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

  const [content, setContent] = React.useState('');

  return (
    <FsDirentPageRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.page.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.localeField.label' })}</Typography>
        <FsDirentSelectSingle options={ownerState.localeOptions} value={ownerState.localeCode} onChange={ownerState.onChangeLocaleCode} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.articleField.label' })}</Typography>
        <FsDirentSelectSingle options={ownerState.articleOptions} value={ownerState.articleId} onChange={ownerState.onChangeArticleId} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.configOptionsField.label' })}</Typography>
        <FsDirentSelectMulti options={ownerState.availableConfigOptions} value={ownerState.configOptions as string[]} onChange={ownerState.onChangeConfigOptions} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.contentField.label' })}</Typography>
        <div data-color-mode={ownerState.isDarkMode ? 'dark' : 'light'}>
          <MDEditor value={content} onChange={(val) => setContent(val ?? '')} />
        </div>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentPageRoot>
  );
};
