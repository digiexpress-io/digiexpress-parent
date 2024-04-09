import React from 'react';
import { Typography, TextField } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
import Burger from 'components-burger';

import { SectionLayout } from 'components-generic';
import { useNewRole } from './RoleCreateContext';
import Context from 'context';


//TODO: Status field
const RoleName: React.FC<{}> = () => {
  const { setName, entity } = useNewRole();
  const intl = useIntl();

  function handleRoleNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setName(event.target.value);
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.roles.name.create.placeholder' })}
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
    placeholder={intl.formatMessage({ id: 'permissions.roles.description.create.placeholder' })}
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
    placeholder={intl.formatMessage({ id: 'permissions.roles.commitComment.placeholder' })}
    fullWidth
    value={entity.commitComment}
    onChange={handleCommitCommentChange}
  />);
}



export const Left: React.FC<{}> = () => {
  const { entity } = useNewRole();
  const { getRole } = Context.useAm();

  React.useEffect(() => {
  }, [entity.permissions]);


  const parentRole = entity.parentId ? getRole(entity.parentId) : undefined;

  return (
    <>
      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.roles.name' /></Typography>
        <RoleName />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.roles.description' /></Typography>
        <RoleDescription />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.roles.commitComment' /></Typography>
        <RoleCommitComment />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.roles.roleParentOverview' /></Typography>
        {parentRole?.name ? <SectionLayout label='permissions.role.roleParentName' value={parentRole?.name} /> :
          <SectionLayout label='permissions.select.none' value={undefined} />}
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.roles.rolePermissionsOverview' /></Typography>
        {entity.permissions.length === 0 ? <SectionLayout label='permissions.select.none' value={undefined} /> :
          entity.permissions.map((permission, index) => <SectionLayout label='permissions.permission.name' key={index} value={permission} />)}
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.roles.rolePrincipalsOverview' /></Typography>
        {entity.principals.length === 0 ? <SectionLayout label='permissions.select.none' value={undefined} /> :
          entity.principals.map((principal, index) => <SectionLayout label='permissions.role.users.username' key={index} value={principal} />)}
      </Burger.Section>
    </>)
}




