import React from 'react';
import { Typography, Collapse, Divider } from '@mui/material';
import { useIntl } from 'react-intl';
import { collectArticles, mockFsData, getConfigOptionsForType } from '@dxs-ts/fs-api';
import { FsDirentMultiSelect } from '../fs-dirent-multi-select';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentCreateLinkProps } from './FsDirentCreateLinkProps';
import { useUtilityClasses, FsDirentCreateLinkRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

const articles = collectArticles(mockFsData);


export const FsDirentCreateLink: React.FC<FsDirentCreateLinkProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();
  const configOptions = getConfigOptionsForType('link');
  const [selectedArticles, setSelectedArticles] = React.useState<string[]>([]);
  const [selectedConfigOptions, setSelectedConfigOptions] = React.useState<string[]>([]);

  return (
    <FsDirentCreateLinkRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntCreate.link.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.link.valueField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.link.valueField.placeholder' })} />

        <Divider />

        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntCreate.link.sectionTitle.createLocaleLabels' })}</Typography>


        {ownerState.locales.map((locale) => (
          <div key={locale} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: `fs.direntCreate.link.labelField.${locale}.label` })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.link.labelField.placeholder' })} />
          </div>
        ))}

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.direntCreate.link.expandToggle.hide' : 'fs.direntCreate.link.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.link.articlesField.label' })}</Typography>
            <FsDirentMultiSelect options={articles} value={selectedArticles} onChange={setSelectedArticles} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.link.configOptionsField.label' })}</Typography>
            <FsDirentMultiSelect options={configOptions} value={selectedConfigOptions} onChange={setSelectedConfigOptions} />
            {selectedConfigOptions.includes('devMode') && (
              <Typography className={classes.configOptionDescription}>{intl.formatMessage({ id: 'fs.direntCreate.configOption.devMode.description' })}</Typography>
            )}
            {selectedConfigOptions.includes('disabledMode') && (
              <Typography className={classes.configOptionDescription}>{intl.formatMessage({ id: 'fs.direntCreate.configOption.disabledMode.description' })}</Typography>
            )}
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentCreateLinkRoot>
  );
};


