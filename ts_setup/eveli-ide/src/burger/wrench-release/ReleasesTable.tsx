import React from "react";

import { Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TableSortLabel, styled, alpha, IconButton, 
  DialogActions, Dialog, Button, DialogTitle, DialogContent, Typography, Box
  
} from "@mui/material";
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import ForkRightIcon from '@mui/icons-material/KeyboardArrowDown';

import { FormattedMessage, useIntl } from "react-intl";
import fileDownload from 'js-file-download'
import { useSnackbar } from 'notistack';

import * as Burger from '@/burger';
import { HdesApi } from '@/burger';
import { WrenchComposerApi as Composer } from '../wrench-setup';
import { Release } from "./release-types";
import { ReleaseComposer } from './ReleaseComposer';
import { ReleaseBranch } from './release-types';
import { AssetMapper } from '../wrench-compare/CompareView'
import { ErrorView } from '../wrench-styles';
import { ExplorerItem, useWrenchNav } from "../wrench-nav";

type SortOptions = 'name' | 'created';
type SortDirections = 'asc' | 'desc';

interface ReleasesTableProps {
  releases: Release[];
}

const StyledTableRow = styled(TableRow)(({ theme }) => ({
  backgroundColor: alpha(theme.palette.secondary.main, .05),
}));


const handleTabs = (actions: (items: ExplorerItem[]) => void) => {
  actions([
    {type: "ACTIVITIES"},
    {type: "RELEASES"},
  ]);
}

const resolveNewBranchName = (releaseName: string, branches: HdesApi.AstBranch[]): string => {
  const matches: HdesApi.AstBranch[] = branches.filter((branch) => branch.name.includes(releaseName));
  if (matches.length === 0) {
    return releaseName + "_dev";
  }
  const branchNo: number = matches.length + 1
  return releaseName + "_dev_" + branchNo;
}

const useSort = (releases: Release[], sort: SortOptions, direction: SortDirections) => {
  const activeBranch = Composer.useBranchName();
  const intl = useIntl();

  const latestRelease = {
    id: 'latest',
    body: {
      name: intl.formatMessage({ id: 'releases.latest.name' }),
      note: intl.formatMessage({ id: 'releases.latest.note' }),
      created: '',
      data: '',
    },
    branches: []
  };

  const defaultBranch = {
    id: 'default',
    body: {
      name: intl.formatMessage({ id: 'releases.default.name' }),
      note: intl.formatMessage({ id: 'releases.default.note' }),
      created: '',
      data: '',
    },
    branches: []
  };

  const defaultBranchRow = activeBranch === undefined ? [] : [defaultBranch];

  switch (sort) {
    case 'name':
      const sortedByName = [...releases].sort((a, b) => {
        const nameA = a.body.name;
        const nameB = b.body.name;
        return (direction === 'asc') ? (nameA.localeCompare(nameB)) : (nameB.localeCompare(nameA));
      });
      return [latestRelease, ...defaultBranchRow, ...sortedByName];
    case 'created':
      const sortedByCreated = [...releases].sort((a, b) => {
        const dateA = new Date(a.body.created);
        const dateB = new Date(b.body.created);
        return (direction === 'asc') ? (dateA.getTime() - dateB.getTime()) : (dateB.getTime() - dateA.getTime());
      });
      return [latestRelease, ...defaultBranchRow, ...sortedByCreated];
    default:
      return [];
  }
};


const DeleteDialog: React.FC<{ asset?: ReleaseBranch | Release, onClose: () => void, }> = ({ asset, onClose }) => {
  const { service, actions } = Composer.useComposer();
  const { onNavReset } = useWrenchNav();
  const { enqueueSnackbar, closeSnackbar } = useSnackbar();
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<HdesApi.StoreError>();

  if (asset === undefined) {
    return <></>;
  }

  const isBranch = (asset as ReleaseBranch).branch !== undefined;
  const prefix = isBranch ? "branch" : "release";
  const id = asset?.id;
  const name = isBranch ? (asset as ReleaseBranch).branch.name : (asset as Release).body.name;

  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id={prefix + ".delete.error.title"} />
      </Typography>
      <ErrorView error={errors} />
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <FormattedMessage id={prefix + ".delete.content"} values={{ name }} />
    </Typography>)
  }

  const handleDelete = () => {
    setErrors(undefined);
    setApply(true);

    if (isBranch) {
      const key = enqueueSnackbar(<FormattedMessage id="release.branch.deleting" values={{ name }} />, { persist: true });
      service.delete().branch(id)
        .then(async (data) => {
          actions.handleBranchUpdate("default");
          await actions.handleLoadSite(data);
          handleTabs(onNavReset);
          closeSnackbar(key);
          enqueueSnackbar(<FormattedMessage id="release.branch.deleted" values={{ name }} />);
        })
        .catch((error: HdesApi.StoreError) => {
          setErrors(error);
        })
    } else {
      service.delete().tag(id)
        .then(data => {
          enqueueSnackbar(<FormattedMessage id="release.deleted.message" values={{ name }} />);
          actions.handleLoadSite(data);
          onClose();
        })
        .catch((error: HdesApi.StoreError) => {
          setErrors(error);
        });
    }
  }
  return (
  <Dialog open={true} onClose={onClose}>
    <DialogTitle><FormattedMessage id={prefix + '.delete.title'} /></DialogTitle>
    <DialogContent>{editor}</DialogContent>
    <DialogActions>
      <Button variant='text' onClick={onClose}>
        <FormattedMessage id='button.cancel'/>
      </Button>
      <Button onClick={handleDelete} disabled={apply}>
        <FormattedMessage id='buttons.delete'/>
      </Button>
    </DialogActions>
  </Dialog>);
}

