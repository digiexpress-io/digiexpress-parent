import React from 'react';
import { Typography, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentArticleRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentArticleUpdateProps } from './FsDirentArticleProps';


export const FsDirentArticleUpdate: React.FC<FsDirentArticleUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { getConfigOptionsForType } = useFsDirent();
  const configOptions = getConfigOptionsForType('article');

  return (
    <FsDirentArticleRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.article.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.article.nameField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.name}
          placeholder={intl.formatMessage({ id: 'fs.dirent.article.nameField.placeholder' })}
          onChange={ownerState.onChangeName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.article.orderNumberField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.orderNumber}
          placeholder={intl.formatMessage({ id: 'fs.dirent.article.orderNumberField.placeholder' })}
          onChange={ownerState.onChangeOrderNumber}
        />

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.article.expandToggle.hide' : 'fs.dirent.article.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.article.descriptionField.label' })}</Typography>
            <FsDirentTextField
              value={ownerState.description}
              placeholder={intl.formatMessage({ id: 'fs.dirent.article.descriptionField.placeholder' })}
              onChange={ownerState.onChangeDescription}
              multiline minRows={2} maxRows={5}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.article.configOptionsField.label' })}</Typography>
            <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.article.labelsField.label' })}</Typography>
            <FsDirentTextField
              value={ownerState.labels}
              placeholder={intl.formatMessage({ id: 'fs.dirent.article.labelsField.placeholder' })}
              onChange={ownerState.onChangeLabels}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.article.commentsField.label' })}</Typography>
            <FsDirentTextField
              value={ownerState.comments}
              placeholder={intl.formatMessage({ id: 'fs.dirent.article.commentsField.placeholder' })}
              onChange={ownerState.onChangeComments}
              multiline minRows={2} maxRows={5}
            />

            <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.dirent.article.sectionTitle.sharing' })}</Typography>
            <div className={classes.sectionBox}>
              <Typography className={classes.sectionContent}>TODO</Typography>
            </div>
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentArticleRoot>
  );
};
