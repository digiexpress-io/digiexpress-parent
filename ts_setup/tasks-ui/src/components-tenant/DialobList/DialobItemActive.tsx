import React from 'react';
import { Box, Stack, Typography, IconButton, Skeleton, useTheme, CircularProgress, Avatar, Chip, Grid } from '@mui/material';
import EditOutlinedIcon from '@mui/icons-material/ModeEditOutlineOutlined';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import RemoveRedEyeIcon from '@mui/icons-material/RemoveRedEye';
import CopyAllIcon from '@mui/icons-material/CopyAll';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import QuestionAnswerOutlinedIcon from '@mui/icons-material/QuestionAnswerOutlined';
import { FormattedMessage } from 'react-intl';

import { TenantEntryDescriptor } from 'descriptor-tenant';
import Burger from 'components-burger';
import Context from 'context';
import { DialobTag, DialobForm, DialobVariable, DialobSession } from 'client';
import DialobDeleteDialog from '../DialobDelete';
import DialobSessionsDialog from '../DialobSessions';
import DialobCopyDialog from 'components-tenant/DialobCopy';
import { DialobEditor } from '../DialobEditor';

const StyledStack: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const theme = useTheme();

  return (<Box sx={{
    height: '100%',
    position: 'fixed',
    overflowY: 'scroll',
    overflowX: 'hidden',
    boxShadow: 1,
    width: '23vw',
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

const copyToClipboard = (text: string) => {
  navigator.clipboard.writeText(text);
}

const DialobItemActive: React.FC<{ entry: TenantEntryDescriptor | undefined }> = ({ entry }) => {
  const [dialobEditOpen, setDialobEditOpen] = React.useState(false);
  const [dialobDeleteOpen, setDialobDeleteOpen] = React.useState(false);
  const [dialobCopyOpen, setDialobCopyOpen] = React.useState(false);
  const [technicalNameEdit, setTechnicalNameEdit] = React.useState(false);
  const [sessionsOpen, setSessionsOpen] = React.useState(false);
  const [editOpen, setEditOpen] = React.useState(false);
  const [sessions, setSessions] = React.useState<DialobSession[]>();


  const [form, setForm] = React.useState<DialobForm | undefined>();
  const backend = Context.useBackend();

  function handleDelete() {
    setDialobDeleteOpen(prev => !prev);
  }
  function handleCopy() {
    setDialobCopyOpen(prev => !prev);
  }
  function handleTechnicalNameEdit() {
    setTechnicalNameEdit(prev => !prev);
  }
  function handleSessionsDialog() {
    setSessionsOpen(prev => !prev);
  }

  function handleEditToggle() {
    setEditOpen(prev => !prev);
  }

  React.useEffect(() => {
    if (entry?.formName) {
      backend.tenant.getDialobForm(entry.formName).then((form) => {
        backend.tenant.getDialobSessions({ formId: form._id, technicalName: entry.formName, tenantId: entry.tenantId }).then(sessions => {
          setSessions(sessions);
          setForm(form)
        })
      });
    }
  }, [entry]);


  if (entry) {

    return (<>
      <DialobDeleteDialog open={dialobDeleteOpen} onClose={handleDelete} entry={entry} />
      <DialobCopyDialog open={dialobCopyOpen} onClose={handleCopy} entry={entry} />
      {sessionsOpen && <DialobSessionsDialog onClose={handleSessionsDialog} entry={entry} form={form} sessions={sessions} />}
      {editOpen ? <DialobEditor onClose={handleEditToggle} entry={entry} form={form} /> : null}
      <StyledStack >

        {/* buttons section */}
        <Burger.Section>
          <StyledTitle children='task.tools' />
          <Stack direction='row' spacing={1} justifyContent='space-evenly' >

            <Box display='flex' flexDirection='column' alignItems='center'>
              <IconButton onClick={handleDelete}><DeleteForeverIcon sx={{ color: 'error.main' }} /></IconButton>
              <Typography><FormattedMessage id='dialob.form.delete' /></Typography>
            </Box>

            <Box display='flex' flexDirection='column' alignItems='center'>
              <IconButton onClick={handleCopy}><CopyAllIcon sx={{ color: 'secondary.light' }} /></IconButton>
              <Typography><FormattedMessage id='dialob.form.copy' /></Typography>
            </Box>

            <Box display='flex' flexDirection='column' alignItems='center'>
              <IconButton onClick={handleSessionsDialog}><QuestionAnswerOutlinedIcon sx={{ color: 'secondary.dark' }} /></IconButton>
              <Typography><FormattedMessage id='dialob.form.sessions' /></Typography>
            </Box>

            <Box display='flex' flexDirection='column' alignItems='center'>
              <IconButton onClick={handleEditToggle}><EditOutlinedIcon sx={{ color: 'uiElements.main' }} /></IconButton>
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

          <Grid container alignItems='center'>
            <Grid item md={4} lg={4} xl={4}>
              <Typography fontWeight='bolder'><FormattedMessage id='dialob.form.title' /></Typography>
            </Grid>
            <Grid item md={8} lg={8} xl={8}>
              <Typography>{entry.formTitle}</Typography>
            </Grid>

            <Grid item md={4} lg={4} xl={4}>
              <Typography fontWeight='bolder'><FormattedMessage id='dialob.form.technicalName' /></Typography>
            </Grid>

            <Grid item md={8} lg={8} xl={8}>
              <Box display='flex' alignItems='center'>
                <Typography>{entry.formName}</Typography>
                <Box flexGrow={1} />
                <IconButton size='small' onClick={() => copyToClipboard(entry.formName)}><ContentCopyIcon sx={{ color: 'uiElements.main', fontSize: 'medium' }} /></IconButton>
              </Box>
            </Grid>


            <Grid item md={4} lg={4} xl={4}>
              <Box display='flex' alignItems='center'>
                <Typography fontWeight='bolder'><FormattedMessage id='dialob.form.sessions' /></Typography>
              </Box>
            </Grid>
            <Grid item md={8} lg={8} xl={8}>
              <Typography>{sessions?.length}</Typography>
            </Grid>


            <Grid item md={1} lg={1} xl={1}>
              <Box display='flex' alignItems='center'>
                <Typography fontWeight='bolder'><FormattedMessage id='dialob.form.id' /></Typography>
              </Box>
            </Grid>

            <Grid item md={11} lg={11} xl={11}>
              <Typography sx={{ wordWrap: 'break-word' }}>{form?._id}</Typography>
            </Grid>

          </Grid>
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