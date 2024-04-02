import React from 'react';
import { TextField, IconButton } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useIntl } from 'react-intl';
import Context from 'context';
import { CreatePermission, ImmutablePermissionStore } from 'descriptor-access-mgmt';

//TODO
const Name: React.FC<{}> = () => {
  const intl = useIntl();
  const backend = Context.useBackend();
  const [name, setName] = React.useState('permission name');

  async function handlePermissionCreate() {
    const command: CreatePermission = {
      commandType: 'CREATE_PERMISSION',
      comment: 'creating permission',
      name,
      description: "New description",
      roles: []
    };
    await new ImmutablePermissionStore(backend.store).createPermission(command);
  };

  function handleNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setName(event.target.value);
  }


  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.permission.name.create.placeholder' })}
    fullWidth
    value={name}
    onChange={handleNameChange}
    onBlur={handlePermissionCreate}
  />);
}

const Description: React.FC<{}> = () => {
  const [description, setDescription] = React.useState('');
  const intl = useIntl();

  function handleDescriptionChange(event: React.ChangeEvent<HTMLInputElement>) {
    setDescription(event.target.value);
  }

  async function handleChange() {

  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.permission.description.create.placeholder' })}
    fullWidth
    multiline
    minRows={3}
    maxRows={6}
    value={description}
    onChange={handleDescriptionChange}
    onBlur={handleChange}
  />);
}


const CreateComment: React.FC<{}> = () => {
  const intl = useIntl();
  const backend = Context.useBackend();
  const [comment, setComment] = React.useState('d');



  function handleCommentChange(event: React.ChangeEvent<HTMLInputElement>) {
    setComment(event.target.value);
  }


  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.permission.createComment.placeholder' })}
    fullWidth
    required
    value={comment}
    onChange={handleCommentChange}
  />);
}

const CloseDialogButton: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  return (
    <IconButton onClick={onClose}>
      <CloseIcon />
    </IconButton>
  )
}



export const Fields = { CloseDialogButton, Name, Description, CreateComment };
export { CloseDialogButton };
