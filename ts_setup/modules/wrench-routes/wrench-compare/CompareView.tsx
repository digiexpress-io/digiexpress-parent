import React from "react";
import { Box, Button, ListItemText, Typography, Dialog, DialogTitle, DialogContent, DialogActions, ListItem, List, ListItemButton, ButtonGroup, Divider } from "@mui/material";
import { FormattedMessage, useIntl } from "react-intl";
import { ArrowBack as ArrowBackIcon } from '@mui/icons-material';

import * as Diff2Html from "diff2html";
import "diff2html/bundles/css/diff2html.min.css";
import { OutputFormatType } from "diff2html/lib/types";

import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';
import { CancelButton } from '@dxs-ts/eveli-primitives';



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
        <CancelButton onClick={() => setOpen(false)} />
      </DialogActions>
    </Dialog>
  );
}


const CompareView: React.FC = () => {
  const intl = useIntl();

  const { service, site } = Composer.useComposer();
  const [base, setBase] = React.useState<string>("");
  const [target, setTarget] = React.useState<string>("");
  const [baseSummary, setBaseSummary] = React.useState<HdesApi.AstTagSummary>();
  const [targetSummary, setTargetSummary] = React.useState<HdesApi.AstTagSummary>();
  const [disabled, setDisabled] = React.useState<boolean>(true);
  const [open, setOpen] = React.useState<boolean>(false);
  const [diff, setDiff] = React.useState<HdesApi.DiffResponse>();
  const [sortDescending, setSortDescending] = React.useState<boolean>(true);

  const releases = React.useMemo(() => {
    const tags = site?.tags ? Object.values(site.tags) : [];

    const getCreatedTime = (tag: HdesApi.Entity<HdesApi.AstTag>): number => {
      const created = tag.ast?.created;
      if (!created) {
        return 0;
      }
      const time = new Date(created).getTime();
      return Number.isNaN(time) ? 0 : time;
    };

    return tags
      .slice()
      .sort((a: HdesApi.Entity<HdesApi.AstTag>, b: HdesApi.Entity<HdesApi.AstTag>) => {
        const aTime = getCreatedTime(a);
        const bTime = getCreatedTime(b);

        if (aTime === bTime) {
          const aName = a.ast?.name || "";
          const bName = b.ast?.name || "";
          const nameCmp = aName.localeCompare(bName);
          return sortDescending ? -nameCmp : nameCmp;
        }

        const cmp = aTime - bTime;
        return sortDescending ? -cmp : cmp;
      });
  }, [site, sortDescending]);

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
            <Typography variant="body2">
              <FormattedMessage id={"activities.compare.desc"} />
            </Typography>
          </Typography>
        </Box>
        <Box flexGrow={1} />
        <Box
          alignSelf="center"
          sx={{ display: 'flex', gap: 2 }}
        >
          <Button
            variant="outlined"
            size="small"
            onClick={() => setSortDescending(!sortDescending)}
          >
            {sortDescending ? (
              <FormattedMessage id="activities.compare.sort.oldestFirst" />           
            ) : (
              <FormattedMessage id="activities.compare.sort.newestFirst" />
            )}
          </Button>

          <Button
            variant="contained"
            onClick={() => setOpen(true)}
            disabled={disabled}
          >
            <FormattedMessage id='activities.compare.view' />
          </Button>
        </Box>
      </Box>

      <Typography variant="body2" sx={{ mx: 1, mt: 2 }}>
        <FormattedMessage id={"compare.tip"} />
      </Typography>

      <Box sx={{ m: 1, display: 'flex' }}>
        <Box
          sx={{
            width: 0.4,
            display: 'flex',
            flexDirection: 'column',
            border: '1px solid',
            borderColor: 'divider',
            borderRadius: 1,
            p: 1
          }}
        >
          <Typography variant="subtitle1" fontWeight='bold' sx={{ mb: 1 }}>
            <FormattedMessage id="compare.base" />
          </Typography>

          <List dense>
            {releases.map((release: any) => (
              <ListItem
                key={release.id}
                disablePadding
              >
                <ListItemButton
                  selected={base === release.id}
                  onClick={() => setBase(release.id)}
                >
                  <ListItemText
                    primary={release.ast?.name}
                  />
                </ListItemButton>
              </ListItem>
            ))}
          </List>

          {base && (
            <>
              <Divider sx={{ my: 2 }} />
              <AssetMapper assets={baseSummary} />
            </>
          )}
        </Box>

        <ArrowBackIcon sx={{ m: 2, mt: 4 }} />

        <Box
          sx={{
            width: 0.4,
            display: 'flex',
            flexDirection: 'column',
            border: '1px solid',
            borderColor: 'divider',
            borderRadius: 1,
            p: 1
          }}
        >
          <Typography variant="subtitle1" fontWeight='bold' sx={{ mb: 1 }}>
            <FormattedMessage id="compare.target" />
          </Typography>

          <List dense>
            {releases.map((release: any) => (
              <ListItem
                key={release.id}
                disablePadding
              >
                <ListItemButton
                  selected={target === release.id}
                  onClick={() => setTarget(release.id)}
                >
                  <ListItemText
                    primary={release.ast?.name}
                  />
                </ListItemButton>
              </ListItem>
            ))}
          </List>

          {target && (
            <>
              <Divider sx={{ my: 2 }} />
              <AssetMapper assets={targetSummary} />
            </>
          )}
        </Box>
      </Box>
      <CompareDialog open={open} setOpen={setOpen} diff={diff} />
    </Box >
  );
}

export { CompareView, AssetMapper };
