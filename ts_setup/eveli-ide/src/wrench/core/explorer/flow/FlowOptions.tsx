import React from 'react';
import { FormattedMessage } from 'react-intl';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import EditIcon from '@mui/icons-material/ModeEdit';
import ScienceOutlinedIcon from '@mui/icons-material/ScienceOutlined';
import { Typography, Box } from "@mui/material";

import { useSnackbar } from 'notistack';
import * as Burger from '@/burger';

import { Composer, Client } from '../../context';
import {ErrorView} from '../../styles';


const FlowDelete: React.FC<{ flowId: Client.FlowId, onClose: () => void }> = ({ flowId, onClose }) => {
  const { flows } = Composer.useSite();
  const { service, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<Client.StoreError>();
  const tabs = Burger.useTabs();

  const flow = flows[flowId];
  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="flows.delete.error.title" />
      </Typography>
      <ErrorView error={errors}/>
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <FormattedMessage id="flows.delete.content" values={{ name: flow.ast?.name }} />
    </Typography>)
  }


  return (<Burger.Dialog open={true}
    onClose={onClose}
    children={editor}
    backgroundColor="uiElements.main"
    title='flows.delete.title'
    submit={{
      title: "buttons.delete",
      disabled: apply,
      onClick: () => {
        setErrors(undefined);
        setApply(true);
        var flowTab = tabs.session.tabs.find(tab => tab.id === flowId);
        service.delete().flow(flowId)
          .then(data => {
            if (flowTab) {
              tabs.actions.handleTabClose(flowTab);
            }
            enqueueSnackbar(<FormattedMessage id="flows.deleted.message" values={{ name: flow.ast?.name }} />);
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


const FlowOptions: React.FC<{ flow: Client.Entity<Client.AstFlow> }> = ({ flow }) => {

  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'FlowDelete' | 'FlowCopy'>(undefined);
  const nav = Composer.useNav();
  const {handleDebugInit} = Composer.useDebug();
  const handleDialogClose = () => setDialogOpen(undefined);
  const { service, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState(flow.ast?.name + "_copy");
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<Client.StoreError>();

  const handleCopy = () => {
    setErrors(undefined);
    setApply(true);

    service.copy(flow.id, name)
      .then(data => {
        enqueueSnackbar(<FormattedMessage id="flows.composer.copiedMessage" values={{ name: flow.ast?.name, newName: name }} />);
        actions.handleLoadSite(data).then(() => {
          const [article] = Object.values(data.flows).filter(d => d.ast?.name === name);
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
        <FormattedMessage id="flows.composer.errorsTitle" />
      </Typography>
      <ErrorView error={errors} />
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <Burger.TextField
        label='flows.composer.assetName'
        value={name}
        onChange={setName}
        onEnter={() => handleCopy()} />
    </Typography>)
  }
  

  return (
    <>
      {dialogOpen === 'FlowDelete' ? <FlowDelete flowId={flow.id} onClose={handleDialogClose} /> : null}

      <Burger.TreeItemOption nodeId={flow.id + 'edit-nested'}
        color='article'
        icon={EditIcon}
        onClick={() => nav.handleInTab({ article: flow })}
        labelText={<FormattedMessage id="flows.edit.title" />}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={flow.id + 'simulate-nested'}
        color='article'
        icon={ScienceOutlinedIcon}
        onClick={() => handleDebugInit(flow.id)}
        labelText={<FormattedMessage id="flows.simulate.title" />}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={flow.id + 'delete-nested'}
        color='article'
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('FlowDelete')}
        labelText={<FormattedMessage id="flows.delete.title" />}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={flow.id + 'copyas-nested'}
        color='article'
        icon={EditIcon}
        onClick={() => setDialogOpen('FlowCopy')}
        labelText={<FormattedMessage id="flows.copyas.title" />}>
      </Burger.TreeItemOption>

      {dialogOpen === 'FlowCopy' ? 
      <Burger.Dialog open={true}
        onClose={handleDialogClose}
        children={editor}
        backgroundColor="uiElements.main"
        title='flows.composer.copyTitle'
        submit={{
          title: "buttons.copy",
          disabled: apply,
          onClick: () => handleCopy()
        }}
      /> : null}
    </>
  );
}

export default FlowOptions;
