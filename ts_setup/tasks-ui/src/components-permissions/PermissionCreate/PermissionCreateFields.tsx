import React from 'react';
import { TextField, IconButton } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useIntl } from 'react-intl';


const PermissionName: React.FC<{}> = () => {
  const [name, setName] = React.useState('');
  const intl = useIntl();



  function handleNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setName(event.target.value);
  }

  async function handleChange() {

  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.permission.name.create.placeholder' })}
    fullWidth
    value={name}
    onChange={handleNameChange}
    onBlur={handleChange}
  />);
}

const PermissionDescription: React.FC<{}> = () => {
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

const CloseDialogButton: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  return (
    <IconButton onClick={onClose}>
      <CloseIcon />
    </IconButton>
  )
}



export const Fields = { CloseDialogButton, PermissionName, PermissionDescription };
export { CloseDialogButton };
