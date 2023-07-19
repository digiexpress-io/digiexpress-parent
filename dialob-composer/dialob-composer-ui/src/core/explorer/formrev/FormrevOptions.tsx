import React from 'react';
import { FormattedMessage } from 'react-intl';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import EditIcon from '@mui/icons-material/ModeEdit';
import ScienceOutlinedIcon from '@mui/icons-material/ScienceOutlined';
import { Typography, Box } from "@mui/material";

import { useSnackbar } from 'notistack';
import Burger from '@the-wrench-io/react-burger';

import { Composer, Client } from '../../context';
import {ErrorView} from '../../styles';


const FormrevDelete: React.FC<{ formrevId: Client.FormrevId, onClose: () => void }> = ({ formrevId, onClose }) => {
  const { revs } = Composer.useSite();
  const { service, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<Client.StoreError>();

  const formrevs = revs[formrevId];
  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="formrevs.delete.error.title" />
      </Typography>
      <ErrorView error={errors}/>
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <FormattedMessage id="formrevs.delete.content" values={{ name: formrevs.name }} />
    </Typography>)
  }


  return (<Burger.Dialog open={true}
    onClose={onClose}
    children={editor}
    backgroundColor="uiElements.main"
    title='formrevs.delete.title'
    submit={{
      title: "buttons.delete",
      disabled: apply,
      onClick: () => {
        setErrors(undefined);
        setApply(true);

        service.delete().formrev(formrevId)
          .then(data => {
            enqueueSnackbar(<FormattedMessage id="formrevs.deleted.message" values={{ name: formrevs.name }} />);
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


const FormrevOptions: React.FC<{ formrev: Client.FormRev }> = ({ formrev }) => {

  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'FormrevDelete' | 'FlowCopy'>(undefined);
  const nav = Composer.useNav();
  const handleDialogClose = () => setDialogOpen(undefined);
  const { service, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState(formrev.name + "_copy");
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<Client.StoreError>();

  const handleCopy = () => {
    setErrors(undefined);
    setApply(true);

    service.copy(formrev.id, name)
      .then(data => {
        enqueueSnackbar(<FormattedMessage id="formrevs.composer.copiedMessage" values={{ name: formrev.name, newName: name }} />);
        actions.handleLoadSite(data).then(() => {
          const [article] = Object.values(data.revs).filter(d => d.name === name);
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
        <FormattedMessage id="formrevs.composer.errorsTitle" />
      </Typography>
      <ErrorView error={errors} />
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <Burger.TextField
        label='formrevs.composer.assetName'
        value={name}
        onChange={setName}
        onEnter={() => handleCopy()} />
    </Typography>)
  }
  

  return (
    <>
      {dialogOpen === 'FormrevDelete' ? <FormrevDelete formrevId={formrev.id} onClose={handleDialogClose} /> : null}

      <Burger.TreeItemOption nodeId={formrev.id + 'edit-nested'}
        color='article'
        icon={EditIcon}
        onClick={() => nav.handleInTab({ article: formrev })}
        labelText={<FormattedMessage id="formrevs.edit.title" />}/>

      <Burger.TreeItemOption nodeId={formrev.id + 'simulate-nested'}
        color='article'
        icon={ScienceOutlinedIcon }
        onClick={() => {
          nav.handleInTab({ article: formrev, id: `debug-fill/${formrev.head}` })
          
        }}
        labelText={<FormattedMessage id="formrevs.simulate.title" />}/>

      <Burger.TreeItemOption nodeId={formrev.id + 'delete-nested'}
        color='article'
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('FormrevDelete')}
        labelText={<FormattedMessage id="formrevs.delete.title" />}/>

      <Burger.TreeItemOption nodeId={formrev.id + 'copyas-nested'}
        color='article'
        icon={EditIcon}
        onClick={() => setDialogOpen('FlowCopy')}
        labelText={<FormattedMessage id="formrevs.copyas.title" />}/>

      {dialogOpen === 'FlowCopy' ? 
      <Burger.Dialog open={true}
        onClose={handleDialogClose}
        children={editor}
        backgroundColor="uiElements.main"
        title='formrevs.composer.copyTitle'
        submit={{
          title: "buttons.copy",
          disabled: apply,
          onClick: () => handleCopy()
        }}
      /> : null}
    </>
  );
}

export default FormrevOptions;
