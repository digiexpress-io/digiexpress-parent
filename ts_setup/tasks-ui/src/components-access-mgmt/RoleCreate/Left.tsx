import React from 'react';
import { Typography, TextField } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
import Burger from 'components-burger';

import { SectionLayout } from 'components-generic';
import { useNewRole } from './RoleCreateContext';
import { useAccessMgmt } from 'components-access-mgmt/AccessMgmtContext';



const RoleName: React.FC<{}> = () => {
  const { setName, entity } = useNewRole();
  const intl = useIntl();

  function handleRoleNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setName(event.target.value);
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.role.name.create.placeholder' })}
    fullWidth
    value={entity.name}
    onChange={handleRoleNameChange}
  />);
}

const RoleDescription: React.FC<{}> = () => {
  const { setDescription, entity } = useNewRole();
  const intl = useIntl();

  function handleDescriptionChange(event: React.ChangeEvent<HTMLInputElement>) {
    setDescription(event.target.value);
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.role.description.create.placeholder' })}
    fullWidth
    multiline
    minRows={3}
    maxRows={6}
    value={entity.description}
    onChange={handleDescriptionChange}
  />);
}

const RoleCommitComment: React.FC<{}> = () => {
  const { setCommitComment, entity } = useNewRole();
  const intl = useIntl();

  function handleCommitCommentChange(event: React.ChangeEvent<HTMLInputElement>) {
    setCommitComment(event.target.value);
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.role.commitComment.placeholder' })}
    fullWidth
    value={entity.commitComment}
    onChange={handleCommitCommentChange}
  />);
}



export const Left: React.FC<{}> = () => {
  const { entity } = useNewRole();
  const { getPermissionById } = useAccessMgmt();

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
        <Typography fontWeight='bold'><FormattedMessage id='permissions.role.commitComment' /></Typography>
        <RoleCommitComment />
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




