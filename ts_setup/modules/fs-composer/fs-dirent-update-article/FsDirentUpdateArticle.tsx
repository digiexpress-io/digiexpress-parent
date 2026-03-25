import React from 'react';
import { Typography, Collapse, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { getConfigOptionsForType } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentMultiSelect } from '../fs-dirent-multi-select';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentUpdateArticleProps } from './FsDirentUpdateArticleProps';
import { useUtilityClasses, FsDirentUpdateArticleRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';


export const FsDirentUpdateArticle: React.FC<FsDirentUpdateArticleProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();
  const configOptions = getConfigOptionsForType('article');

  return (
    <FsDirentUpdateArticleRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntUpdate.article.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntUpdate.article.nameField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.name}
          placeholder={intl.formatMessage({ id: 'fs.direntUpdate.article.nameField.placeholder' })}
          onChange={ownerState.onChangeName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntUpdate.article.orderNumberField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.orderNumber}
          placeholder={intl.formatMessage({ id: 'fs.direntUpdate.article.orderNumberField.placeholder' })}
          onChange={ownerState.onChangeOrderNumber}
        />

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.direntUpdate.article.expandToggle.hide' : 'fs.direntUpdate.article.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntUpdate.article.descriptionField.label' })}</Typography>
            <FsDirentTextField
              value={ownerState.description}
              placeholder={intl.formatMessage({ id: 'fs.direntUpdate.article.descriptionField.placeholder' })}
              onChange={ownerState.onChangeDescription}
              multiline minRows={2} maxRows={5}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntUpdate.article.configOptionsField.label' })}</Typography>
            <FsDirentMultiSelect options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntUpdate.article.labelsField.label' })}</Typography>
            <FsDirentTextField
              value={ownerState.labels}
              placeholder={intl.formatMessage({ id: 'fs.direntUpdate.article.labelsField.placeholder' })}
              onChange={ownerState.onChangeLabels}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntUpdate.article.commentsField.label' })}</Typography>
            <FsDirentTextField
              value={ownerState.comments}
              placeholder={intl.formatMessage({ id: 'fs.direntUpdate.article.commentsField.placeholder' })}
              onChange={ownerState.onChangeComments}
              multiline minRows={2} maxRows={5}
            />

            <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.direntUpdate.article.sectionTitle.sharing' })}</Typography>
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
    </FsDirentUpdateArticleRoot>
  );
};
