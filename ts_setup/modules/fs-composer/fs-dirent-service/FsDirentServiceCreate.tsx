import React from 'react';
import { Typography, Divider, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import { collectArticles, collectDialobs, collectDialobTags, collectFlows, mockFsData, mockFsDirentProperties, getConfigOptionsForType } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentSingleSelect } from '../fs-dirent-single-select';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentServiceCreateProps } from './FsDirentServiceProps';
import { useUtilityClasses, FsDirentServiceRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';


const dialobForms = collectDialobs(mockFsData);
const flows = collectFlows(mockFsData);
const articles = collectArticles(mockFsData);

export const FsDirentServiceCreate: React.FC<FsDirentServiceCreateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState(props);
  const classes = useUtilityClasses();
  const configOptions = getConfigOptionsForType('service');

  const [selectedDialobForm, setSelectedDialobForm] = React.useState<string>('');
  const [selectedDialobTag, setSelectedDialobTag] = React.useState<string>('');
  const dialobTags = collectDialobTags(selectedDialobForm, mockFsDirentProperties);

  const [selectedFlow, setSelectedFlow] = React.useState<string>('');
  const [selectedArticles, setSelectedArticles] = React.useState<string[]>([]);
  const [selectedConfigOptions, setSelectedConfigOptions] = React.useState<string[]>([]);

  return (
    <FsDirentServiceRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.service.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.nameField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.service.nameField.placeholder' })} />

        <Divider />

        <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.dirent.service.sectionTitle.dialobForm' })}</Typography>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.dialobFormField.label' })}</Typography>
        <FsDirentSingleSelect options={dialobForms} value={selectedDialobForm} onChange={setSelectedDialobForm} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.dialobTagField.label' })}</Typography>
        <FsDirentSingleSelect options={dialobTags} value={selectedDialobTag} onChange={setSelectedDialobTag} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.flowField.label' })}</Typography>
        <FsDirentSingleSelect options={flows} value={selectedFlow} onChange={setSelectedFlow} />

        <Divider />

        <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.dirent.service.sectionTitle.createLocaleLabels' })}</Typography>

        {ownerState.locales.map((locale) => (
          <div key={locale} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: `fs.dirent.service.labelField.${locale}.label` })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.service.labelField.placeholder' })} />
          </div>
        ))}

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.service.expandToggle.hide' : 'fs.dirent.service.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.validityStartField.label' })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.service.validityStartField.placeholder' })} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.validityEndField.label' })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.service.validityEndField.placeholder' })} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.articlesField.label' })}</Typography>
            <FsDirentSelectMulti options={articles} value={selectedArticles} onChange={setSelectedArticles} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.configOptionsField.label' })}</Typography>
            <FsDirentSelectMulti options={configOptions} value={selectedConfigOptions} onChange={setSelectedConfigOptions} />
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentServiceRoot>
  );
};
