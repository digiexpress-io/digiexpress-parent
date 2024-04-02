import React from 'react';
import { TextField, IconButton, Alert, AlertTitle, Box, Chip, Button } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useIntl } from 'react-intl';

import { CreateRole, ImmutablePermissionStore } from 'descriptor-access-mgmt';
import { SectionLayout, FilterByString } from 'components-generic';
import { StyledRoleCreateTransferList } from './StyledRoleCreateTransferList';
import { useNewRole } from './RoleCreateContext';
import Context from 'context';



interface ChipData {
  key: number;
  label: string;
}

const CurrentlySelected: React.FC<{}> = () => {
  const [data, setData] = React.useState<readonly ChipData[]>([
    { key: 0, label: 'label here' }
  ]);

  const handleDelete = (chipToDelete: ChipData) => () => {
    setData((chips) => chips.filter((chip) => chip.key !== chipToDelete.key));
  };

  return (
    <Alert severity='info' icon={false}>
      <AlertTitle>Currently selected</AlertTitle>
      {data.map((c) => <Chip sx={{ mx: '2px' }} label={c.label} key={c.key} onDelete={handleDelete(c)} />)}
    </Alert>)
}


const RoleParent: React.FC = () => {
  const { roles } = Context.usePermissions();
  const { setParentId, setParentName } = useNewRole();

  function handleParentChange(selectedParentId: string, selectedParentName: string) {
    setParentId(selectedParentId);
    setParentName(selectedParentName);
  };

  return (<>
    <CurrentlySelected />
    <FilterByString onChange={() => { }} />
    {roles.map((role) => <Box key={role.id}><Button variant='text' onClick={() => handleParentChange(role.id, role.name)}>{role.name}</Button></Box>)}
  </>
  )
}

const RoleName: React.FC<{}> = () => {
  const backend = Context.useBackend();
  const [name, setName] = React.useState('');
  const intl = useIntl();

  async function handleRoleCreate() {
    const command: CreateRole = {
      commandType: 'CREATE_ROLE',
      description: 'New role created',
      comment: 'This comment is for my first role',
      permissions: [],
      name
    }
    await new ImmutablePermissionStore(backend.store).createRole(command);
    console.log("new role created", name)
  }
  function handleRoleNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setName(event.target.value);
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.role.name.create.placeholder' })}
    fullWidth
    value={name}
    onChange={handleRoleNameChange}
    onBlur={handleRoleCreate} //TODO remove the onBlur
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

const RoleParentOverview: React.FC<{}> = () => {
  const intl = useIntl();
  const { entity } = useNewRole();
  const parentRoleName = entity.parentName;

  if (!parentRoleName) {
    return (<SectionLayout label='permissions.role.roleParentName' value={intl.formatMessage({ id: 'permissions.role.roleParentName.none' })} />)
  }
  return (<SectionLayout label='permissions.role.roleParentName' value={parentRoleName} />);
}


const RolePermissionsOverview: React.FC<{}> = () => {
  return (<Box>
    <SectionLayout label='permissions.role.permissions.permissionName' value={'permission 1'} />
    <SectionLayout label='permissions.role.permissions.permissionName' value={'permission 2'} />
    <SectionLayout label='permissions.role.permissions.permissionName' value={'permission 3'} />
    <SectionLayout label='permissions.role.permissions.permissionName' value={'permission 4'} />
  </Box>);
}


const RoleMembersOverview: React.FC<{}> = () => {
  return (<Box>
    <SectionLayout label='permissions.role.users.username' value={'member 1'} />
    <SectionLayout label='permissions.role.users.username' value={'member 2'} />
    <SectionLayout label='permissions.role.users.username' value={'member 3'} />
    <SectionLayout label='permissions.role.users.username' value={'member 4'} />
  </Box>);
}

const CloseDialogButton: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  return (
    <IconButton onClick={onClose}>
      <CloseIcon />
    </IconButton>
  )
}

const RolePermissions: React.FC = () => {

  const { permissions } = Context.usePermissions();

  if (!permissions) {
    console.log('no permissions found')
  }

  function handleSave() {
    console.log('click for save!')
  }

  return (<>
    <CurrentlySelected />
    <FilterByString onChange={() => { }} />

    <StyledRoleCreateTransferList
      title="permissions.title"
      titleArgs={{ name: 'fun name' }}
      searchTitle="permissions.search.title"
      selectedTitle="permissions.selectedPermissions"
      headers={["permissions.permission.name", "permissions.permission.description"]}
      rows={permissions
        .sort((a, b) => a.name.localeCompare(b.name))
        .map((permission) => permission.id)}
      selected={permissions.map((r) => r.id)}
      filterRow={(row, search) => {
        const permission = permissions.find(permission => permission.id === row)?.name;
        return ((permission && permission.toLowerCase().indexOf(search) > -1) ? true : false);
      }}
      renderCells={(row) => {
        const { name, description } = permissions.find(permission => permission.id === row)!;
        return [name, description];
      }}
      cancel={{
        label: 'button.cancel',
        onClick: () => { }
      }}
      submit={{
        label: "button.apply",
        disabled: false,
        onClick: handleSave
      }}
    />
  </>
  )
}

const RolePrincipals: React.FC = () => {

  const { principals } = Context.usePermissions();

  if (!principals) {
    console.log('no principals found')
  }

  function handleSave() {
    console.log('click for save!')
  }


  return (<>
    <CurrentlySelected />
    <FilterByString onChange={() => { }} />

    <StyledRoleCreateTransferList
      title="permissions.principals.title"
      titleArgs={{ name: 'fun name' }}
      searchTitle="permissions.principals.search.title"
      selectedTitle="permissions.principals.selectedPrincipals"
      headers={["permissions.principal.name"]}
      rows={principals
        .sort((a, b) => a.name.localeCompare(b.name))
        .filter((principal, index, srcArray) => srcArray.findIndex(p => p.id === principal.id) === index)
        .map((principal) => principal.id)
      }
      selected={principals
        .filter((principal, index, srcArray) => srcArray
          .findIndex(p => p.id === principal.id) === index)
        .map((r) => r.id)}
      filterRow={(row, search) => {
        const principal = principals.find(principal => principal.id === row)?.name;
        return ((principal && principal
          .toLowerCase()
          .indexOf(search) > -1) ? true : false);
      }}
      renderCells={(row) => {
        const { name } = principals.find(principal => principal.id === row)!;
        return [name];
      }}
      cancel={{
        label: 'button.cancel',
        onClick: () => { }
      }}
      submit={{
        label: "button.apply",
        disabled: false,
        onClick: handleSave
      }}
    />
  </>
  )
}

export { CloseDialogButton };

const Fields = {
  CloseDialogButton, RoleName, RoleDescription, RoleParent, RolePermissions, RolePrincipals,
  RoleParentOverview, RolePermissionsOverview, RoleMembersOverview
};
export default Fields;