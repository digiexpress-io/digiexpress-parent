import React from 'react';
import { Divider, Link, List, Typography } from '@mui/material';

import {
  GLinkFormUnsecured,
  GLinkPhone,
  GLinkHyper,
} from '../';

import { GRouterSecuredServicesResultsDividerRoot, GRouterSecuredServicesSearchResultsRoot, OwnerState } from './useUtilityClasses';
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

      <ResultsDivider ownerState={ownerState} title='Forms' />

      {search.forms.map((form) => (
        <List dense>
          <GLinkFormUnsecured key={form.linkToForm.id} label={form.label}
            value={form.linkToForm.value}
            onClick={() => onForm(form)} />
        </List>
      )
      )}

      <ResultsDivider ownerState={ownerState} title='Services' />

      {search.topics.map((topic) => (
        <List dense>
          <Link component='span' key={topic.id} onClick={() => onTopic(topic)}>{topic.name}</Link>
        </List>
      ))}

      <ResultsDivider ownerState={ownerState} title='Phone numbers' />

      {search.phones.map((phone) => (
        <List dense>
          <GLinkPhone key={phone.id} label={phone.name} value={phone.value} />
        </List>
      ))}

      <ResultsDivider ownerState={ownerState} title='Links to other pages' />

      {...search.internal.map((link) => (
        <List dense>
          <GLinkHyper label={link.name} value={link.value} key={link.id} />
        </List>
      ))}

      <ResultsDivider ownerState={ownerState} title='Links to our pages' />

      {...search.external.map((link) => (
        <List dense>
          <GLinkHyper label={link.name} value={link.value} key={link.id} />
        </List>))}
    </GRouterSecuredServicesSearchResultsRoot>
  );
}
