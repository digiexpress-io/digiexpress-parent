import React from "react";
import { Box, Button, ListItemText, Typography, Dialog, DialogTitle, DialogContent, DialogActions, ListItem, List, ButtonGroup } from "@mui/material";
import { FormattedMessage, useIntl } from "react-intl";
import ArrowBackIcon from '@mui/icons-material/ArrowBack';

import * as Diff2Html from "diff2html";
import "diff2html/bundles/css/diff2html.min.css";
import { OutputFormatType } from "diff2html/lib/types";

import { WrenchComposerApi as Composer } from '../wrench-setup';
import { HdesApi } from '@/api-wrench';
import * as Burger from '@/eveli-styles';
import { useWrenchNav } from "../wrench-nav";



interface CompareDialogProps {
  open: boolean;
  setOpen: (open: boolean) => void;
  diff?: HdesApi.DiffResponse;
}

const AssetMapper: React.FC<{ assets?: HdesApi.AstTagSummary }> = ({ assets }) => {
  if (!assets) {
    return null;
  }
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', m: 2 }}>
      {assets.flows.length ? <>
        <Typography variant='h5' fontWeight='bold'><FormattedMessage id='flows' /></Typography>
        <List>
          {assets.flows.map((flow) => (
            <ListItem>
              <Typography>{flow.name}</Typography>
            </ListItem>
          ))}
        </List>
      </> : <></>}
      {assets.decisions.length ? <>
        <Typography variant='h5' fontWeight='bold' sx={{ mt: 1 }}><FormattedMessage id='decisions' /></Typography>
        <List>
          {assets.decisions.map((decision) => (
            <ListItem>
              <Typography>{decision.name}</Typography>
            </ListItem>
          ))}
        </List>
      </> : <></>}
      {assets.services.length ? <>
        <Typography variant='h5' fontWeight='bold' sx={{ mt: 1 }}><FormattedMessage id='services' /></Typography>
        <List>
          {assets.services.map((service) => (
            <ListItem>
              <Typography>{service.name}</Typography>
            </ListItem>
          ))}
        </List>
      </> : <></>}
    </Box>
  );
}

const ReleaseSelect: React.FC<{ release: string, setRelease: (release: string) => void, label: string }> = ({ release, setRelease, label }) => {
  const { site } = Composer.useComposer();
  const releases = Object.values(site.tags);

  return (
    <Burger.Select label={label}
      selected={release}
      onChange={setRelease}
      items={releases.map((release) => ({
        id: release.id,
        value: <ListItemText primary={release.ast?.name} />
      }))}
      empty={{
        id: "",
        label
      }}
    />
  );
}

const CompareDialog: React.FC<CompareDialogProps> = ({ open, setOpen, diff }) => {
  const [outputFormat, setOutputFormat] = React.useState<OutputFormatType>('line-by-line');

  const diffJson = Diff2Html.parse(diff?.body || "");
  const diffHtml = Diff2Html.html(diffJson, { outputFormat });

  return (
    <Dialog open={open} onClose={() => setOpen(false)} maxWidth='xl'>
      <DialogTitle>
        <Typography variant="h3" sx={{ p: 1, fontWeight: "bold", color: "text.primary" }}>
          <FormattedMessage id="compare.dialog.title" values={{ base: diff?.baseName, target: diff?.targetName }} />
        </Typography>
      </DialogTitle>
      <DialogContent sx={{ mx: 1, overflowY: 'unset' }}>
        <Box sx={{ minWidth: '80vw', display: 'flex', flexDirection: 'column' }}>
          <ButtonGroup variant="text" sx={{ alignSelf: 'flex-end' }}>
            <Button  onClick={() => setOutputFormat('line-by-line')}  variant='text'><FormattedMessage id='compare.dialog.line'/></Button>
            <Button  onClick={() => setOutputFormat('side-by-side')}  variant='text'><FormattedMessage id='compare.dialog.side'/></Button>
          </ButtonGroup>
          <div dangerouslySetInnerHTML={{ __html: diffHtml }} />
        </Box>
      </DialogContent>
      <DialogActions>
        <Button  onClick={() => setOpen(false)}  variant='text'><FormattedMessage id='button.cancel'/></Button>
      </DialogActions>
    </Dialog>
  );
}


const CompareView: React.FC = () => {
  const intl = useIntl();

  const { service } = Composer.useComposer();
  const [base, setBase] = React.useState<string>("");
  const [target, setTarget] = React.useState<string>("");
  const [baseSummary, setBaseSummary] = React.useState<HdesApi.AstTagSummary>();
  const [targetSummary, setTargetSummary] = React.useState<HdesApi.AstTagSummary>();
  const [disabled, setDisabled] = React.useState<boolean>(true);
  const [open, setOpen] = React.useState<boolean>(false);
  const [diff, setDiff] = React.useState<HdesApi.DiffResponse>();
  const { onTabCurrentClose } = useWrenchNav();

  React.useEffect(() => {
    if (base) {
      service.summary(base).then(setBaseSummary);
    }
  }, [base]);

  React.useEffect(() => {
    if (target) {
      service.summary(target).then(setTargetSummary);
    }
  }, [target]);

  React.useEffect(() => {
    if (base && target) {
      setDisabled(false);
      service.diff({ baseId: base, targetId: target }).then(setDiff);
    } else {
      setDisabled(true);
    }
  }, [base, target]);

  return (
    <Box sx={{ paddingBottom: 1 }}>
      <Box display="flex">
        <Box alignSelf="center">
          <Typography variant="h1" sx={{ p: 1 }}>
            <FormattedMessage id="activities.compare.title" />
            <Typography variant="body2"><FormattedMessage id={"activities.compare.desc"} /></Typography>
          </Typography>
        </Box>
        <Box flexGrow={1} />
        <Box alignSelf="center">
          <Button  onClick={() => onTabCurrentClose()} sx={{ marginRight: 1 }} variant='text'><FormattedMessage id='button.cancel'/></Button>
          <Button variant='contained'  onClick={() => setOpen(true)} disabled={disabled} ><FormattedMessage id='activities.compare.view'/></Button>
        </Box>
      </Box>
      <Typography variant="body2" sx={{ mx: 1, mt: 2 }}><FormattedMessage id={"compare.tip"} /></Typography>
      <Box sx={{ m: 1, display: 'flex' }}>
        <Box sx={{ width: 0.3, alignItems: 'center' }}>
          <ReleaseSelect release={base} setRelease={setBase} label="compare.base" />
          {base && <AssetMapper assets={baseSummary} />}
        </Box>
        <ArrowBackIcon sx={{ m: 2, mt: 4 }} />
        <Box sx={{ width: 0.3 }}>
          <ReleaseSelect release={target} setRelease={setTarget} label="compare.target" />
          {target && <AssetMapper assets={targetSummary} />}
        </Box>
      </Box>
      <CompareDialog open={open} setOpen={setOpen} diff={diff} />
    </Box >
  );
}

export { CompareView, AssetMapper };
