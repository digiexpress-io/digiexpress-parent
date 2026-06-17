import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsIcon, FsIcons } from '../fs-theme';
import { createWidget } from '../fs-factory';
import { FsDirentSelectMulti, FsDirentTextField, FsDirentFormField } from '../fs-utilities';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
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
      <div className={classes.titleRow}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.article.sectionTitle.createNew' })}</Typography>
        <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isDirty} />
      </div>
      <div className={classes.formContainer}>
        {ownerState.parentArticle && (
          <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.article.parentArticleField.label' })}>
            <>
              <Box display='flex' justifyContent='space-between' alignItems='center'>
                <FsIcon icon={FsIcons.Info} small tooltip={intl.formatMessage({ id: 'fs.dirent.article.parentArticleField.desc' })} />
              </Box>
              <FsDirentTextField value={ownerState.parentArticlePath} disabled />
            </>

          </FsDirentFormField>
        )}

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.nameField.label' })}>
          <FsDirentTextField required placeholder={intl.formatMessage({ id: 'fs.dirent.article.nameField.placeholder' })}
            value={ownerState.name}
            onChange={ownerState.onChangeName}
          />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.article.orderNumberField.label' })}>
          <FsDirentTextField required placeholder={intl.formatMessage({ id: 'fs.dirent.article.orderNumberField.placeholder' })}
            value={ownerState.orderNumber}
            onChange={ownerState.onChangeOrderNumber}
          />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}>
          <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />
        </FsDirentFormField>

      </div>
    </FsDirentArticleRoot>
  );
};
