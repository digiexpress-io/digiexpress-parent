import React from 'react';
import { Alert, Divider, Link, List, ListItem, Typography } from '@mui/material';

import {
  GLinkPhone,
  GLinkHyper,
  GLinkFormUnlockedGrouped,
} from '../';

import { GRouterSecuredServicesSearchResultsRoot, OwnerState } from './useUtilityClasses';
import { useUtilityClasses } from './useUtilityClasses';
import { useIntl } from 'react-intl';
import { SearchApi } from '../api-search';

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
  const intl = useIntl();
  const classes = useUtilityClasses();

  const noResults = search.topics.length === 0 &&
    search.forms.length === 0 &&
    search.phones.length === 0 &&
    search.internal.length === 0 &&
    search.external.length === 0;


  const groupedForms = Object.values(search.groupedForms)


  return (
    <GRouterSecuredServicesSearchResultsRoot className={classes.searchResults}>
      {noResults ? (
        <Alert severity='info' variant='outlined'>
          {intl.formatMessage({ id: 'gamut.search.results.noResults' })}
          {intl.formatMessage({ id: 'gamut.noValueIndicatorColon' })} {search.searchString}
        </Alert>
      ) : (
        <>
            <ResultsDivider searchState={search} title='gamut.search.results.serviceLinks' isHidden={search.topics.length === 0} />
          <List dense>
            {search.topics.map((topic) => (
              <ListItem key={topic.id}>
                <Link onClick={() => onTopic(topic)}>{topic.name}</Link>
              </ListItem>
            ))}
          </List>


            {groupedForms.map((group, index) => (
              <div key={index}>
                {group.length > 1 && (
                  <>
                    <ResultsDivider searchState={search} title={group[0].linkToForm.name} className={classes.resultsDividerTitle} isHidden={Object.values(search.groupedForms).length === 0} />
                    {group.map((form) => (
                      <ListItem dense key={form.topic.id}>
                        <GLinkFormUnlockedGrouped key={form.linkToForm.id} label={form.topic.name} value={form.linkToForm.name}
                          onClick={() => { onForm(form) }}
                        />
                      </ListItem>
                    ))}
                  </>
                )}
              </div>
            ))}

            <ResultsDivider searchState={search} title={'gamut.search.results.otherForms'} className={classes.resultsDividerTitle} isHidden={search.forms.length === 0} />
            {groupedForms.map((group, index) => (
              <div key={index}>
                {group.length === 1 && (
                  <>
                    {group.map((form) => (
                      <ListItem dense>
                        <GLinkFormUnlockedGrouped
                          key={form.linkToForm.id}
                          label={form.linkToForm.name}
                          value={form.linkToForm.name}
                          onClick={() => { onForm(form) }}
                        />
                      </ListItem>
                    ))}
                  </>
              )}
              </div>
            ))}

            <ResultsDivider searchState={search} title='gamut.search.results.phoneLinks' isHidden={search.phones.length === 0} />
            <List dense>
              {search.phones.map((phone) => (
                <ListItem key={phone.id}>
                  <GLinkPhone label={phone.name} value={phone.value} />
                </ListItem>
              ))}
            </List>

            <ResultsDivider searchState={search} title='gamut.search.results.internalLinks' isHidden={search.internal.length === 0} />
            <List dense>
              {...search.internal.map((link) => (
                <ListItem key={link.name}>
                  <GLinkHyper label={link.name} value={link.value} key={link.id} />
                </ListItem>
              ))}
            </List>

            <ResultsDivider searchState={search} title='gamut.search.results.externalLinks' isHidden={search.external.length === 0} />
            <List dense>
              {...search.external.map((link) => (
                <ListItem key={link.name}>
                  <GLinkHyper label={link.name} value={link.value} key={link.id} />
                </ListItem>
              ))}
            </List>
        </>
      )}
    </GRouterSecuredServicesSearchResultsRoot>
  );
}
