import * as React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Typography, TextField, Stack } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import Context from 'context';
import { NavigationButtonSearch } from '../NavigationSticky';
import ProjectRepoType from '../ProjectRepoType';
import Burger from '@the-wrench-io/react-burger';
import Client from 'client';


const ProjectCreate: React.FC<{
}> = () => {
  const ctx = Context.useProjects();

  const [open, setOpen] = React.useState(false);
  const [title, setTitle] = React.useState('project title');
  const [description, setDescription] = React.useState('project description');
  const [repoType, setRepoType] = React.useState<Client.RepoType>('tasks');

  function handleDialog() {
    setOpen(prev => !prev);
  }

  function handleTitleChange(event: React.ChangeEvent<HTMLInputElement>) {
    setTitle(event.target.value);
  }
  function handleDescriptionChange(event: React.ChangeEvent<HTMLInputElement>) {
    setDescription(event.target.value);
  }

  return (<>
    <Dialog fullWidth maxWidth='md' open={open} onClose={handleDialog}>
      <DialogTitle>
        <Typography variant='body2'><FormattedMessage id='project.search.searchBar.newProject.dialog.title' /></Typography>
      </DialogTitle>
      <DialogContent>
        <Stack spacing={1}>
          <TextField value={title} onChange={handleTitleChange} fullWidth />
          <TextField value={description} onChange={handleDescriptionChange} fullWidth />
          <ProjectRepoType onChange={async (newType) => setRepoType(newType)} project={{ repoType }} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Burger.PrimaryButton label='buttons.accept' onClick={handleDialog} />
      </DialogActions>
    </Dialog>
    <NavigationButtonSearch onClick={handleDialog} id='project.search.searchBar.newProject' values={undefined} />
  </>
  );
}

export default ProjectCreate;