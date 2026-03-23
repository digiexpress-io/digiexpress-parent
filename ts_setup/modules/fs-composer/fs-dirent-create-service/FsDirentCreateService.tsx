import React from 'react';
import { Typography, Divider, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import { collectArticles, mockFsData } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentSingleSelect } from '../fs-dirent-single-select';
import { FsDirentMultiSelect } from '../fs-dirent-multi-select';
import { FsDirentCreateServiceProps } from './FsDirentCreateServiceProps';
import { useUtilityClasses, FsDirentCreateServiceRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

const DIALOB_FORM_OPTIONS = [
  { value: 'dialob-application-form', label: 'Application Form' },
  { value: 'dialob-permit-request', label: 'Permit Request Form' },
  { value: 'dialob-service-inquiry', label: 'Service Inquiry Form' },
];

const DIALOB_TAG_OPTIONS = [
  { value: 'v1.0.0', label: 'v1.0.0' },
  { value: 'v1.1.0', label: 'v1.1.0' },
  { value: 'v2.0.0', label: 'v2.0.0' },
];

const FLOW_OPTIONS = [
  { value: 'flow-main-service', label: 'Main Service Flow' },
  { value: 'flow-approval', label: 'Approval Flow' },
  { value: 'flow-notification', label: 'Notification Flow' },
];

const CONFIG_OPTIONS = [
  { value: 'devMode', label: 'Development mode' },
  { value: 'assignableMode', label: 'Assignable mode' },
  { value: 'disabledMode', label: 'Disabled mode' },
  { value: 'anonymousMode', label: 'Anonymous mode' },
];

const articles = collectArticles(mockFsData);

export const FsDirentCreateService: React.FC<FsDirentCreateServiceProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  const [selectedDialobForm, setSelectedDialobForm] = React.useState<string>('');
  const [selectedDialobTag, setSelectedDialobTag] = React.useState<string>('');
  const [selectedFlow, setSelectedFlow] = React.useState<string>('');
  const [selectedArticles, setSelectedArticles] = React.useState<string[]>([]);
  const [selectedConfigOptions, setSelectedConfigOptions] = React.useState<string[]>([]);

  return (
    <FsDirentCreateServiceRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntCreate.service.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.service.nameField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.service.nameField.placeholder' })} />

        <Divider />

        <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.direntCreate.service.sectionTitle.dialobForm' })}</Typography>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.service.dialobFormField.label' })}</Typography>
        <FsDirentSingleSelect options={DIALOB_FORM_OPTIONS} value={selectedDialobForm} onChange={setSelectedDialobForm} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.service.dialobTagField.label' })}</Typography>
        <FsDirentSingleSelect options={DIALOB_TAG_OPTIONS} value={selectedDialobTag} onChange={setSelectedDialobTag} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.service.flowField.label' })}</Typography>
        <FsDirentSingleSelect options={FLOW_OPTIONS} value={selectedFlow} onChange={setSelectedFlow} />

        <Divider />

        <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.direntCreate.service.sectionTitle.createLocaleLabels' })}</Typography>

        {ownerState.locales.map((locale) => (
          <div key={locale} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: `fs.direntCreate.service.labelField.${locale}.label` })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.service.labelField.placeholder' })} />
          </div>
        ))}

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.direntCreate.service.expandToggle.hide' : 'fs.direntCreate.service.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.service.validityStartField.label' })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.service.validityStartField.placeholder' })} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.service.validityEndField.label' })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.service.validityEndField.placeholder' })} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.service.articlesField.label' })}</Typography>
            <FsDirentMultiSelect options={articles} value={selectedArticles} onChange={setSelectedArticles} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.service.configOptionsField.label' })}</Typography>
            <FsDirentMultiSelect options={CONFIG_OPTIONS} value={selectedConfigOptions} onChange={setSelectedConfigOptions} />
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentCreateServiceRoot>
  );
};