const ReleaseDelete: React.FC<{ release: Release, onClose: () => void }> = ({ release, onClose }) => {
  const { service, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<HdesApi.StoreError>();

  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="releases.delete.error.title" />
      </Typography>
      <ErrorView error={errors} />
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <FormattedMessage id="releases.delete.content" values={{ name: release.body.name }} />
    </Typography>)
  }

  const handleDelete = () => {
    setErrors(undefined);
    setApply(true);

    service.delete().tag(release.id)
      .then(data => {
        enqueueSnackbar(<FormattedMessage id="release.deleted.message" values={{ name: release.body.name }} />);
        actions.handleLoadSite(data);
        onClose();
      })
      .catch((error: HdesApi.StoreError) => {
        setErrors(error);
      });
  }

  return (
  <Dialog open={true} onClose={onClose}>
    <DialogTitle><FormattedMessage id='release.delete.title' /></DialogTitle>
    <DialogContent>{editor}</DialogContent>
    <DialogActions>
      <Button variant='text' onClick={onClose}>
        <FormattedMessage id='button.cancel'/>
      </Button>
      <Button onClick={handleDelete} disabled={apply}>
        <FormattedMessage id='buttons.delete'/>
      </Button>
    </DialogActions>
  </Dialog>);
}

const RelRow: React.FC<{ release: Release }> = ({ release }) => {
  const { service, actions, site } = Composer.useComposer();
  const { enqueueSnackbar, closeSnackbar } = useSnackbar();
  const { onNavReset } = useWrenchNav();
  const branches = Object.values(site.branches).map((b) => b.ast!);
  const [assetToDelete, setAssetToDelete] = React.useState<ReleaseBranch | Release>();
  //const [deleteDialogOpen, setDeleteDialogOpen] = React.useState<boolean>(false);
  //const [deleteBranchDialogOpen, setDeleteBranchDialogOpen] = React.useState<boolean>(false);
  const [detailsDialogOpen, setDetailsDialogOpen] = React.useState<boolean>(false);
  const [details, setDetails] = React.useState<HdesApi.AstTagSummary>();
  const [expanded, setExpanded] = React.useState<boolean>(false);
  const [releaseComposer, setReleaseComposer] = React.useState(false);
  const isLatest = release.id === 'latest';
  const isDefault = release.id === 'default';
  const backgroundColor = isLatest || isDefault ? { backgroundColor: '#F2F2F2' } : {};

  React.useEffect(() => {
    if (detailsDialogOpen) {
      service.summary(release.id).then(setDetails);
    }
  }, [detailsDialogOpen])

  const toggleExpand = () => {
    setExpanded(!expanded);
  }

  const onDownload = (data: string | undefined) => {
    if (data) {
      fileDownload(data, release.body.name + "_" + release.body.created + '.json');
    }
  }

  const handleCreateBranch = (releaseName: string, releaseId: string) => {
    const branchName = resolveNewBranchName(releaseName, branches);
    const command: HdesApi.AstCommand = {
      type: 'CREATE_BRANCH',
      value: branchName,
      id: releaseId
    }
    const key = enqueueSnackbar(<FormattedMessage id="release.branch.creating" values={{ name: branchName }} />, { persist: true });
    service.create().branch([command])
      .then((data) => {
        actions.handleBranchUpdate(branchName);
        actions.handleLoadSite(data);
        handleTabs(onNavReset);
        closeSnackbar(key);
        enqueueSnackbar(<FormattedMessage id="release.branch.created" values={{ name: branchName }} />);
      })
      .catch((error: HdesApi.StoreError) => {
        console.error(error)
      });
  }

  const handleCheckout = (branchName: string) => {
    service.getSite()
      .then(async (data) => {
        actions.handleBranchUpdate(branchName);
        await actions.handleLoadSite(data);
        handleTabs(onNavReset);
        enqueueSnackbar(<FormattedMessage id="release.branch.checkout" values={{ name: branchName }} />);
      })
      .catch((error: HdesApi.StoreError) => {
        console.error(error)
      });
  }

  const actionButton = () => {
    if (isLatest) {
      return <Button variant='contained'  onClick={() => setReleaseComposer(true)} ><FormattedMessage id='releases.button.release'/></Button>
    } else if (isDefault) {
      return <Button  onClick={() => handleCheckout("default")} variant='text'><FormattedMessage id='releases.button.checkout'/></Button>
    } else {
      return <Button sx={{ border: 1 }}  onClick={() => onDownload(release.body.data)} variant='text'><FormattedMessage id='buttons.download'/></Button>
    }
  }

  return (
    <>
      <DeleteDialog asset={assetToDelete} onClose={() => setAssetToDelete(undefined)} />
      {releaseComposer ? <ReleaseComposer onClose={() => setReleaseComposer(false)} /> : null}
      <TableRow key={release.id} sx={backgroundColor}>
        <TableCell align="center" sx={{ width: "10px" }}>{!isLatest && !isDefault && <IconButton onClick={toggleExpand}>{expanded ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}</IconButton>}</TableCell>
        <TableCell align="left"><Typography>{release.body.name}</Typography></TableCell>
        <TableCell align="center" sx={{ width: "10px" }}>{release.branches.length ? <ForkRightIcon /> : <></>}</TableCell>
        <TableCell align="left"><Burger.DateTimeFormatter timestamp={release.body.created} /></TableCell>
        <TableCell align="left">{release.body.note}</TableCell>
        <TableCell align="center">
          {actionButton()}
        </TableCell>
        <TableCell align="right">
          {!isLatest && !isDefault && <IconButton onClick={() => setAssetToDelete(release)} sx={{ color: 'error.main' }}><DeleteOutlineOutlinedIcon /> </IconButton>}
        </TableCell>
      </TableRow>
      {expanded &&
        <>
          {
            release.branches.length ? release.branches.map((branch) => {
              return (
                <StyledTableRow key={branch.id}>
                  <TableCell />
                  <TableCell align="left">{branch.branch.name}</TableCell>
                  <TableCell />
                  <TableCell align="left"><Burger.DateTimeFormatter timestamp={branch.branch.created} /></TableCell>
                  <TableCell align="left">Branch created from release: {release.body.name}</TableCell>
                  <TableCell align="center">
                    <Button  onClick={() => handleCheckout(branch.branch.name)} variant='text'><FormattedMessage id='releases.button.checkout'/></Button>
                  </TableCell>
                  <TableCell align="right">
                    <IconButton sx={{ color: 'error.main' }} onClick={() => setAssetToDelete(branch)}><DeleteOutlineOutlinedIcon /> </IconButton>
                  </TableCell>
                </StyledTableRow>
              )
            }) : <></>
          }
          <TableRow>
            <TableCell />
            <TableCell colSpan={5}>
              <Button variant='contained'  onClick={() => handleCreateBranch(release.body.name, release.id)} ><FormattedMessage id='releases.button.branch'/></Button>
              <Button  onClick={() => setDetailsDialogOpen(true)} variant='text'><FormattedMessage id='releases.button.details'/></Button>
              {detailsDialogOpen && (

              <Dialog open={detailsDialogOpen} onClose={() => setDetailsDialogOpen(false)}>
                <DialogTitle><FormattedMessage id='releases.details.title' values={{name: release.body.name }}/></DialogTitle>
                <DialogContent><AssetMapper assets={details} /></DialogContent>
                <DialogActions>
                  <Button variant='text' onClick={() => setDetailsDialogOpen(false)}>
                    <FormattedMessage id='button.cancel'/>
                  </Button>
                </DialogActions>
              </Dialog>
              )}
            </TableCell>
          </TableRow>
        </>
      }
    </>
  )
}

