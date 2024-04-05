import React from 'react';
import { Typography, TextField } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
import Burger from 'components-burger';
import { useNewPermission } from './PermissionCreateContext';

const Left: React.FC<{}> = () => {
  const intl = useIntl();
  const { entity, setCommitComment, setName, setDescription } = useNewPermission();

  function handleNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setName(event.target.value);
  }

  function handleDescriptionChange(event: React.ChangeEvent<HTMLInputElement>) {
    setDescription(event.target.value);
  }

  function handleCommentChange(event: React.ChangeEvent<HTMLInputElement>) {
    setCommitComment(event.target.value);
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
      <Typography fontWeight='bold'><FormattedMessage id='permissions.permission.createComment' /></Typography>
      <TextField InputProps={{ disableUnderline: true }} variant='standard'
        placeholder={intl.formatMessage({ id: 'permissions.permission.createComment.placeholder' })}
        fullWidth
        required
        value={entity.commitComment}
        onChange={handleCommentChange}
      />
    </Burger.Section>
  </>
  );
}


export { Left } 