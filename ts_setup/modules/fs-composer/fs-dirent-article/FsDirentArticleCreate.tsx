import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { createWidget } from '../fs-factory';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentSelectMulti, FsDirentSelectSingle, FsDirentTextField, FsDirentFormField } from '../fs-utilities';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { useUtilityClasses, FsDirentArticleRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';


export const FsDirentArticleCreate: React.FC = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();
  const { selectOptions, getDirentName } = useFsDirent();
  const configOptions = createWidget({ type: 'ARTICLE' }).meta.configOptions.map(opt => ({
    value: opt,
    label: intl.formatMessage({ id: `fs.dirent.configOption.${opt}` }),
  }));
  const articleOptions = selectOptions.articles.map(item => ({ value: item.value, label: getDirentName(item.value) ?? item.label }));

  return (
    <FsDirentArticleRoot className={classes.root}>
      <div className={classes.titleRow}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.article.sectionTitle.createNew' })}</Typography>
        <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isDirty} />
      </div>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.nameField.label' })}>
          <FsDirentTextField required placeholder={intl.formatMessage({ id: 'fs.dirent.article.nameField.placeholder' })}
            value={ownerState.name}
            onChange={ownerState.onChangeName}
          />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.article.parentArticleField.label' })}>
          <FsDirentSelectSingle allowNone options={articleOptions} value={ownerState.parentId} onChange={ownerState.onChangeParentId} />
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
