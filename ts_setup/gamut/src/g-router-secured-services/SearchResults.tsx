import React from 'react';
import { Divider, Link, List, ListItem, Typography } from '@mui/material';

import {
  GLinkFormUnsecured,
  GLinkPhone,
  GLinkHyper,
} from '../';

import { GRouterSecuredServicesSearchResultsRoot, OwnerState } from './useUtilityClasses';
import { useUtilityClasses } from './useUtilityClasses';
import { useIntl } from 'react-intl';


const ResultsDivider: React.FC<{ ownerState: OwnerState, title: string }> = ({ ownerState, title }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  if (ownerState.search.searchOptionType === 'ALL') {
    return (
      <>
        <Divider className={classes.resultsDivider} />
        <Typography className={classes.resultsDividerTitle}>{intl.formatMessage({ id: title })}</Typography>
      </ >
    )
  }
  return <></>
}

export const SearchResults: React.FC<{ ownerState: OwnerState }> = ({ ownerState }) => {
  const { search, onTopic, onForm } = ownerState;
  const classes = useUtilityClasses();

  return (
    <GRouterSecuredServicesSearchResultsRoot className={classes.searchResults}>

      <ResultsDivider ownerState={ownerState} title='gamut.search.results.serviceLinks' />

      <List dense>
        {search.topics.map((topic) => (
          <ListItem key={topic.id}>
            <Link onClick={() => onTopic(topic)}>{topic.name}</Link>
          </ListItem>
        ))}
      </List>

      <ResultsDivider ownerState={ownerState} title='gamut.search.results.formLinks' />
      <List dense>
        {search.forms.map((form) => (
          <ListItem key={form.linkToForm.id}>
            <GLinkFormUnsecured key={form.linkToForm.id} label={form.label}
              value={form.linkToForm.value}
              onClick={() => onForm(form)} />
          </ListItem>
        )
        )}
      </List>

      <ResultsDivider ownerState={ownerState} title='gamut.search.results.phoneLinks' />

      <List dense>
        {search.phones.map((phone) => (
          <ListItem key={phone.id}>
            <GLinkPhone label={phone.name} value={phone.value} />
          </ListItem>
        ))}
      </List>

      <ResultsDivider ownerState={ownerState} title='gamut.search.results.internalLinks' />

      <List dense>
        {...search.internal.map((link) => (
          <ListItem key={link.name}>
            <GLinkHyper label={link.name} value={link.value} key={link.id} />
          </ListItem>
        ))}
      </List>

      <ResultsDivider ownerState={ownerState} title='gamut.search.results.externalLinks' />

      <List dense>
        {...search.external.map((link) => (
          <ListItem key={link.name}>
            <GLinkHyper label={link.name} value={link.value} key={link.id} />
          </ListItem>
        ))}
      </List>

    </GRouterSecuredServicesSearchResultsRoot>
  );
}
