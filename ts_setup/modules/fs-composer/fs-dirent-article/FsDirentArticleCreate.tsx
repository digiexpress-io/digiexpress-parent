import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsIcon, FsIcons } from '../fs-theme';
import { createWidget } from '../fs-factory';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentArticleRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';


export const FsDirentArticleCreate: React.FC = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();
  const configOptions = createWidget({ type: 'ARTICLE' }).meta.configOptions.map(opt => ({
    value: opt,
    label: intl.formatMessage({ id: `fs.dirent.configOption.${opt}` }),
  }));

  return (
    <FsDirentArticleRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.article.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        {ownerState.parentArticle && (
          <>
            <Box display='flex' justifyContent='space-between' alignItems='center'>
              <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.article.parentArticleField.label' })}</Typography>
              <FsIcon icon={FsIcons.Info} small tooltip={intl.formatMessage({ id: 'fs.dirent.article.parentArticleField.desc' })} />
            </Box>
            <FsDirentTextField value={ownerState.parentArticlePath} disabled />
          </>
        )}

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.nameField.label' })}</Typography>
        <FsDirentTextField
          required
          placeholder={intl.formatMessage({ id: 'fs.dirent.article.nameField.placeholder' })}
          value={ownerState.name}
          onChange={ownerState.onChangeName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.article.orderNumberField.label' })}</Typography>
        <FsDirentTextField
          required
          placeholder={intl.formatMessage({ id: 'fs.dirent.article.orderNumberField.placeholder' })}
          value={ownerState.orderNumber}
          onChange={ownerState.onChangeOrderNumber}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}</Typography>
        <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />

        <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.dirent.article.sectionTitle.sharing' })}</Typography>
        <div className={classes.sectionBox}>
          <Typography className={classes.sectionContent}>TODO</Typography>
        </div>

      </div>
    </FsDirentArticleRoot>
  );
};
