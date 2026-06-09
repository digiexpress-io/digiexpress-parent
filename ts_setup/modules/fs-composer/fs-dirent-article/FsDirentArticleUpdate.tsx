import React from 'react';
import { Typography, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentArticleRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentArticleUpdateProps } from './FsDirentArticleProps';
export const FsDirentArticleUpdate: React.FC<FsDirentArticleUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { getConfigOptionsForType } = useFsDirent();
  const configOptions = getConfigOptionsForType('ARTICLE');

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

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.expandToggle.hide' : 'fs.dirent.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}</Typography>
            <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.article.commentsField.label' })}</Typography>
            <FsDirentTextField value={'todo'}
              placeholder={intl.formatMessage({ id: 'fs.dirent.article.commentsField.placeholder' })}
              multiline minRows={2} maxRows={5}
            />

            <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.dirent.article.sectionTitle.sharing' })}</Typography>
            <div className={classes.sectionBox}>
              <Typography className={classes.sectionContent}>TODO</Typography>
            </div>
          </div>
        </Collapse>

      </div>
    </FsDirentArticleRoot>
  );
};
