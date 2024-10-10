import React from 'react';
import { FormattedMessage } from 'react-intl';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import EditIcon from '@mui/icons-material/ModeEdit';
import ScienceOutlinedIcon from '@mui/icons-material/ScienceOutlined';
import { Typography, Box } from "@mui/material";

import { useSnackbar } from 'notistack';
import Burger from '@/burger';

import { Composer, Client } from '../../context';
import { ErrorView } from '../../styles';


const ServiceDelete: React.FC<{ serviceId: Client.ServiceId, onClose: () => void }> = ({ serviceId, onClose }) => {
  const { services } = Composer.useSite();
  const { service: composerService, actions } = Composer.useComposer();
  const tabs = Burger.useTabs();
  const { enqueueSnackbar } = useSnackbar();
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<Client.StoreError>();

  const service = services[serviceId];
  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="services.delete.error.title" />
      </Typography>
      <ErrorView error={errors} />
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <FormattedMessage id="services.delete.content" values={{ name: service.ast?.name }} />
    </Typography>)
  }

  return (<Burger.Dialog open={true}
    onClose={onClose}
    children={editor}
    backgroundColor="uiElements.main"
    title='services.delete.title'
    submit={{
      title: "buttons.delete",
      disabled: apply,
      onClick: () => {
        setErrors(undefined);
        setApply(true);
        var serviceTab = tabs.session.tabs.find(tab => tab.id === serviceId);
        composerService.delete().service(serviceId)
          .then(data => {
            if (serviceTab) {
              tabs.actions.handleTabClose(serviceTab);
            }
            enqueueSnackbar(<FormattedMessage id="services.deleted.message" values={{ name: service.ast?.name }} />);
            actions.handleLoadSite(data);
            onClose();
          })
          .catch((error: Client.StoreError) => {
            setErrors(error);
          });
      }
    }}
  />);
}

const ServiceOptions: React.FC<{ service: Client.Entity<Client.AstService> }> = ({ service }) => {

  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'ServiceDelete' | 'ServiceCopy'>(undefined);
  const nav = Composer.useNav();
  const {handleDebugInit} = Composer.useDebug();
  const handleDialogClose = () => setDialogOpen(undefined);
  const { service: clientService, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState(service.ast?.name + "_Copy");
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<Client.StoreError>();

  const handleCopy = () => {
    setErrors(undefined);
    setApply(true);

    clientService.copy(service.id, name)
      .then(data => {
        enqueueSnackbar(<FormattedMessage id="services.composer.copiedMessage" values={{ name: service.ast?.name, newName: name }} />);
        actions.handleLoadSite(data).then(() => {
          const [article] = Object.values(data.services).filter(d => d.ast?.name === name);
          nav.handleInTab({ article })
        });
        handleDialogClose();
      }).catch((error: Client.StoreError) => {
        setErrors(error);
      });
  }


  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="services.composer.errorsTitle" />
      </Typography>
      <ErrorView error={errors} />
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <Burger.TextField
        label='services.composer.assetName'
        value={name}
        onChange={setName}
        onEnter={() => handleCopy()} />
    </Typography>)
  }

  return (
    <>
      {dialogOpen === 'ServiceDelete' ? <ServiceDelete serviceId={service.id} onClose={handleDialogClose} /> : null}
      <Burger.TreeItemOption nodeId={service.id + 'edit-nested'}
        color='link'
        icon={EditIcon}
        onClick={() => nav.handleInTab({ article: service })}
        labelText={<FormattedMessage id="services.edit.title" />}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={service.id + 'simulate-nested'}
        color='link'
        icon={ScienceOutlinedIcon}
        onClick={() => handleDebugInit(service.id)}
        labelText={<FormattedMessage id="services.simulate.title" />}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={service.id + 'delete-nested'}
        color='link'
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('ServiceDelete')}
        labelText={<FormattedMessage id="services.delete.title" />}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={service.id + 'copyas-nested'}
        color='link'
        icon={EditIcon}
        onClick={() => setDialogOpen('ServiceCopy')}
        labelText={<FormattedMessage id="services.copyas.title" />}>
      </Burger.TreeItemOption>
      {dialogOpen === 'ServiceCopy' ? 
      <Burger.Dialog open={true}
        onClose={handleDialogClose}
        children={editor}
        backgroundColor="uiElements.main"
        title='services.composer.copyTitle'
        submit={{
          title: "buttons.copy",
          disabled: apply,
          onClick: () => handleCopy()
        }}
      /> : null}
    </>
  );
}

export default ServiceOptions;
