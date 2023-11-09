import React from 'react';
import { Box, Stack, Typography, IconButton, Skeleton, useTheme } from '@mui/material';
import EditIcon from '@mui/icons-material/ModeEditOutlineOutlined';
import CrmIcon from '@mui/icons-material/AdminPanelSettingsOutlined';
import { FormattedMessage } from 'react-intl';

import Context from 'context';
import { TenantEntryDescriptor } from 'descriptor-tenant';
import Burger from 'components-burger';

const StyledStack: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const theme = useTheme();

  return (<Box sx={{
    height: '100%',
    position: 'fixed',
    //height: 'vh',
    overflowY: 'scroll',
    overflowX: 'hidden',
    boxShadow: 1,
    width: '23%',
    pt: theme.spacing(2),
    px: theme.spacing(2),
    backgroundColor: theme.palette.background.paper
  }}>
    <Stack direction='column' spacing={1}>
      {children}
    </Stack>
  </Box >);
}



const StyledTitle: React.FC<{ children: string }> = ({ children }) => {
  return (<Typography fontWeight='bold'><FormattedMessage id={children} /></Typography>)
}



const DialobItemActive: React.FC<{ entry: TenantEntryDescriptor | undefined }> = ({ entry }) => {
  const [crmOpen, setCrmOpen] = React.useState(false);
  const [taskEditOpen, setTaskEditOpen] = React.useState(false);

  const tasks = Context.useTenants();
  const backend = Context.useBackend();



  function handleCrm() {
    setCrmOpen(prev => !prev);
  }

  function handleTaskEdit() {
    setTaskEditOpen(prev => !prev);
  }


  if (entry) {


    return (<>
      <StyledStack>

        {/* duedate alert section */}


        {/* buttons section */}
        <Burger.Section>
          <StyledTitle children='task.tools' />
          <Stack direction='row' spacing={1} justifyContent='center'>
            <IconButton onClick={handleTaskEdit}><EditIcon sx={{ color: 'uiElements.main' }} /></IconButton>
            <IconButton onClick={handleCrm}><CrmIcon sx={{ color: 'locale.dark' }} /></IconButton>
          </Stack>
        </Burger.Section>

        {/* title section */}
        <Burger.Section>
          <StyledTitle children='task.title' />
          <Typography fontWeight='bold'>{entry.formTitle}</Typography>
        </Burger.Section>

        {/* description section */}
        <Burger.Section>
          <StyledTitle children='task.description' />
          <Typography>{entry.created.getTime()}</Typography>
        </Burger.Section>


      </StyledStack >
    </>

    );
  }

  return (<StyledStack>
    <Skeleton animation={false} variant="rounded" width='100%' height={40} />
    <Skeleton animation={false} variant="rounded" width='100%' height={40} />
    <Skeleton animation={false} variant="rounded" width='100%' height={40} />

    <Skeleton animation={false} variant="text" width='100%' height='2rem' />
    <Skeleton animation={false} variant="rounded" width='100%' height={70} />

    <Skeleton animation={false} variant="text" width='100%' height='2rem' />
    <Skeleton animation={false} variant="text" width='85%' height='1rem' />
    <Skeleton animation={false} variant="text" width='35%' height='1rem' />
    <Skeleton animation={false} variant="text" width='60%' height='1rem' />

    <Skeleton animation={false} variant="text" width='100%' height='2rem' />
    <Skeleton animation={false} variant="rounded" width='25%' height={30} sx={{ borderRadius: '15px' }} />
  </StyledStack>);
}

const TaskItemActiveWithRefresh: React.FC<{ entry: TenantEntryDescriptor | undefined }> = ({ entry }) => {
  const [dismount, setDismount] = React.useState(false);

  React.useEffect(() => {
    if (dismount) {
      setDismount(false);
    }
  }, [dismount]);

  React.useEffect(() => {
    setDismount(true);
  }, [entry]);

  if (dismount) {
    return null;
  }

  return (<DialobItemActive entry={entry} />)
}


export default TaskItemActiveWithRefresh;