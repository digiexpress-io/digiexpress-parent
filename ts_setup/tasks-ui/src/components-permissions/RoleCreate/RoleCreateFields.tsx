import React from 'react';
import { TextField, Box, IconButton, Typography, Stack } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useIntl } from 'react-intl';
import { Permission, PermissionId, Role, RoleId } from 'descriptor-permissions';
import { permissions_mock_data } from '../PermissionsContext';
import Burger from 'components-burger';





/*
interface MenuItemType {
  id: string;
  value: string
}

interface SelectMenuProps {
  onChange(id: string): void;
  selectedItemId: string;
  menuItems: MenuItemType[];
}

const RolePermissions: React.FC<SelectMenuProps> = ({ onChange, selectedItemId, menuItems }) => {

  return (<Burger.Select
    label='permissions.permission.select'
    onChange={onChange}
    selected={selectedItemId}
    items={menuItems}
    empty={{ id: NONE_SELECTED_ID, label: 'permissions.permission.select.none' }}
  />
  )
}

const RoleParent: React.FC<SelectMenuProps> = ({ onChange, selectedItemId, menuItems }) => {

  return (<Burger.Select
    label='permissions.role.parentSelect'
    onChange={onChange}
    selected={selectedItemId}
    items={menuItems}
    empty={{ id: NONE_SELECTED_ID, label: 'permissions.role.parentSelect.none' }}
  />
  )
}
*/
const NONE_SELECTED_ID = 'none';

function getParentRolePermissions(parentId: string, roles: Role[]) {
  if (parentId === NONE_SELECTED_ID) {
    return undefined;
  }

  const selectedParent = roles.find(role => role.id === parentId);
  if (selectedParent) {
    return (
      <Box sx={{ mx: 1, pt: 1 }}>
        {selectedParent.permissions.map((permission) => (<Box>
          <Typography key={permission.id}>{permission.name}</Typography>
        </Box>
        ))}
      </Box>
    );
  }
}

const ParentRoleAndPermissions: React.FC<{}> = () => {
  const [parentId, setParentId] = React.useState<RoleId>('');
  const [permissionIds, setPermissionIds] = React.useState<PermissionId[]>([]);
  const [directPermissions, setDirectPermissions] = React.useState<string[]>([]);

  const roles: Role[] = permissions_mock_data.testRoles;
  const permissions: Permission[] = permissions_mock_data.testRoles.flatMap((r) => r.permissions);
  const roleParentMenuItems = roles.map((role) => ({ id: role.id, value: role.name }));
  const permissionsMenuItems = permissions.map((permission) => ({ id: permission.id, value: permission.name }));

  const inheritedPermissions = getParentRolePermissions(parentId, roles);

  function handlePermissionChange(selectedPermissions: string[]) {
    setPermissionIds(selectedPermissions);
    if (selectedPermissions.includes(NONE_SELECTED_ID)) {
      setPermissionIds([NONE_SELECTED_ID]);
    }
    setDirectPermissions(selectedPermissions.map(permissionId => permissions.find(p => p.id === permissionId)?.name || ''));
  };

  function handleParentChange(selectedParent: string) {
    setParentId(selectedParent);
  };


  return (
    <>
      <Burger.Select
        label='permissions.role.parentSelect'
        onChange={handleParentChange}
        selected={parentId}
        items={roleParentMenuItems}
        empty={{ id: NONE_SELECTED_ID, label: 'permissions.role.parentSelect.none' }}
      />
      <Burger.SelectMultiple
        multiline
        label='permissions.permission.select'
        onChange={handlePermissionChange}
        selected={permissionIds}
        items={permissionsMenuItems}
        empty={{ id: NONE_SELECTED_ID, label: 'permissions.permission.select.none' }}
      />
      <Stack sx={{ mt: 1 }} spacing={1}>
        <Typography fontWeight='bold'>Direct Permissions: {directPermissions.join(', ')}</Typography>
        <Typography fontWeight='bold'>Inherited Permissions: {inheritedPermissions}</Typography>
      </Stack>
    </>)
}

const RoleName: React.FC<{}> = () => {
  const [name, setName] = React.useState('');
  const intl = useIntl();

  function handleRoleNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setName(event.target.value);
  }
  //TODO
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

export { CloseDialogButton };

const Fields = { CloseDialogButton, RoleName, RoleDescription, ParentRoleAndPermissions };
export default Fields;