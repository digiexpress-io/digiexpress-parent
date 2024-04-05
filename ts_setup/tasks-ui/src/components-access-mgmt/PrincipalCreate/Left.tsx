import React from 'react';
import { Typography, TextField } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
import Burger from 'components-burger';

import { SectionLayout } from 'components-generic';
import { useNewPrincipal } from './PrincipalCreateContext';


const PrincipalUsername: React.FC = () => {
  const intl = useIntl();
  const { setUsername, entity } = useNewPrincipal();

  function handleUsernameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setUsername(event.target.value);
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.principal.name.create.placeholder' })}
    fullWidth
    value={entity.username}
    onChange={handleUsernameChange}
  />)
}


const PrincipalEmail: React.FC = () => {
  const intl = useIntl();
  const { setEmail, entity } = useNewPrincipal();

  function handleEmailChange(event: React.ChangeEvent<HTMLInputElement>) {
    setEmail(event.target.value);
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.principal.email.create.placeholder' })}
    fullWidth
    value={entity.email}
    onChange={handleEmailChange}
  />)
}

const PrincipalCommitComment: React.FC = () => {
  const intl = useIntl();
  const { setCommitComment, entity } = useNewPrincipal();

  function handleChangeCommitComment(event: React.ChangeEvent<HTMLInputElement>) {
    setCommitComment(event.target.value);
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'permissions.principal.commitComment.placeholder' })}
    fullWidth
    value={entity.commitComment}
    onChange={handleChangeCommitComment}
  />)
}

export const Left: React.FC<{}> = () => {

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
        <Typography fontWeight='bold'><FormattedMessage id='permissions.role.roleMembersOverview' /></Typography>
        <SectionLayout label='permissions.role.users.username' value={'member 1'} />
        <SectionLayout label='permissions.role.users.username' value={'member 2'} />
        <SectionLayout label='permissions.role.users.username' value={'member 3'} />
        <SectionLayout label='permissions.role.users.username' value={'member 4'} />
      </Burger.Section>
    </>)
}


