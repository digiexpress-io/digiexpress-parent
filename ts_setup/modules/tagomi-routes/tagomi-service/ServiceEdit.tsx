import React from 'react';
import { Box, Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { useSnackbar } from 'notistack';

import { useIntl } from 'react-intl';

import * as Burger from '@dxs-ts/eveli-primitives';
import { CancelButton } from '@dxs-ts/eveli-primitives';
import { TagomiApi, TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';
import { useFetch } from '@dxs-ts/envir-fetch';
import { LocaleLabels } from '../tagomi-locale';


const ServiceEdit: React.FC<{ serviceId: TagomiApi.ServiceId, onClose: () => void }> = ({ serviceId, onClose }) => {
  const { enqueueSnackbar } = useSnackbar();
  const intl = useIntl();

  const { actions, site, backend } = Composer.useComposer();
  const service = site.services[serviceId];
  const [serviceName, setServiceName] = React.useState(service.serviceName);
  const [orchestratorName, setOrchestratorName] = React.useState(service.orchestratorName);
  const [labels, setLabels] = React.useState<TagomiApi.LocaleAndLabel[]>(service.labels);
  const [changeInProgress, setChangeInProgress] = React.useState(false);
  const { flows: allFlows = [] } = useFetch('worker/rest/api/assets/wrench/flow-names.GET', {});

  const message = intl.formatMessage({ id: 'snack.services.editedMessage' })

  const handleUpdate = () => {
    const entity: TagomiApi.ServiceMutator = { serviceId, serviceName, orchestratorName, labels };
    backend.updateService(entity).then(_success => {
      enqueueSnackbar(message, { variant: 'success' });
      onClose();
      actions.handleLoadSite();
    });
  }

  const updateDisabled = !serviceName || changeInProgress;

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'tagomi.service.edit.dialog.title' })}{" "}{service.serviceName}</DialogTitle>
      <DialogContent>

        <Burger.TextField label="tagomi.service.edit.dialog.name" required value={serviceName} onChange={setServiceName} />
        <Burger.Select label="tagomi.service.edit.dialog.orchestratorName" onChange={setOrchestratorName}
          selected={orchestratorName}
          items={allFlows.map((flow) => { return { id: flow, value: flow } })}
        />

        <Box mb={2} />

        <LocaleLabels
          onChange={(labels) => {
            setChangeInProgress(false);
            setLabels(labels.map(l => ({ locale: l.locale, labelValue: l.value })));
          }}
          onChangeStart={() => setChangeInProgress(true)}
          selected={labels.map(label => ({ locale: label.locale, value: label.labelValue }))}
        />

      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleUpdate} disabled={updateDisabled}>
          {intl.formatMessage({ id: 'button.update' })}
        </Button>
      </DialogActions>
    </Dialog>);
}

export { ServiceEdit }


