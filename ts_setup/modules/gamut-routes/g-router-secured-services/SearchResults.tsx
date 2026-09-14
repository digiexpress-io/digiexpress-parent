import React from 'react';
import { Alert, Box, CircularProgress, Divider, Link, List, ListItem, Typography } from '@mui/material';
import { Circle as CircleIcon } from '@mui/icons-material';

import { GLinkPhone, GLinkHyper, GLinkFormUnlockedSearchResults, GLinkFormLocked } from '@dxs-ts/gamut-primitives';
import  { useIam, SearchApi, SiteApi, useSite  } from '@dxs-ts/gamut-api';

import { OwnerState, useUtilityClasses } from './useUtilityClasses';
import { useIntl } from 'react-intl';


interface ResultsDividerProps {
  searchState: SearchApi.SearchState,
  title: string,
  isHidden: boolean,
  className?: string
}


const ResultsDivider: React.FC<ResultsDividerProps> = ({ title, isHidden }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  if (isHidden) {
    return <></>;
  }

  return (
    <>
      <Divider className={classes.resultsDivider} />
      <Typography className={classes.resultsDividerTitle}>{intl.formatMessage({ id: title })}</Typography>
    </>
  )

}

export const SearchResults: React.FC<{ ownerState: OwnerState }> = ({ ownerState }) => {
  const { onTopic, onForm } = ownerState;
  const { value: search } = SearchApi.useSearch();
  const iam = useIam();
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { views } = useSite();

  const backend = SearchApi.useBackendSearch(search.searchString);
  const semantic = SearchApi.useSemanticResults(views, search, backend);

  const topics = semantic?.topics ?? search.topics;
  const forms = semantic?.forms ?? search.forms;
  const phones = backend.loading ? [] : search.phones;
  const internal = backend.loading ? [] : search.internal;
  const external = backend.loading ? [] : search.external;

  const noResults = !backend.loading &&
    topics.length === 0 &&
    forms.length === 0 &&
    phones.length === 0 &&
    internal.length === 0 &&
    external.length === 0;

  const childTopicIds = new Set(
    (Object.values(views) as SiteApi.TopicView[])
      .flatMap(topic => topic.children ?? [])
      .map(child => child.id)
  );

  return (
    <div className={classes.searchResults} aria-busy={backend.loading}>
      {backend.showLoader ? (
        <Box className={classes.resultsLoading}>
          <CircularProgress size={32} aria-label={intl.formatMessage({ id: 'gamut.loading' })} />
        </Box>
      ) : noResults ? (
        <Alert severity='info' variant='outlined'>
          {intl.formatMessage({ id: 'gamut.search.results.noResults' })}
          {intl.formatMessage({ id: 'gamut.noValueIndicatorColon' })} {search.searchString}
        </Alert>
      ) : (
        <>
          <ResultsDivider searchState={search} title='gamut.search.results.serviceLinks' isHidden={topics.length === 0} />
            <List dense>
              {topics.map((topic) => {
                const isChild = childTopicIds.has(topic.id);

                return (
                  <ListItem
                    key={topic.id}
                    className={isChild ? classes.childTopic : undefined}
                  >
                    {isChild && <CircleIcon fontSize="small" />}
                    <Link onClick={() => onTopic(topic)}>{topic.name}</Link>
                  </ListItem>
                );
              })}
            </List>

            <ResultsDivider searchState={search} title='gamut.search.results.formLinks' className={classes.resultsDividerTitle} isHidden={forms.length === 0} />
            {forms.map((form) => (
              <ListItem dense key={form.linkToForm.id}>

              { iam.isFormLinkEnabled(form.linkToForm) ?
              (<GLinkFormUnlockedSearchResults
                key={form.linkToForm.id}
                label={form.linkToForm.name}
                value={form.linkToForm.value}
                onClick={() => { onForm(form) }}
              />) : (
                <GLinkFormLocked
                  key={form.linkToForm.id}
                  label={form.linkToForm.name}
                  value={form.linkToForm.value}
                  onClick={() => { onForm(form) }}
                />)
              }
            </ListItem>
          ))}

            <ResultsDivider searchState={search} title='gamut.search.results.phoneLinks' isHidden={phones.length === 0} />
            <List dense>
              {phones.map((phone) => (
                <ListItem key={phone.id}>
                  <GLinkPhone label={phone.name} value={phone.value} />
                </ListItem>
              ))}
            </List>

            <ResultsDivider searchState={search} title='gamut.search.results.internalLinks' isHidden={internal.length === 0} />
            <List dense>
              {...internal.map((link) => (
                <ListItem key={link.name}>
                  <GLinkHyper label={link.name} value={link.value} key={link.id} />
                </ListItem>
              ))}
            </List>

            <ResultsDivider searchState={search} title='gamut.search.results.externalLinks' isHidden={external.length === 0} />
            <List dense>
              {...external.map((link) => (
                <ListItem key={link.name}>
                  <GLinkHyper label={link.name} value={link.value} key={link.id} />
                </ListItem>
              ))}
            </List>
        </>
      )}
    </div>
  );
}
