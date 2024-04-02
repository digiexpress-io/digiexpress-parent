import React from 'react';
import { Typography, TextField } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
import Burger from 'components-burger';

import { SectionLayout } from 'components-generic';
import Context from 'context';
import { CreateRole, ImmutablePermissionStore } from 'descriptor-access-mgmt';
import { useNewRole } from './RoleCreateContext';
import { usePermissions } from 'components-access-mgmt/AccessMgmtContext';



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



export const Left: React.FC<{}> = () => {
  const { entity } = useNewRole();
  const { getPermissionById } = usePermissions();

  const parentRole = entity.parentId ? getPermissionById(entity.parentId) : undefined;
  return (
    <>
      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.role.name' /></Typography>
        <RoleName />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.role.description' /></Typography>
        <RoleDescription />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.role.roleParentOverview' /></Typography>
        <SectionLayout label='permissions.role.roleParentName' value={parentRole?.name} />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.role.rolePermissionsOverview' /></Typography>
        <SectionLayout label='permissions.role.permissions.permissionName' value={'permission 1'} />
        <SectionLayout label='permissions.role.permissions.permissionName' value={'permission 2'} />
        <SectionLayout label='permissions.role.permissions.permissionName' value={'permission 3'} />
        <SectionLayout label='permissions.role.permissions.permissionName' value={'permission 4'} />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.role.roleMembersOverview' /></Typography>
        <SectionLayout label='permissions.role.users.username' value={'member 1'} />
        <SectionLayout label='permissions.role.users.username' value={'member 2'} />
        <SectionLayout label='permissions.role.users.username' value={'member 3'} />
        <SectionLayout label='permissions.role.users.username' value={'member 4'} />
      </Burger.Section>
    </>)
}




