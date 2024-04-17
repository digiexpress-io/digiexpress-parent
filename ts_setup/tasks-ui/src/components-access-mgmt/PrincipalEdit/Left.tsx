import React from 'react';
import { Typography, TextField } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
import Burger from 'components-burger';

import { SectionLayout } from 'components-generic';
import { usePrincipalEdit } from './PrincipalEditContext';

const PrincipalUsername: React.FC = () => {
  const intl = useIntl();
  const { setName, entity } = usePrincipalEdit();

  function handleUsernameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setName(event.target.value);
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.principal.name.create.placeholder' })}
    fullWidth
    value={entity.name}
    disabled
    onChange={handleUsernameChange}
  />)
}


const PrincipalEmail: React.FC = () => {
  const intl = useIntl();
  const { setEmail, entity } = usePrincipalEdit();

  function handleEmailChange(event: React.ChangeEvent<HTMLInputElement>) {
    setEmail(event.target.value);
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.principal.email.create.placeholder' })}
    fullWidth
    value={entity.email}
    disabled
    onChange={handleEmailChange}
  />)
}

const PrincipalCommitComment: React.FC = () => {
  const intl = useIntl();
  const { setCommitComment, entity } = usePrincipalEdit();

  function handleChangeCommitComment(event: React.ChangeEvent<HTMLInputElement>) {
    setCommitComment(event.target.value);
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.principal.commitComment.placeholder' })}
    fullWidth
    value={entity.commitComment}
    disabled
    onChange={handleChangeCommitComment}
  />)
}

export const Left: React.FC<{}> = () => {
  const { entity } = usePrincipalEdit();

  return (
    <>
      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.principal.username' /></Typography>
        <PrincipalUsername />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.principal.email' /></Typography>
        <PrincipalEmail />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.principal.commitComment' /></Typography>
        <PrincipalCommitComment />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.principal.rolesOverview' /></Typography>
        {entity.roles.length === 0 ? <SectionLayout label='permissions.principal.rolesOverview.none' value={undefined} /> :
          entity.roles.map((role, index) => <SectionLayout label='permissions.principals.rolesOverview.roleName' value={role} key={index} />)}
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.principal.permissionsOverview' /></Typography>
        {entity.permissions.length === 0 ? <SectionLayout label='permissions.principal.permissionsOverview.none' value={undefined} /> :
          entity.permissions.map((permission, index) => <SectionLayout label='permissions.principal.permissionsOverview.permissionName' value={permission} key={index} />)
        }

      </Burger.Section>
    </>)
}


