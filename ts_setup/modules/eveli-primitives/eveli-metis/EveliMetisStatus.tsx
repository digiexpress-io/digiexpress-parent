import React from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  Chip,
  LinearProgress,
  Tooltip,
  Typography
} from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
import { useFetch } from '@dxs-ts/envir-fetch';
import { IamApi, mapIamRole, MetisApi, PublicationApi, useIam } from '@dxs-ts/eveli-api';
import { EveliAlert } from '../eveli-alert';
import { EveliDateTimeFormatter } from '../eveli-datetime-formatter';
import { EveliSpinner } from '../eveli-spinner';
import { ConfirmDialog } from '../eveli-styles';
import { EveliMetisStatusRoot, useUtilityClasses } from './useUtilityClasses';


const POLL_MS = 3000;

type ConfirmKind = 'rebuild' | 'replace';

function isActiveJob(state: MetisApi.JobState | undefined): boolean {
  return state === 'RUNNING' || state === 'CANCELLING';
}

function isAssetAdmin(roles: string[]): boolean {
  return roles.some(role => mapIamRole(role) === 'ASSET_ADMIN');
}

function canManageIndex(user: IamApi.User): boolean {
  return isAssetAdmin(user.roles);
}

function publicationLabel(publicationId: string | undefined, publications: PublicationApi.Publication[] | undefined): { value?: string; title?: string } {
  if (!publicationId) {
    return {};
  }
  const match = publications?.find(publication => publication.id === publicationId);
  if (!match?.name) {
    return { value: publicationId, title: publicationId };
  }
  return { value: match.name, title: publicationId };
}

function chipColor(state: string): 'success' | 'info' | 'warning' | 'error' | 'default' {
  switch (state) {
    case 'READY':
    case 'COMPLETED':
      return 'success';
    case 'RUNNING':
      return 'info';
    case 'CANCELLING':
    case 'NOT_READY':
      return 'warning';
    case 'ERROR':
    case 'FAILED':
      return 'error';
    default:
      return 'default';
  }
}

function showKeywordFallback(state: MetisApi.JobState | undefined): boolean {
  return !!state && state !== 'COMPLETED' && state !== 'NONE';
}

