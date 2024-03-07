import React from 'react';
import { TextField, IconButton, Alert, AlertTitle } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useIntl } from 'react-intl';
import { PermissionId, Principal, PrincipalId, RoleId } from 'descriptor-permissions';
import { permissions_mock_data, usePermissions } from '../PermissionsContext';
import Burger from 'components-burger';


const NONE_SELECTED_ID = 'none';

const RolePermissions: React.FC = () => {
  const { permissions } = usePermissions();
  const [permissionIds, setPermissionIds] = React.useState<PermissionId[]>([]);
  const [directPermissions, setDirectPermissions] = React.useState<string[]>([]);
  const permissionsMenuItems = permissions && permissions.map((permission) => ({ id: permission.id, value: permission.name }));

  function handlePermissionChange(selectedIds: string[]) {
    setPermissionIds(selectedIds);
    if (selectedIds.includes(NONE_SELECTED_ID)) {
      setPermissionIds([NONE_SELECTED_ID]);
    }
    setDirectPermissions(selectedIds.map(permissionId => permissions?.find(p => p.id === permissionId)?.name || ''));
  };

  return (<>
    <Alert severity='info'>
      <AlertTitle>
        Permissions are directly assigned as well as inherited from the selected role parent and all of its parents
      </AlertTitle>
    </Alert>

    <Burger.SelectMultiple
      label='permissions.permission.select'
      onChange={handlePermissionChange}
      selected={permissionIds}
      items={permissionsMenuItems ?? []}
      empty={{ id: NONE_SELECTED_ID, label: 'permissions.select.none' }}
    />
  </>
  )
}

const RolePrincipals: React.FC = () => {
  const [principalIds, setPrincipalIds] = React.useState<PrincipalId[]>([]);
  const [selectedPrincipals, setSelectedPrincipals] = React.useState<string[]>([]);
  const principals: Principal[] = permissions_mock_data.testRoles.flatMap((r) => r.principals);
  const principalsMenuItems = principals.map((principal) => ({ id: principal.id, value: principal.name }));

  function handlePrincipalsChange(selectedIds: string[]) {
    setPrincipalIds(selectedIds);
    if (selectedIds.includes(NONE_SELECTED_ID)) {
      setPrincipalIds([NONE_SELECTED_ID]);
    }
    setSelectedPrincipals(selectedIds.map(principalId => principals.find(p => p.id === principalId)?.name || ''));
  };


  return (<>
    <Alert severity='info'>
      <AlertTitle>
        Selected members will blah blah
      </AlertTitle>
    </Alert>

    <Burger.SelectMultiple
      label='permissions.principals.select'
      onChange={handlePrincipalsChange}
      selected={principalIds}
      items={principalsMenuItems}
      empty={{ id: NONE_SELECTED_ID, label: 'permissions.select.none' }}
    />
  </>
  )
}

const RoleParent: React.FC = () => {
  const { roles } = usePermissions();
  const [parentId, setParentId] = React.useState<RoleId>('');

  let roleMenuItems;

  if (!roles?.length) {
    roleMenuItems = [{ id: '', value: '' }];
  } else {
    roleMenuItems = roles.map((role) => ({ id: role.id, value: role.name }))
  }

  function handleParentChange(selectedParent: string) {
    setParentId(selectedParent);
  };


  return (<>
    <Alert severity='info'>
      <AlertTitle>
        Selecting a parent role will inherit all parent permissions until the top-level parent
      </AlertTitle>
    </Alert>

    <Burger.Select
      label='permissions.role.parentSelect'
      onChange={handleParentChange}
      selected={parentId}
      items={roleMenuItems}
      empty={{ id: NONE_SELECTED_ID, label: 'permissions.select.none' }}
    />
  </>
  )
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

const Fields = { CloseDialogButton, RoleName, RoleDescription, RoleParent, RolePermissions, RolePrincipals };
export default Fields;