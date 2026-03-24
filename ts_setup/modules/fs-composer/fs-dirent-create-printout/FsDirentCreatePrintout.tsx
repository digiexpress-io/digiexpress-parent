import React from 'react';
import { Typography, Divider } from '@mui/material';
import { useIntl } from 'react-intl';
import { getConfigOptionsForType } from '@dxs-ts/fs-api';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentSingleSelect } from '../fs-dirent-single-select';
import { FsDirentMultiSelect } from '../fs-dirent-multi-select';
import { FsDirentCreatePrintoutProps } from './FsDirentCreatePrintoutProps';
import { useUtilityClasses, FsDirentCreatePrintoutRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

const ORCHESTRATOR_OPTIONS = [
  { value: 'orchestrator-main', label: 'Main Orchestrator' },
  { value: 'workflow-orchestrator', label: 'Workflow Orchestrator' },
  { value: 'process-orchestrator-v2', label: 'Process Orchestrator v2' },
];

export const FsDirentCreatePrintout: React.FC<FsDirentCreatePrintoutProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();
  const configOptions = getConfigOptionsForType('printout');

  const [printoutServiceName, setPrintoutServiceName] = React.useState<string>('');
  const [selectedOrchestratorName, setSelectedOrchestratorName] = React.useState<string>('');
  const [selectedConfigOptions, setSelectedConfigOptions] = React.useState<string[]>([]);

  return (
    <FsDirentCreatePrintoutRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntCreate.printout.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.printout.printoutServiceNameField.label' })}</Typography>
        <FsDirentTextField
          required={true}
          value={printoutServiceName}
          onChange={setPrintoutServiceName}
          placeholder={intl.formatMessage({ id: 'fs.direntCreate.printout.printoutServiceNameField.placeholder' })}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.printout.orchestratorNameField.label' })}</Typography>
        <FsDirentSingleSelect options={ORCHESTRATOR_OPTIONS} value={selectedOrchestratorName} onChange={setSelectedOrchestratorName} />

        <Divider />

        <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.direntCreate.printout.sectionTitle.createLocaleLabels' })}</Typography>

        {ownerState.locales.map((locale) => (
          <div key={locale} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: `fs.direntCreate.printout.labelField.${locale}.label` })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.printout.labelField.placeholder' })} />
          </div>
        ))}

        <Divider />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.printout.configOptionsField.label' })}</Typography>
        <FsDirentMultiSelect options={configOptions} value={selectedConfigOptions} onChange={setSelectedConfigOptions} />
        {selectedConfigOptions.includes('devMode') && (
          <Typography className={classes.configOptionDescription}>{intl.formatMessage({ id: 'fs.direntCreate.configOption.devMode.description' })}</Typography>
        )}

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentCreatePrintoutRoot>
  );
};
