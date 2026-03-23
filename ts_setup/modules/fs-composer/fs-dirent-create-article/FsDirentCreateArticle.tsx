import React from 'react';
import { Typography, Collapse, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentMultiSelect } from '../fs-dirent-multi-select';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentCreateArticleProps } from './FsDirentCreateArticleProps';
import { useUtilityClasses, FsDirentCreateArticleRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

const CONFIG_OPTIONS = [
  { value: 'devMode', label: 'Development mode' },
  { value: 'assignableMode', label: 'Assignable mode' },
  { value: 'disabledMode', label: 'Disabled mode' },
  { value: 'anonymousMode', label: 'Anonymous mode' },
];

export const FsDirentCreateArticle: React.FC<FsDirentCreateArticleProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();
  const [selectedConfigOptions, setSelectedConfigOptions] = React.useState<string[]>([]);

  return (
    <FsDirentCreateArticleRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntCreate.article.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        {ownerState.parentArticle && (
          <>
            <Box display='flex' justifyContent='space-between' alignItems='center'>
              <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.article.parentArticleField.label' })}</Typography>
              <FsIcon icon={FsIcons.Info} small tooltip={intl.formatMessage({ id: 'fs.direntCreate.article.parentArticleField.desc' })} />
            </Box>
            <FsDirentTextField
              value={ownerState.parentArticlePath}
              disabled
            />
          </>
        )}

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.article.nameField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.article.nameField.placeholder' })} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.article.orderNumberField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.article.orderNumberField.placeholder' })} />

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.direntCreate.article.expandToggle.hide' : 'fs.direntCreate.article.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.article.descriptionField.label' })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.article.descriptionField.placeholder' })}
              multiline minRows={2} maxRows={5}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.article.configOptionsField.label' })}</Typography>
            <FsDirentMultiSelect options={CONFIG_OPTIONS} value={selectedConfigOptions} onChange={setSelectedConfigOptions} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.article.labelsField.label' })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.article.labelsField.placeholder' })} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.article.commentsField.label' })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.article.commentsField.placeholder' })}
              multiline minRows={2} maxRows={5}
            />

            <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.direntCreate.article.sectionTitle.sharing' })}</Typography>
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
    </FsDirentCreateArticleRoot>
  );
};

