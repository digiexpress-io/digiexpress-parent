import React from 'react';
import { Typography, Divider, Collapse } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentSelectSingle } from '../fs-dirent-select-single';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentWorkflowCreateProps } from './FsDirentWorkflowProps';
import { useUtilityClasses, FsDirentWorkflowRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';


export const FsDirentWorkflowCreate: React.FC<FsDirentWorkflowCreateProps> = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();
  const { selectOptions, getConfigOptionsForType } = useFsDirent();
  const dialobForms = selectOptions.dialobs;
  const flows = selectOptions.flows;
  const articles = selectOptions.articles;
  const configOptions = getConfigOptionsForType('ARTICLE_WORKFLOW');

  const [workflowName, setWorkflowName] = React.useState('');
  const [selectedDialobForm, setSelectedDialobForm] = React.useState<string>('');
  const [selectedDialobTag, setSelectedDialobTag] = React.useState<string>('');
  const dialobTags = selectOptions.collectDialobTags(selectedDialobForm);

  const [selectedFlow, setSelectedFlow] = React.useState<string>('');
  const [selectedArticles, setSelectedArticles] = React.useState<string[]>([]);
  const [selectedConfigOptions, setSelectedConfigOptions] = React.useState<string[]>([]);

  return (
    <FsDirentWorkflowRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.service.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.service.nameField.placeholder' })} required value={workflowName} onChange={setWorkflowName} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.dialobFormField.label' })}</Typography>
        <FsDirentSelectSingle options={dialobForms} value={selectedDialobForm} onChange={setSelectedDialobForm} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.dialobTagField.label' })}</Typography>
        <FsDirentSelectSingle options={dialobTags} value={selectedDialobTag} onChange={setSelectedDialobTag} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.service.flowField.label' })}</Typography>
        <FsDirentSelectSingle options={flows} value={selectedFlow} onChange={setSelectedFlow} />

        <Divider />

        <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.dirent.service.sectionTitle.createLocaleLabels' })}</Typography>

        {ownerState.locales.map((locale) => (
          <div key={locale.value} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: `fs.dirent.service.labelField.${locale.label}.label` })}</Typography>
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

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}</Typography>
            <FsDirentSelectMulti options={configOptions} value={selectedConfigOptions} onChange={setSelectedConfigOptions} />
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentWorkflowRoot>
  );
};
