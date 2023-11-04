import * as React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Typography, TextField, Stack, Divider } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { NavigationButtonSearch } from '../NavigationSticky';
import ProjectRepoType from '../ProjectRepoType';
import Burger from 'components-burger';
import Client from 'client';
import Context from 'context';



const ProjectCreate: React.FC<{
}> = () => {
  const ctx = Context.useProjects();
  const backend = Context.useBackend();

  const [open, setOpen] = React.useState(false);
  const [title, setTitle] = React.useState('project title');
  const [description, setDescription] = React.useState('project description');
  const [repoType, setRepoType] = React.useState<Client.RepoType>('TASKS');

  function handleToggleDialog() {
    setOpen(prev => !prev);
  }

  function handleTitleChange(event: React.ChangeEvent<HTMLInputElement>) {
    setTitle(event.target.value);
  }
  function handleDescriptionChange(event: React.ChangeEvent<HTMLInputElement>) {
    setDescription(event.target.value);
  }

  function handleAccept() {
    const command: Client.CreateProject = {
      commandType: 'CreateProject',
      description,
      repoType: repoType,
      title,
      repoId: title + '-' + repoType,
      users: []
    };
    backend.project.createProject(command)
      .then(_data => ctx.reload())
      .then(() => handleToggleDialog());
    ;
  }


  return (<>
    <Dialog fullWidth maxWidth='md' open={open} onClose={handleToggleDialog}>
      <DialogTitle>
        <Typography variant='body2'><FormattedMessage id='project.search.searchBar.newProject.dialog.title' /></Typography>
      </DialogTitle>
      <Divider />

      <DialogContent>
        <Stack spacing={1}>

          <Burger.Section>
            <Typography fontWeight='bold'><FormattedMessage id='project.title' /></Typography>
            <TextField InputProps={{ disableUnderline: true }}
              variant='standard'
              value={title}
              onChange={handleTitleChange}
              fullWidth />
          </Burger.Section>

          <Burger.Section>
            <Typography fontWeight='bold'><FormattedMessage id='project.description' /></Typography>
            <TextField
              InputProps={{ disableUnderline: true }}
              variant='standard'
              value={description}
              onChange={handleDescriptionChange}
              fullWidth />
          </Burger.Section>

          <Burger.Section>
            <Typography fontWeight='bold'><FormattedMessage id='project.repoType' /></Typography>
            <ProjectRepoType onChange={async (newType) => setRepoType(newType)} project={{ repoType }} />
          </Burger.Section>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Burger.PrimaryButton label='buttons.accept' onClick={handleAccept} />
      </DialogActions>
    </Dialog>
    <NavigationButtonSearch onClick={handleToggleDialog} id='project.search.searchBar.newProject' values={undefined} />
  </>
  );
}

export default ProjectCreate;