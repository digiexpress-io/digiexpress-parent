import React from 'react';
import { Link } from '@mui/material';
import { DashboardItem, useDialobForms } from '@dxs-ts/eveli-api';
import { FormattedMessage } from 'react-intl';


export const FormTableTitleRow: React.FC<{ value: DashboardItem }> = ({ value }) => {
  const { openForm } = useDialobForms();

  function handleOnClick(event: React.MouseEvent) {
    event.preventDefault()
    openForm(value);
  }

  return (
    <Link target='_blank' rel='noopener noreferrer' sx={{ ":hover": { cursor: 'pointer' } }}  onClick={handleOnClick}>
      {value.metadata.label || <FormattedMessage id='adminUI.dialog.emptyTitle' />}
    </Link>
  );
}