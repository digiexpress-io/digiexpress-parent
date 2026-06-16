import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { createWidget } from '../fs-factory';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentArticleRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentArticleProps } from './FsDirentArticleProps';


export const FsDirentArticleUpdate: React.FC<FsDirentArticleProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const configOptions = createWidget({ type: 'ARTICLE' }).meta.configOptions.map(opt => ({
    value: opt,
    label: intl.formatMessage({ id: `fs.dirent.configOption.${opt}` }),
  }));

  return (
    <FsDirentArticleRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.article.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>

      <div className={classes.formContainer}>
        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.nameField.label' })}</Typography>
        <FsDirentTextField required value={ownerState.name}
          placeholder={intl.formatMessage({ id: 'fs.dirent.article.nameField.placeholder' })}
          onChange={ownerState.onChangeName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.article.orderNumberField.label' })}</Typography>
        <FsDirentTextField required value={ownerState.orderNumber}
          placeholder={intl.formatMessage({ id: 'fs.dirent.article.orderNumberField.placeholder' })}
          onChange={ownerState.onChangeOrderNumber}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}</Typography>
        <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />
      </div>
    </FsDirentArticleRoot>
  )
};
