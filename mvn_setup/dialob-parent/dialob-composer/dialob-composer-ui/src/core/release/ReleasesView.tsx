import React from 'react';
import {
  Box, Typography, IconButton, Table, TableBody,
  TableCell, TableContainer, TableRow, TableHead, Paper, Card
} from '@mui/material';
import GetAppIcon from '@mui/icons-material/GetApp';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import { FormattedMessage } from 'react-intl';
import fileDownload from 'js-file-download'
import { useSnackbar } from 'notistack';

import Burger from '@the-wrench-io/react-burger';
import { Composer, Client } from '../context';
import { ReleaseComposer } from './ReleaseComposer';
import {ErrorView} from '../styles';

const ReleasesView: React.FC<{}> = () => {

  const { site } = Composer.useComposer();
  const layout = Burger.useTabs();
  const releases = Object.values(site.releases);
  const [releaseComposer, setReleaseComposer] = React.useState(false);

  return (
    <>
      {releaseComposer ? <ReleaseComposer onClose={() => setReleaseComposer(false)} /> : null}

      <Box sx={{ paddingBottom: 1, m: 2 }}>
        <Box display="flex">
          <Box alignSelf="center">
            <Typography variant="h3" sx={{ p: 1, mb: 3, fontWeight: "bold", color: "mainContent.dark" }}>
              <FormattedMessage id="activities.releases.title" />: {releases.length}
              <Typography variant="body2" sx={{ pt: 1 }}><FormattedMessage id={"activities.releases.desc"} /></Typography>
            </Typography>
          </Box>
          <Box flexGrow={1} />
          <Box>
            <Burger.SecondaryButton label={"button.cancel"} onClick={() => layout.actions.handleTabCloseCurrent()} sx={{ marginRight: 1 }} />
            <Burger.SecondaryButton label={"activities.releases.graph"} onClick={() => layout.actions.handleTabAdd({ id: 'graph', label: "Release Graph" })} sx={{ marginRight: 1 }} />
            <Burger.PrimaryButton label={"buttons.create"} onClick={() => setReleaseComposer(true)} />
          </Box>
        </Box>

        <Box display="flex" sx={{ justifyContent: 'center' }}>

          <Card sx={{ margin: 1, width: 'fill-available' }}>
            <Typography variant="h4" sx={{ p: 2, backgroundColor: "table.main" }}>
              <FormattedMessage id="activities.releases.title" />
            </Typography>

            <TableContainer component={Paper}>
              <Table size="small">
                <TableHead>
                  <TableRow sx={{ p: 1 }}>
                    <TableCell align="left" sx={{ fontWeight: 'bold' }}><FormattedMessage id="releases.view.tag" /></TableCell>
                    <TableCell align="left" sx={{ fontWeight: 'bold' }}><FormattedMessage id="releases.view.created" /></TableCell>
                    <TableCell align="left" sx={{ fontWeight: 'bold' }}><FormattedMessage id="releases.view.note" /></TableCell>
                    <TableCell align="center"><FormattedMessage id="releases.view.download" /></TableCell>
                    <TableCell align="right" sx={{ width: "30px" }}></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {releases.map(r => ({ id: new Date(r.created as string), body: r }))
                    .sort(({ id: a }, { id: b }) => a > b ? -1 : a < b ? 1 : 0)
                    .map((release, index) => (<Row key={index} release={release.body} />))}
                </TableBody>
              </Table>
            </TableContainer>
          </Card>
        </Box>
      </Box>
    </>
  );
}



const ReleaseDelete: React.FC<{ release: Client.Entity, onClose: () => void }> = ({ release, onClose }) => {
  const { service, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<Client.StoreError>();

  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="releases.delete.error.title" />
      </Typography>
      <ErrorView error={errors}/>
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <FormattedMessage id="releases.delete.content" values={{ name: release.name }} />
    </Typography>)
  }


  return (<Burger.Dialog open={true}
    onClose={onClose}
    children={editor}
    backgroundColor="uiElements.main"
    title='release.delete.title'
    submit={{
      title: "buttons.delete",
      disabled: apply,
      onClick: () => {
        setErrors(undefined);
        setApply(true);

        service.delete().release(release.id)
          .then(data => {
            enqueueSnackbar(<FormattedMessage id="release.deleted.message" values={{ name: release.name }} />);
            actions.handleLoadSite(data);
            onClose();
          })
          .catch((error: Client.StoreError) => {
            setErrors(error);
          });
      }
    }}
  />);
}



const Row: React.FC<{ release: Client.Release }> = ({ release }) => {
  const [dialogOpen, setDialogOpen] = React.useState<boolean>(false);
  const handleDialogClose = () => setDialogOpen(false);
  const composer = Composer.useComposer();

  const onDownload = () => {
    composer.service.releaseDump(release.id).then(dump => {
      fileDownload(dump.content, release.name + "_" + release.created + '_dump.txt');      
    });

  }

  return (
    <>
      <TableRow key={release.id}>
        <TableCell align="left" >{release.name}</TableCell>
        <TableCell align="left">{release.created}</TableCell>
        <TableCell align="left">{release.description}</TableCell>
        <TableCell align="center">
          <IconButton onClick={onDownload} sx={{ color: 'uiElements.main' }}><GetAppIcon /> </IconButton>
        </TableCell>
        <TableCell align="right">
          {dialogOpen ? <ReleaseDelete release={release} onClose={handleDialogClose} /> : null}
          <IconButton onClick={() => setDialogOpen(true)} sx={{ color: 'error.main' }}><DeleteOutlineOutlinedIcon /> </IconButton>
        </TableCell>
      </TableRow>
    </>
  )
}

export { ReleasesView }




