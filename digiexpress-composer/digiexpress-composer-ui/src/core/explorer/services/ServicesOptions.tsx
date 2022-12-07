import React from 'react';
import { FormattedMessage } from 'react-intl';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import EditIcon from '@mui/icons-material/ModeEdit';
import ScienceOutlinedIcon from '@mui/icons-material/ScienceOutlined';
import { Typography, Box } from "@mui/material";

import { useSnackbar } from 'notistack';
import Burger from '@the-wrench-io/react-burger';

import { Composer, Client } from '../../context';
import { ErrorView } from '../../styles';


const ServiceDelete: React.FC<{ value: Client.ProcessValue, onClose: () => void }> = ({ value, onClose }) => {
  const {  } = Composer.useSite();
  const { service: composerService, actions } = Composer.useComposer();
  
  const { enqueueSnackbar } = useSnackbar();
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<Client.StoreError>();


  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="processValue.delete.error.title" />
      </Typography>
      <ErrorView error={errors} />
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <FormattedMessage id="processValue.delete.content" values={{ ...value }} />
    </Typography>)
  }

  return (<Burger.Dialog open={true}
    onClose={onClose}
    children={editor}
    backgroundColor="uiElements.main"
    title='processValue.delete.title'
    submit={{
      title: "buttons.delete",
      disabled: apply,
      onClick: () => {
        setErrors(undefined);
        setApply(true);

/*
        composerService.delete().service(serviceId)
          .then(data => {
            enqueueSnackbar(<FormattedMessage id="processValue.deleted.message" values={{ name: service.ast?.name }} />);
            actions.handleLoadSite(data);
            onClose();
          })
          .catch((error: Client.StoreError) => {
            setErrors(error);
          });
          */
      }
    }}
  />);
}

const ProcessValueOptions: React.FC<{ value: Client.ProcessValue }> = ({ value }) => {

  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'ServiceDelete' | 'ServiceCopy'>(undefined);
  const nav = Composer.useNav();
  
  const handleDialogClose = () => setDialogOpen(undefined);
  const { service: clientService, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState(value.name + "_Copy");
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<Client.StoreError>();

  const handleCopy = () => {
    setErrors(undefined);
    setApply(true);

    clientService.copy(value.id, name)
      .then(data => {
        enqueueSnackbar(<FormattedMessage id="processValue.composer.copiedMessage" values={{ name: value.name, newName: name }} />);
/*
        actions.handleLoadSite(data).then(() => {
          const [article] = Object.values(data.services).filter(d => d.ast?.name === name);
          nav.handleInTab({ article })
        });
        */
        handleDialogClose();
      }).catch((error: Client.StoreError) => {
        setErrors(error);
      });
  }


  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="processValue.composer.errorsTitle" />
      </Typography>
      <ErrorView error={errors} />
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <Burger.TextField
        label='processValue.composer.assetName'
        value={name}
        onChange={setName}
        onEnter={() => handleCopy()} />
    </Typography>)
  }

  return (
    <>
      {dialogOpen === 'ServiceDelete' ? <ServiceDelete value={value} onClose={handleDialogClose} /> : null}
      <Burger.TreeItemOption nodeId={value.id + 'edit-nested'}
        color='link'
        icon={EditIcon}
        onClick={() => {
          
          //nav.handleInTab({ article: service })
          
        }}
        labelText={<FormattedMessage id="processValue.edit.title" />}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={value.id + 'simulate-nested'}
        color='link'
        icon={ScienceOutlinedIcon}
        onClick={() => { 
          //handleDebugInit(service.id) 
        }}
        labelText={<FormattedMessage id="processValue.simulate.title" />}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={value.id + 'delete-nested'}
        color='link'
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('ServiceDelete')}
        labelText={<FormattedMessage id="processValue.delete.title" />}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={value.id + 'copyas-nested'}
        color='link'
        icon={EditIcon}
        onClick={() => setDialogOpen('ServiceCopy')}
        labelText={<FormattedMessage id="processValue.copyas.title" />}>
      </Burger.TreeItemOption>
      {dialogOpen === 'ServiceCopy' ? 
      <Burger.Dialog open={true}
        onClose={handleDialogClose}
        children={editor}
        backgroundColor="uiElements.main"
        title='processValue.composer.copyTitle'
        submit={{
          title: "buttons.copy",
          disabled: apply,
          onClick: () => handleCopy()
        }}
      /> : null}
    </>
  );
}

export { ProcessValueOptions };
