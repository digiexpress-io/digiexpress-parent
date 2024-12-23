import React from 'react';
import { Typography, TextField } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
import Burger from 'components-burger';
import { usePermissionEdit } from './PermissionEditContext';
import { SectionLayout } from 'components-generic';

const Left: React.FC<{}> = () => {
  const intl = useIntl();
  const { entity, setName, setCommitComment, setDescription } = usePermissionEdit();

  function handleDescriptionChange(event: React.ChangeEvent<HTMLInputElement>) {
    setDescription(event.target.value);
  }

  function handleCommentChange(event: React.ChangeEvent<HTMLInputElement>) {
    setCommitComment(event.target.value);
  }

  function handleNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setName(event.target.value);
  }

  return (<>
    <Burger.Section>
      <Typography fontWeight='bold'><FormattedMessage id='permissions.permission.name' /></Typography>
      <TextField InputProps={{ disableUnderline: true }} variant='standard'
        placeholder={intl.formatMessage({ id: 'permissions.permission.name.create.placeholder' })}
        fullWidth
        value={entity.name}
        onChange={handleNameChange}
      />
    </Burger.Section>

    <Burger.Section>
      <Typography fontWeight='bold'><FormattedMessage id='permissions.permission.description' /></Typography>
      <TextField InputProps={{ disableUnderline: true }} variant='standard'
        placeholder={intl.formatMessage({ id: 'permissions.permission.description.create.placeholder' })}
        fullWidth
        multiline
        minRows={3}
        maxRows={6}
        value={entity.description}
        onChange={handleDescriptionChange}
      />
    </Burger.Section>

    <Burger.Section>
      <Typography fontWeight='bold'><FormattedMessage id='permissions.permission.comment' /></Typography>
      <TextField InputProps={{ disableUnderline: true }} variant='standard'
        placeholder={intl.formatMessage({ id: 'permissions.permission.comment.placeholder' })}
        fullWidth
        required
        value={entity.commitComment}
        onChange={handleCommentChange}
      />
    </Burger.Section>

    <Burger.Section>
      <Typography fontWeight='bold'><FormattedMessage id='permissions.permission.edit.rolePermissionsOverview' /></Typography>
      {entity.roles.length === 0 ? <SectionLayout label='permissions.select.none' value={undefined} /> :
        entity.roles.map((role, index) => <SectionLayout label='permissions.role.name' key={index} value={role} />)}
    </Burger.Section>

    <Burger.Section>
      <Typography fontWeight='bold'><FormattedMessage id='permissions.permission.edit.rolePrincipalsOverview' /></Typography>
      {entity.principals.length === 0 ? <SectionLayout label='permissions.select.none' value={undefined} /> :
        entity.principals.map((principal, index) => <SectionLayout label='permissions.role.users.username' key={index} value={principal} />)}
    </Burger.Section>

  </>
  );
}


export { Left } 