const ReleasesTable: React.FC<ReleasesTableProps> = ({ releases }) => {
  const [sort, setSort] = React.useState<SortOptions>('name');
  const [direction, setDirection] = React.useState<SortDirections>('desc');

  const sortByName = () => {
    setSort('name');
    setDirection(direction === 'asc' ? 'desc' : 'asc');
  };

  const sortByCreated = () => {
    setSort('created');
    setDirection(direction === 'asc' ? 'desc' : 'asc');
  };

  return (
    <TableContainer component={Paper}>
      <Table size="small">
        <TableHead>
          <TableRow sx={{ p: 1 }}>
            <TableCell sx={{ width: "10px" }} />
            <TableCell align="left" sx={{ fontWeight: 'bold' }}>
              <TableSortLabel active={sort === 'name'} direction={direction} onClick={sortByName}>
                <FormattedMessage id="releases.view.tag" />
              </TableSortLabel>
            </TableCell>
            <TableCell sx={{ width: "10px" }} />
            <TableCell align="left" sx={{ fontWeight: 'bold' }}>
              <TableSortLabel active={sort === 'created'} direction={direction} onClick={sortByCreated}>
                <FormattedMessage id="releases.view.created" />
              </TableSortLabel>
            </TableCell>
            <TableCell align="left" sx={{ fontWeight: 'bold' }}><FormattedMessage id="releases.view.note" /></TableCell>
            <TableCell align="center" sx={{ fontWeight: 'bold' }}><FormattedMessage id="releases.view.action" /></TableCell>
            <TableCell align="right" sx={{ width: "30px", fontWeight: 'bold' }}><FormattedMessage id="buttons.delete" /></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {useSort(releases, sort, direction).map((release, index) => (<RelRow key={index} release={release} />))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}

export default ReleasesTable;
