import React from 'react';
import {
  Box, Typography, IconButton,
  TableCell, TableRow, Card,
} from '@mui/material';
import GetAppIcon from '@mui/icons-material/GetApp';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import { FormattedMessage } from 'react-intl';
import fileDownload from 'js-file-download'
import { useSnackbar } from 'notistack';

import Burger from 'components-burger';
import { Composer, Client } from '../context';
import { ReleaseComposer } from './ReleaseComposer';
import { ErrorView } from '../styles';

const ReleasesView: React.FC<{}> = () => {

  const { site } = Composer.useComposer();
  const layout = Burger.useTabs();
  const releases = Object.values(site.tags);
  const [releaseComposer, setReleaseComposer] = React.useState(false);

  const formattedReleases: Burger.Release[] = releases.map((release) => {
    const { id } = release;
    const name  = release.ast?.name || '';
    const created = release.ast?.created || '';
    const note = release.ast?.description;
    const data = JSON.stringify(release.ast, null, 2);
    return {
      id,
      body: {
        name,
        note,
        created,
        data,
      }
    }
  });

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
            <Burger.ReleaseTable releases={formattedReleases} tableRowComponent={Row} />
          </Card>
        </Box>
      </Box>
    </>
  );
}



const ReleaseDelete: React.FC<{ release: Burger.Release, onClose: () => void }> = ({ release, onClose }) => {
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
      <FormattedMessage id="releases.delete.content" values={{ name: release.body.name }} />
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

        service.delete().tag(release.id)
          .then(data => {
            enqueueSnackbar(<FormattedMessage id="release.deleted.message" values={{ name: release.body.name }} />);
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



const Row: React.FC<{ release: Burger.Release }> = ({ release }) => {
  const [dialogOpen, setDialogOpen] = React.useState<boolean>(false);
  const handleDialogClose = () => setDialogOpen(false);

  const onDownload = (data: string | undefined) => {
    if (data) {
      fileDownload(data, release.body.name + "_" + release.body.created + '.json');
    }
  }

  return (
    <>
      <TableRow key={release.id}>
        <TableCell align="left" >{release.body.name}</TableCell>
        {/* @ts-ignore */}
        <TableCell align="left"><Burger.DateTimeFormatter timestamp={release.body.created} /></TableCell>
        <TableCell align="left">{release.body.note}</TableCell>
        <TableCell align="center">
          <IconButton onClick={() => onDownload(release.body.data)} sx={{ color: 'uiElements.main' }}><GetAppIcon /> </IconButton>
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
