import * as React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Typography, TextField, Stack, Divider } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { NavigationButtonSearch } from '../NavigationSticky';
import ProjectRepoType from '../ProjectRepoType';
import Burger from '@the-wrench-io/react-burger';
import Client from 'client';
import Section from 'section';


const ProjectCreate: React.FC<{}> = () => {
  const [open, setOpen] = React.useState(false);
  const [title, setTitle] = React.useState('project title');
  const [description, setDescription] = React.useState('project description');
  const [repoType, setRepoType] = React.useState<Client.RepoType>('TASKS');

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
      <Divider />

      <DialogContent>
        <Stack spacing={1}>

          <Section>
            <Typography fontWeight='bold'><FormattedMessage id='project.title' /></Typography>
            <TextField InputProps={{ disableUnderline: true }}
              variant='standard'
              value={title}
              onChange={handleTitleChange}
              fullWidth />
          </Section>

          <Section>
            <Typography fontWeight='bold'><FormattedMessage id='project.description' /></Typography>
            <TextField
              InputProps={{ disableUnderline: true }}
              variant='standard'
              value={description}
              onChange={handleDescriptionChange}
              fullWidth />
          </Section>

          <Section>
            <Typography fontWeight='bold'><FormattedMessage id='project.repoType' /></Typography>
            <ProjectRepoType onChange={async (newType) => setRepoType(newType)} project={{ repoType }} />
          </Section>
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