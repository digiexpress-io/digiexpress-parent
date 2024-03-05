import React from 'react';
import { TextField, Box, IconButton, Typography } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useIntl } from 'react-intl';
import { Role } from 'descriptor-permissions';
import { permissions_mock_data } from '../PermissionsContext';
import Burger from 'components-burger';

const RoleName: React.FC<{}> = () => {
  const [name, setName] = React.useState('');
  const intl = useIntl();

  function handleRoleNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setName(event.target.value);
  }

  async function handleChange() {

  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.role.name.create.placeholder' })}
    fullWidth
    value={name}
    onChange={handleRoleNameChange}
    onBlur={handleChange}
  />);
}

const RoleDescription: React.FC<{}> = () => {
  const [description, setDescription] = React.useState('');
  const intl = useIntl();

  function handleDescriptionChange(event: React.ChangeEvent<HTMLInputElement>) {
    setDescription(event.target.value);
  }

  async function handleChange() {

  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.role.description.create.placeholder' })}
    fullWidth
    value={description}
    onChange={handleDescriptionChange}
    onBlur={handleChange}
  />);
}

const RolePermissions: React.FC = () => {
  return (<>TODO</>)
}


const NONE_SELECTED_ID = 'none';
const RoleParent: React.FC<{}> = () => {
  const [parentId, setParentId] = React.useState<string>('');

  const roles: Role[] = permissions_mock_data.testRoles;
  const menuItems = roles.map((role) => ({ id: role.id, value: role.name }))

  function handleParentChange(selectedParent: string) {
    setParentId(selectedParent);
  };

  function getParentPermissions() {
    if (parentId === NONE_SELECTED_ID) {
      return undefined;
    }

    const selectedParent = roles.find(role => role.id === parentId);
    if (selectedParent) {
      return (
        <Box sx={{ mx: 1, pt: 1 }}>
          {selectedParent.permissions.map((permission) => (<div key={permission.id}>
            <Typography>{permission.name}</Typography>
          </div>
          ))}
        </Box>
      );
    }

    return undefined;
  };

  const permissions = getParentPermissions();

  return (<>

    <Burger.Select
      label='permissions.role.parentSelect'
      onChange={handleParentChange}
      selected={parentId}
      items={menuItems}
      empty={{ id: NONE_SELECTED_ID, label: 'permissions.role.parentSelect.none' }}
    />
    {<>{permissions}</>}
  </>
  )
}

const CloseDialogButton: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  return (
    <IconButton onClick={onClose}>
      <CloseIcon />
    </IconButton>
  )
}

export { CloseDialogButton };

const Fields = { CloseDialogButton, RoleName, RoleDescription, RolePermissions, RoleParent };
export default Fields;