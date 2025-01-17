import React from 'react';
import { Link } from '@mui/material';

import {
  GLinkFormUnsecured,
  GLinkPhone,
  GLinkHyper,
} from '../';

import { OwnerState } from './useUtilityClasses';

export const SearchResults: React.FC<{ ownerState: OwnerState }> = ({ ownerState }) => {
  const { search, onTopic, onForm } = ownerState;

  return (<>
    {search.forms.map((form) => <GLinkFormUnsecured key={form.linkToForm.id} label={form.label}
      value={form.linkToForm.value}
      onClick={() => onForm(form)} />
    )}
    {search.phones.map((phone) => <GLinkPhone key={phone.id} label={phone.name} value={phone.value} />)}
    {search.topics.map((topic) => <Link key={topic.id} onClick={() => onTopic(topic)}>{topic.name}</Link>)}
    {...search.internal.map((link) => <GLinkHyper label={link.name} value={link.value} key={link.id} />)}
    {...search.external.map((link) => <GLinkHyper label={link.name} value={link.value} key={link.id} />)}
  </>
  );
}
