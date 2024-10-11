import React from 'react';

import {
  Box, Typography, IconButton,
  TableCell, TableRow, Card, alpha, styled,
} from '@mui/material';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import ForkRightIcon from '@mui/icons-material/ForkRight';

import { FormattedMessage, useIntl } from 'react-intl';
import fileDownload from 'js-file-download'
import { useSnackbar } from 'notistack';

import { ReleaseComposer } from './ReleaseComposer';
import { ErrorView } from '../styles';
import ReleasesTable from './ReleasesTable';
import type { Release } from './release-types';
import { ReleaseBranch } from './release-types';
import { Composer } from '../context';
import * as Burger from '@/burger';
import { BurgerApi } from '@/burger';

import { AssetMapper } from '../compare/CompareView';
import { HdesApi } from '../client';

const StyledTableRow = styled(TableRow)(({ theme }) => ({
  backgroundColor: alpha(theme.palette.explorer.main, .05),
}));

const resolveNewBranchName = (releaseName: string, branches: HdesApi.AstBranch[]): string => {
  const matches: HdesApi.AstBranch[] = branches.filter((branch) => branch.name.includes(releaseName));
  if (matches.length === 0) {
    return releaseName + "_dev";
  }
  const branchNo: number = matches.length + 1
  return releaseName + "_dev_" + branchNo;
}

const handleTabs = (actions: BurgerApi.TabsActions) => {
  actions.handleTabCloseAll();
  actions.handleTabAdd({ id: 'activities', label: "Activities" });
  actions.handleTabAdd({ id: 'releases', label: "Releases" });
}

const ReleasesView: React.FC<{}> = () => {

  const { site } = Composer.useComposer();
  const layout = Burger.useTabs();
  const releases = Object.values(site.tags);
  const branches = Object.values(site.branches);

  const formattedReleases: Release[] = releases.map((release) => {
    const { id } = release;
    const name = release.ast?.name || '';
    const created = release.ast?.created || '';
    const note = release.ast?.description;
    const data = JSON.stringify(release.ast, null, 2);
    const releaseBranches: ReleaseBranch[] = branches.filter((branch) => branch.ast?.tagId === id).map((branch) => {
      return {
        id: branch.id,
        branch: branch.ast!,
      }
    });
    return {
      id,
      body: {
        name,
        note,
        created,
        data,
      },
      branches: releaseBranches
    }
  });

  return (
    <Box sx={{ paddingBottom: 1, m: 2 }}>
      <Box display="flex">
        <Box alignSelf="center">
          <Typography variant="h3" sx={{ p: 1, mb: 3, fontWeight: "bold", color: "mainContent.dark" }}>
            <FormattedMessage id="activities.releases.title" />: {releases.length}
            <Typography variant="body2" sx={{ pt: 1 }}><FormattedMessage id={"activities.releases.desc"} /></Typography>
            <Typography variant="body2" sx={{ pt: 1 }}><FormattedMessage id={"activities.releases.desc.additional"} /></Typography>
          </Typography>
        </Box>
        <Box flexGrow={1} />
        <Box>
          <Burger.SecondaryButton label={"button.cancel"} onClick={() => layout.actions.handleTabCloseCurrent()} sx={{ marginRight: 1 }} />
          <Burger.SecondaryButton label={"activities.releases.graph"} onClick={() => layout.actions.handleTabAdd({ id: 'graph', label: "Release Graph" })} sx={{ marginRight: 1 }} />
          <Burger.SecondaryButton label={"releases.button.compare"} onClick={() => layout.actions.handleTabAdd({ id: 'compare', label: "Compare" })} />
        </Box>
      </Box>

      <Box display="flex" sx={{ justifyContent: 'center' }}>

        <Card sx={{ margin: 1, width: 'fill-available' }}>
          <Typography variant="h4" sx={{ p: 2, backgroundColor: "table.main" }}>
            <FormattedMessage id="releases.table.title" />
          </Typography>
          <ReleasesTable releases={formattedReleases} tableRowComponent={Row} />
        </Card>
      </Box>
    </Box>
  );
}

const DeleteDialog: React.FC<{ asset?: ReleaseBranch | Release, onClose: () => void, }> = ({ asset, onClose }) => {
  const { service, actions } = Composer.useComposer();
  const tabs = Burger.useTabs();
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


  return (<Burger.Dialog open={true}
    onClose={onClose}
    children={editor}
    backgroundColor="uiElements.main"
    title={prefix + '.delete.title'}
    submit={{
      title: "buttons.delete",
      disabled: apply,
      onClick: () => {
        setErrors(undefined);
        setApply(true);

        if (isBranch) {
          const key = enqueueSnackbar(<FormattedMessage id="release.branch.deleting" values={{ name }} />, { persist: true });
          service.withBranch("default").delete().branch(id)
            .then((data) => {
              actions.handleBranchUpdate("default");
              actions.handleLoadSite(data);
              handleTabs(tabs.actions);
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
    }}
  />);
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
          .catch((error: HdesApi.StoreError) => {
            setErrors(error);
          });
      }
    }}
  />);
}

const Row: React.FC<{ release: Release }> = ({ release }) => {
  const { service, actions, site } = Composer.useComposer();
  const { enqueueSnackbar, closeSnackbar } = useSnackbar();
  const intl = useIntl();
  const tabs = Burger.useTabs();
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
    service.withBranch(branchName).create().branch([command])
      .then((data) => {
        actions.handleBranchUpdate(branchName);
        actions.handleLoadSite(data);
        handleTabs(tabs.actions);
        closeSnackbar(key);
        enqueueSnackbar(<FormattedMessage id="release.branch.created" values={{ name: branchName }} />);
      })
      .catch((error: HdesApi.StoreError) => {
        console.error(error)
      });
  }

  const handleCheckout = (branchName: string) => {
    service.withBranch(branchName).getSite()
      .then((data) => {
        actions.handleBranchUpdate(branchName);
        actions.handleLoadSite(data);
        handleTabs(tabs.actions);
        enqueueSnackbar(<FormattedMessage id="release.branch.checkout" values={{ name: branchName }} />);
      })
      .catch((error: HdesApi.StoreError) => {
        console.error(error)
      });
  }

  const actionButton = () => {
    if (isLatest) {
      return <Burger.PrimaryButton label={'releases.button.release'} onClick={() => setReleaseComposer(true)} />
    } else if (isDefault) {
      return <Burger.SecondaryButton label={'releases.button.checkout'} onClick={() => handleCheckout("default")} />
    } else {
      return <Burger.SecondaryButton sx={{ border: 1 }} label={'buttons.download'} onClick={() => onDownload(release.body.data)} />
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
                    <Burger.SecondaryButton label={'releases.button.checkout'} onClick={() => handleCheckout(branch.branch.name)} />
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
              <Burger.PrimaryButton label={'releases.button.branch'} onClick={() => handleCreateBranch(release.body.name, release.id)} />
              <Burger.SecondaryButton label={'releases.button.details'} onClick={() => setDetailsDialogOpen(true)} />
              {detailsDialogOpen &&
                <Burger.Dialog
                  onClose={() => setDetailsDialogOpen(false)}
                  backgroundColor='uiElements.main'
                  title={intl.formatMessage({ id: 'releases.details.title' }, { name: release.body.name })}
                  open={detailsDialogOpen}
                >
                  <AssetMapper assets={details} />
                </Burger.Dialog>}
            </TableCell>
          </TableRow>
        </>
      }
    </>
  )
}

export { ReleasesView }
