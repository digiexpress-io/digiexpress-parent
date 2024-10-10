import React from 'react';
import {
  Box, Typography, IconButton,
  TableCell, TableRow, Card
} from '@mui/material';
import GetAppIcon from '@mui/icons-material/GetApp';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';

import { FormattedMessage } from 'react-intl';
import fileDownload from 'js-file-download'

import Burger from '@the-wrench-io/react-burger';
import { Composer } from '../context';
import { ReleaseComposer, ReleaseDelete } from './';


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
              <FormattedMessage id="releases" />: {releases.length}
              <Typography variant="body2" sx={{ pt: 1 }}><FormattedMessage id={"release.desc"} /></Typography>
            </Typography>
          </Box>
          <Box flexGrow={1} />
          <Box>
            <Burger.SecondaryButton label={"button.cancel"} onClick={() => layout.actions.handleTabCloseCurrent()} sx={{ marginRight: 1 }} />
            <Burger.SecondaryButton label={"button.releasegraph"} onClick={() => layout.actions.handleTabAdd({ id: 'graph', label: "Release Graph" })} sx={{ marginRight: 1 }} />
            <Burger.PrimaryButton label={"button.create"} onClick={() => setReleaseComposer(true)} />
          </Box>
        </Box>

        <Box display="flex" sx={{ justifyContent: 'center' }}>

          <Card sx={{ margin: 1, width: 'fill-available' }}>
            <Typography variant="h4" sx={{ p: 2, backgroundColor: "table.main" }}>
              <FormattedMessage id="releases" />
            </Typography>
            <Burger.ReleaseTable releases={releases} tableRowComponent={Row} />
          </Card>
        </Box>
      </Box>
    </>
  );
}


const Row: React.FC<{ release: Burger.Release }> = ({ release }) => {
  const [releaseDeleteOpen, setReleaseDeleteOpen] = React.useState(false);
  const { service } = Composer.useComposer();

  const onDownload = (releaseId: string) => {
    service.getReleaseContent(releaseId).then(content => {
      const data = JSON.stringify(content, null, 2);
      fileDownload(data, release.body.name + '.json');
    })  
  }

  return (
    <>
      {releaseDeleteOpen ? <ReleaseDelete id={release.id} onClose={() => setReleaseDeleteOpen(false)} /> : null}

      <TableRow key={release.id}>
        <TableCell align="left" >{release.body.name}</TableCell>
        <TableCell align="left"><Burger.DateTimeFormatter timestamp={release.body.created} /></TableCell>
        <TableCell align="left">{release.body.note}</TableCell>
        <TableCell align="center" >
          <IconButton onClick={() => onDownload(release.id)} sx={{ color: 'uiElements.main' }}><GetAppIcon /> </IconButton>
        </TableCell>
        <TableCell align="center" >
          <IconButton onClick={() => setReleaseDeleteOpen(true)} sx={{ color: 'error.main' }}><DeleteOutlineIcon /> </IconButton>
        </TableCell>
      </TableRow>
    </>
  )
}

export { ReleasesView }