export const EveliMetisStatus: React.FC = () => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const { user } = useIam();
  const { getMetisStatus } = useFetch('worker/rest/api/metis/status.GET', {});
  const { getMetisSearchStatus } = useFetch('worker/rest/api/metis/search/status.GET', {});
  const { startMetisReindex } = useFetch('worker/rest/api/metis/search/reindex.POST', {});
  const { cancelMetisReindex } = useFetch('worker/rest/api/metis/search/reindex/cancel.POST', {});
  const { assetReleases } = useFetch('worker/rest/api/assets/publications.GET', {});

  const [loading, setLoading] = React.useState(true);
  const [busy, setBusy] = React.useState(false);
  const [loadError, setLoadError] = React.useState<string>();
  const [platform, setPlatform] = React.useState<MetisApi.Status>();
  const [search, setSearch] = React.useState<MetisApi.SearchIndexStatus>();
  const [searchEnabled, setSearchEnabled] = React.useState(true);
  const [confirm, setConfirm] = React.useState<ConfirmKind>();

  const canManage = canManageIndex(user);
  const jobState = search?.state;
  const running = jobState === 'RUNNING';
  const cancelling = jobState === 'CANCELLING';

  React.useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const nextPlatform = await getMetisStatus();
        if (cancelled) {
          return;
        }
        setPlatform(nextPlatform);
        if (!nextPlatform) {
          setSearch(undefined);
          setSearchEnabled(false);
          setLoadError(undefined);
          return;
        }
        const nextSearch = await getMetisSearchStatus();
        if (cancelled) {
          return;
        }
        setSearch(nextSearch);
        setSearchEnabled(!!nextSearch);
        setLoadError(undefined);
      } catch (error) {
        if (!cancelled) {
          setLoadError(error instanceof Error ? error.message : String(error));
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  React.useEffect(() => {
    if (!isActiveJob(jobState)) {
      return;
    }
    const timer = window.setInterval(() => {
      getMetisSearchStatus()
        .then(nextSearch => {
          setSearch(nextSearch);
          setSearchEnabled(!!nextSearch);
        })
        .catch(() => undefined);
    }, POLL_MS);
    return () => window.clearInterval(timer);
  }, [jobState]);

  async function applySearchResult(result: MetisApi.SearchIndexStatus | undefined) {
    if (!result) {
      setSearchEnabled(false);
      setSearch(undefined);
      return;
    }
    setSearchEnabled(true);
    setSearch(result);
  }

  async function startReindex(command: MetisApi.ReindexCommand) {
    setBusy(true);
    try {
      const result = await startMetisReindex(command);
      await applySearchResult(result);
      setConfirm(undefined);
    } catch (error) {
      setLoadError(error instanceof Error ? error.message : String(error));
    } finally {
      setBusy(false);
    }
  }

  function handleReindex() {
    if (running) {
      setConfirm('replace');
      return;
    }
    startReindex({ force: false, replace: false });
  }

  function handleConfirm() {
    if (confirm === 'rebuild') {
      startReindex({ force: true, replace: running });
      return;
    }
    if (confirm === 'replace') {
      startReindex({ force: false, replace: true });
    }
  }

  async function handleStop() {
    setBusy(true);
    try {
      const result = await cancelMetisReindex();
      await applySearchResult(result);
    } catch (error) {
      setLoadError(error instanceof Error ? error.message : String(error));
    } finally {
      setBusy(false);
    }
  }

  if (loading) {
    return <EveliSpinner />;
  }

  return (
    <EveliMetisStatusRoot className={classes.root}>
      <Box display="flex" alignItems="center" className={classes.heading}>
        <Typography variant="h1" sx={{ flexGrow: 1 }}>
          <FormattedMessage id="eveli.metis.title" />
        </Typography>
      </Box>

      {loadError && (
        <Alert severity="error">{loadError}</Alert>
      )}

      {!platform && (
        <EveliAlert
          title={intl.formatMessage({ id: 'eveli.metis.disabled.title' })}
          body={intl.formatMessage({ id: 'eveli.metis.disabled.body' })}
        />
      )}

      {platform && (
        <Card className={classes.card} variant="outlined">
          <Typography variant="h4" gutterBottom>
            <FormattedMessage id="eveli.metis.platform.title" />
          </Typography>
          <StatusRow labelId="eveli.metis.provider" value={platform.provider} />
          <StatusRow labelId="eveli.metis.chatModel" value={platform.chatModel} />
          <StatusRow labelId="eveli.metis.embeddingModel" value={platform.embeddingModel} />
          <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>
            <FormattedMessage id="eveli.metis.capabilities" />
          </Typography>
          {platform.capabilities.map(capability => (
            <Box key={capability.id} className={classes.row}>
              <Typography variant="body2" color="text.secondary">
                <FormattedMessage
                  id={`eveli.metis.capability.${capability.id}`}
                  defaultMessage={capability.id}
                />
              </Typography>
              <Box>
                <Chip
                  size="small"
                  color={chipColor(capability.state)}
                  label={intl.formatMessage({
                    id: `eveli.metis.capability.state.${capability.state}`,
                    defaultMessage: capability.state
                  })}
                />
                {capability.detail && (
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                    {capability.detail}
                  </Typography>
                )}
              </Box>
            </Box>
          ))}
        </Card>
      )}

      {platform && !searchEnabled && (
        <EveliAlert
          title={intl.formatMessage({ id: 'eveli.metis.search.disabled.title' })}
          body={intl.formatMessage({ id: 'eveli.metis.search.disabled.body' })}
        />
      )}

      {platform && searchEnabled && search && (
        <Card className={classes.card} variant="outlined">
          <Typography variant="h4" gutterBottom>
            <FormattedMessage id="eveli.metis.search.title" />
          </Typography>

          <Box className={classes.row}>
            <Typography variant="body2" color="text.secondary">
              <FormattedMessage id="eveli.metis.search.jobState" />
            </Typography>
            <Chip
              size="small"
              color={chipColor(search.state)}
              label={intl.formatMessage({
                id: `eveli.metis.search.state.${search.state}`,
                defaultMessage: search.state
              })}
            />
          </Box>

          {isActiveJob(search.state) && (
            <Box className={classes.progress}>
              <LinearProgress
                variant={search.totalCount ? 'determinate' : 'indeterminate'}
                value={search.totalCount
                  ? Math.min(100, (((search.processedCount ?? 0) + (search.skippedCount ?? 0)) / search.totalCount) * 100)
                  : undefined}
              />
            </Box>
          )}

          <StatusRow
            labelId="eveli.metis.search.documents"
            value={search.totalCount == null
              ? String((search.processedCount ?? 0) + (search.skippedCount ?? 0))
              : `${(search.processedCount ?? 0) + (search.skippedCount ?? 0)} / ${search.totalCount}`}
          />
          <StatusRow labelId="eveli.metis.search.skipped" value={String(search.skippedCount ?? 0)} />
          <StatusRow labelId="eveli.metis.search.indexed" value={String(search.indexedDocuments ?? 0)} />
          <StatusRow labelId="eveli.metis.embeddingModel" value={search.embeddingModel} />
          <StatusRow
            labelId="eveli.metis.search.publicationId"
            {...publicationLabel(search.publicationId, assetReleases)}
          />
          <StatusRow labelId="eveli.metis.search.startedAt" value={search.startedAt} datetime />
          <StatusRow labelId="eveli.metis.search.finishedAt" value={search.finishedAt} datetime />

          {search.error && (
            <Alert severity="error" sx={{ mt: 1, mb: 1 }}>{search.error}</Alert>
          )}

          {search.accepted === false && (
            <Alert severity="warning" sx={{ mt: 1, mb: 1 }}>
              <FormattedMessage id="eveli.metis.search.conflict" />
            </Alert>
          )}

          {showKeywordFallback(search.state) && (
            <Alert severity="info" sx={{ mt: 1, mb: 1 }}>
              <FormattedMessage id="eveli.metis.search.fallback" />
            </Alert>
          )}

          {canManage && (
            <Box className={classes.actions}>
              <Button
                variant="contained"
                disabled={busy || cancelling}
                onClick={handleReindex}
              >
                <FormattedMessage id="eveli.metis.search.reindex" />
              </Button>
              <Button
                variant="outlined"
                disabled={busy || cancelling}
                onClick={() => setConfirm('rebuild')}
              >
                <FormattedMessage id="eveli.metis.search.rebuild" />
              </Button>
              {running && (
                <Button
                  color="warning"
                  variant="outlined"
                  disabled={busy}
                  onClick={handleStop}
                >
                  <FormattedMessage id="eveli.metis.search.stop" />
                </Button>
              )}
            </Box>
          )}
        </Card>
      )}

      {confirm && (
        <ConfirmDialog
          open
          title={intl.formatMessage({
            id: confirm === 'rebuild'
              ? 'eveli.metis.search.rebuild.confirm.title'
              : 'eveli.metis.search.replace.confirm.title'
          })}
          message={intl.formatMessage({
            id: confirm === 'rebuild'
              ? 'eveli.metis.search.rebuild.confirm.message'
              : 'eveli.metis.search.replace.confirm.message'
          })}
          confirmLabel={intl.formatMessage({
            id: confirm === 'rebuild' ? 'eveli.metis.search.rebuild' : 'eveli.metis.search.reindex'
          })}
          onCancel={() => { if (!busy) setConfirm(undefined); }}
          onConfirm={() => { if (!busy) handleConfirm(); }}
        />
      )}
    </EveliMetisStatusRoot>
  );
};


const StatusRow: React.FC<{
  labelId: string;
  value?: string;
  datetime?: boolean;
  title?: string;
}> = ({ labelId, value, datetime, title }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const display = value || intl.formatMessage({ id: 'eveli.noValueIndicator' });
  const content = datetime && value
    ? <EveliDateTimeFormatter value={value} />
    : <Typography variant="body2" component="span">{display}</Typography>;
  return (
    <Box className={classes.row}>
      <Typography variant="body2" color="text.secondary">
        <FormattedMessage id={labelId} />
      </Typography>
      {title && value
        ? <Tooltip title={title}>{content}</Tooltip>
        : content}
    </Box>
  );
};
