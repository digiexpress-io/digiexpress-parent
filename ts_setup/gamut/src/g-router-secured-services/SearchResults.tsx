import React from 'react';
import { Alert, Divider, Link, List, ListItem, Typography } from '@mui/material';

import {
  GLinkFormUnlocked,
  GLinkPhone,
  GLinkHyper,
} from '../';

import { GRouterSecuredServicesSearchResultsRoot, OwnerState } from './useUtilityClasses';
import { useUtilityClasses } from './useUtilityClasses';
import { useIntl } from 'react-intl';

interface ResultsDividerProps {
  ownerState: OwnerState,
  title: string,
  isHidden: boolean
}


const ResultsDivider: React.FC<ResultsDividerProps> = ({ ownerState, title, isHidden }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  if (isHidden) {
    return <></>;
  }


  if (ownerState.search.searchOptionType === 'ALL') {
    return (
      <>
        <Divider className={classes.resultsDivider} />
        <Typography className={classes.resultsDividerTitle}>{intl.formatMessage({ id: title })}</Typography>
      </>
    )
  }
  return <></>
}

export const SearchResults: React.FC<{ ownerState: OwnerState }> = ({ ownerState }) => {
  const { search, onTopic, onForm } = ownerState;
  const intl = useIntl();
  const classes = useUtilityClasses();

  const noResults = search.topics.length === 0 &&
    search.forms.length === 0 &&
    search.phones.length === 0 &&
    search.internal.length === 0 &&
    search.external.length === 0;

  return (
    <GRouterSecuredServicesSearchResultsRoot className={classes.searchResults}>
      {noResults ? (
        <Alert severity='info' variant='outlined'>
          {intl.formatMessage({ id: 'gamut.search.results.noResults' })}
          {intl.formatMessage({ id: 'gamut.noValueIndicatorColon' })} {search.searchString}
        </Alert>
      ) : (
        <>
          <ResultsDivider ownerState={ownerState} title='gamut.search.results.serviceLinks' isHidden={ownerState.search.topics.length === 0} />
          <List dense>
            {search.topics.map((topic) => (
              <ListItem key={topic.id}>
                <Link onClick={() => onTopic(topic)}>{topic.name}</Link>
              </ListItem>
            ))}
          </List>

            <ResultsDivider ownerState={ownerState} title='gamut.search.results.formLinks' isHidden={ownerState.search.forms.length === 0} />
            <List dense>
              {search.forms.map((form) => (
                <ListItem key={form.linkToForm.id}>
                  <GLinkFormUnlocked key={form.linkToForm.id} label={form.label}
                    value={form.linkToForm.value}
                    onClick={() => onForm(form)} />
                </ListItem>
              )
              )}
            </List>

            <ResultsDivider ownerState={ownerState} title='gamut.search.results.phoneLinks' isHidden={ownerState.search.phones.length === 0} />
            <List dense>
              {search.phones.map((phone) => (
                <ListItem key={phone.id}>
                  <GLinkPhone label={phone.name} value={phone.value} />
                </ListItem>
              ))}
            </List>

            <ResultsDivider ownerState={ownerState} title='gamut.search.results.internalLinks' isHidden={ownerState.search.internal.length === 0} />
            <List dense>
              {...search.internal.map((link) => (
                <ListItem key={link.name}>
                  <GLinkHyper label={link.name} value={link.value} key={link.id} />
                </ListItem>
              ))}
            </List>

            <ResultsDivider ownerState={ownerState} title='gamut.search.results.externalLinks' isHidden={ownerState.search.external.length === 0} />
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
