import React from 'react';
import { Box, Stack, Typography, IconButton, Skeleton, useTheme, CircularProgress, Avatar, Chip } from '@mui/material';
import EditIcon from '@mui/icons-material/ModeEditOutlineOutlined';
import RemoveRedEyeIcon from '@mui/icons-material/RemoveRedEye';
import CopyAllIcon from '@mui/icons-material/CopyAll';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import { FormattedMessage } from 'react-intl';

import { TenantEntryDescriptor } from 'descriptor-tenant';
import Burger from 'components-burger';
import Context from 'context';
import { DialobTag, DialobForm, DialobVariable } from 'client';


const StyledStack: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const theme = useTheme();

  return (<Box sx={{
    height: '100%',
    position: 'fixed',
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


const DialobFormTags: React.FC<{ entry: TenantEntryDescriptor }> = ({ entry }) => {
  const backend = Context.useBackend();
  const [loading, setLoading] = React.useState(true);
  const [tags, setTags] = React.useState<DialobTag[]>([]);

  React.useEffect(() => {
    backend.tenant.getDialobTags(entry?.formName).then(tags => {
      setTags(tags);
      setLoading(false);
    });

  }, [entry]);

  if (loading) {
    return <CircularProgress size='10pt' />
  }
  if (!tags.length) {
    return (<Typography><FormattedMessage id='dialob.form.versionTags.latest' /></Typography>);
  }

  return (<>{tags.map((tag) => <Stack direction='row' spacing={1}>
    <Typography>{tag.name}</Typography>
  </Stack>)}</>);
}

const DialobFormLocales: React.FC<{ entry: TenantEntryDescriptor }> = ({ entry }) => {
  const backend = Context.useBackend();
  const [loading, setLoading] = React.useState(true);
  const [locales, setLocales] = React.useState<string[]>([]);


  React.useEffect(() => {
    backend.tenant.getDialobForm(entry.formName).then(form => {
      setLocales(form.metadata.languages);
      setLoading(false);
    });

  }, [entry]);

  if (loading) {
    return <CircularProgress size='10pt' />
  }
  if (!locales.length) {
    return (<Typography><FormattedMessage id='dialob.form.languages' /></Typography>);
  }

  return (<>{locales.map((locale, index) => <Typography display='inline'>
    {locales.length - 1 === index ? locale : locale += ', '}</Typography>)}
  </>);
}

const DialobFormLabels: React.FC<{ entry: TenantEntryDescriptor }> = ({ entry }) => {
  const backend = Context.useBackend();
  const [loading, setLoading] = React.useState(true);
  const [labels, setLabels] = React.useState<string[] | undefined>();


  React.useEffect(() => {
    backend.tenant.getDialobForm(entry.formName).then(form => {
      setLabels(form.metadata.labels);
      setLoading(false);
    });

  }, [entry]);


  if (!labels?.length) {
    return (<Typography><FormattedMessage id='dialob.form.labels.none' /></Typography>);
  }

  return (<>{labels.map((label) => <Chip label={label} size="small" variant='outlined' sx={{ mr: 1 }} />)}
  </>);
}



const VariableAvatar: React.FC<{ value: string }> = ({ value }) => {
  return (<Avatar sx={{ width: 15, height: 15, fontSize: '8px', backgroundColor: 'lightblue', color: 'black' }}>{value}</Avatar>)
}

const DialobFormVariables: React.FC<{ entry: TenantEntryDescriptor }> = ({ entry }) => {
  const backend = Context.useBackend();
  const [loading, setLoading] = React.useState(true);
  const [variables, setVariables] = React.useState<DialobVariable[] | undefined>(undefined);

  React.useEffect(() => {
    backend.tenant.getDialobForm(entry.formName).then(data => {
      setVariables(data.variables);
      setLoading(false);
    });

  }, [entry]);

  if (loading) {
    return <CircularProgress size='10pt' />
  }
  if (!variables || !variables.length) {
    return (<Typography><FormattedMessage id='dialob.form.variables.none' /></Typography>);
  }

  return (<>{variables.map((variable) => <Stack direction='row' spacing={1}>
    <Box display='flex' alignItems='center'>
      <Typography fontWeight='bold'>{variable.context === undefined ?
        <VariableAvatar value={'E'} /> :
        <VariableAvatar value={'C'} />}</Typography>
    </Box>

    <Typography fontWeight='bold'><FormattedMessage id='dialob.form.variable.type.context.name' /></Typography>
    <Typography>{variable.name}</Typography>

    {variable.context && <><Typography fontWeight='bold'><FormattedMessage id='dialob.form.variable.type.context.type' /></Typography>
      <Typography>{variable.contextType}</Typography></>}

  </Stack>)}</>);
}



const DialobItemActive: React.FC<{ entry: TenantEntryDescriptor | undefined }> = ({ entry }) => {
  const [crmOpen, setCrmOpen] = React.useState(false);
  const [dialobEditOpen, setDialobEditOpen] = React.useState(false);
  const [form, setForm] = React.useState<DialobForm | undefined>();
  const backend = Context.useBackend();


  React.useEffect(() => {
    if (entry?.formName) {
      backend.tenant.getDialobForm(entry.formName).then(setForm);
    }
  }, [entry]);
  const x = form?.data;

  console.log(form?.metadata.labels)

  function handleTaskEdit() {
    setDialobEditOpen(prev => !prev);
  }
  if (entry) {

    return (<>
      <StyledStack >

        {/* buttons section */}
        <Burger.Section>
          <StyledTitle children='task.tools' />
          <Stack direction='row' spacing={1} justifyContent='space-evenly'>

            <Box display='flex' flexDirection='column' alignItems='center'>
              <IconButton onClick={() => { }}><DeleteForeverIcon sx={{ color: 'error.main' }} /></IconButton>
              <Typography><FormattedMessage id='dialob.form.delete' /></Typography>
            </Box>

            <Box display='flex' flexDirection='column' alignItems='center'>
              <IconButton onClick={() => { }}><CopyAllIcon sx={{ color: 'secondary.light' }} /></IconButton>
              <Typography><FormattedMessage id='dialob.form.copy' /></Typography>
            </Box>

            <Box display='flex' flexDirection='column' alignItems='center'>
              <IconButton onClick={handleTaskEdit}><EditIcon sx={{ color: 'uiElements.main' }} /></IconButton>
              <Typography><FormattedMessage id='dialob.form.edit' /></Typography>
            </Box>

            <Box display='flex' flexDirection='column' alignItems='center'>
              <IconButton onClick={() => { }}><RemoveRedEyeIcon sx={{ color: 'locale.dark' }} /></IconButton>
              <Typography><FormattedMessage id='dialob.form.preview' /></Typography>
            </Box>
          </Stack>
        </Burger.Section>

        {/* info section */}
        <Burger.Section loadingValue={form}>
          <StyledTitle children='dialob.form.info' />
          <Stack>
            <Box display='flex'>
              <Typography fontWeight='bolder'><FormattedMessage id='dialob.form.title' /></Typography>
              <Box flexGrow={1} />
              <Typography>{entry.formTitle}</Typography>
            </Box>

            <Box display='flex'>
              <Typography fontWeight='bolder'><FormattedMessage id='dialob.form.technicalName' /></Typography>
              <Box flexGrow={1} />
              <Typography>{entry.formName}</Typography>
            </Box>

            <Box display='flex' alignItems='center'>
              <Typography fontWeight='bolder'><FormattedMessage id='dialob.form.id' /></Typography>
              <Box flexGrow={1} />
              <Typography>{form?._id}</Typography>
            </Box>

          </Stack>
        </Burger.Section>

        {/* labels section */}
        <Burger.Section>
          <StyledTitle children='dialob.form.labels' />
          <DialobFormLabels entry={entry} />
        </Burger.Section>

        {/* language section */}
        <Burger.Section loadingValue={entry}>
          <StyledTitle children='dialob.form.languages' />
          <DialobFormLocales entry={entry} />
        </Burger.Section>

        {/* created date section */}
        <Burger.Section>
          <StyledTitle children='dialob.form.created' />
          <Typography><Burger.DateTimeFormatter type='date' value={entry.created} /></Typography>
        </Burger.Section>

        {/* last saved date section */}
        <Burger.Section>
          <StyledTitle children='dialob.form.lastSaved' />
          <Typography><Burger.DateTimeFormatter type='date' value={entry.lastSaved} /></Typography>
        </Burger.Section>

        {/* version tag section */}
        <Burger.Section>
          <StyledTitle children='dialob.form.versionTags' />
          <DialobFormTags entry={entry} />
        </Burger.Section>

        {/* variables section */}
        <Burger.Section>
          <StyledTitle children='dialob.form.variables' />
          <DialobFormVariables entry={entry} />
        </Burger.Section>

      </StyledStack >
    </>

    );
  }

  return (<StyledStack>
    <Skeleton animation={false} variant="rounded" width='100%' height={40} />
    <Skeleton animation={false} variant="rounded" width='100%' height={40} />
    <Skeleton animation={false} variant="rounded" width='100%' height={40} />
    <Skeleton animation={false} variant="rounded" width='100%' height={40} />
  </StyledStack>);
}

const DialobItemActiveWithRefresh: React.FC<{ entry: TenantEntryDescriptor | undefined }> = ({ entry }) => {
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


export default DialobItemActiveWithRefresh;