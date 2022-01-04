import React from 'react';
import {
  Box, Typography, IconButton, Table, TableBody,
  TableCell, TableContainer, TableRow, TableHead, Paper, Card
} from '@mui/material';
import GetAppIcon from '@mui/icons-material/GetApp';
import { FormattedMessage } from 'react-intl';
import fileDownload from 'js-file-download'

import Burger from '@the-wrench-io/react-burger';
import { Composer, Client } from '../context';
import { ReleaseComposer } from './ReleaseComposer';

const ReleasesView: React.FC<{}> = () => {

  const { site } = Composer.useComposer();
  const layout = Burger.useTabs();
  const releases = Object.values(site.tags);
  const [releaseComposer, setReleaseComposer] = React.useState(false);

  const onDownload = (release?: Client.AstTag) => {
    if(release) {
      const data = JSON.stringify(release, null, 2);
      fileDownload(data, release.name + "_" + release.created + '.json');      
    }
  }

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

            <TableContainer component={Paper}>
              <Table size="small">
                <TableHead>
                  <TableRow sx={{ p: 1 }}>
                    <TableCell align="left" sx={{ fontWeight: 'bold' }}><FormattedMessage id="tag" /></TableCell>
                    <TableCell align="left" sx={{ fontWeight: 'bold' }}><FormattedMessage id="created" /></TableCell>
                    <TableCell align="left" sx={{ fontWeight: 'bold' }}><FormattedMessage id="release.composer.note" /></TableCell>
                    <TableCell align="center" sx={{ fontWeight: 'bold' }}><FormattedMessage id="download" /></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {releases.map((release, index) => (
                    <Row key={index}
                      release={release}
                      onDownload={() => onDownload(release.ast)}
                    />))}
                </TableBody>
              </Table>
            </TableContainer>
          </Card>
        </Box>
      </Box>
    </>
  );
}


const Row: React.FC<{ release: Client.Entity<Client.AstTag>, onDownload: () => void }> = ({ release, onDownload }) => {
  return (
    <>
      <TableRow key={release.id}>
        <TableCell align="left" >{release.ast?.name}</TableCell>
        <TableCell align="left">{release.ast?.created}</TableCell>
        <TableCell align="left">{release.ast?.description}</TableCell>
        <TableCell align="center" >
          <IconButton onClick={onDownload} sx={{ color: 'uiElements.main' }}><GetAppIcon /> </IconButton>
        </TableCell>
      </TableRow>
    </>
  )
}

export { ReleasesView }




