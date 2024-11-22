import React, { useContext } from 'react';
import { Grid } from '@mui/material';
import { RowGroupContext } from './GFormReviewRowGroupContext';


interface ItemProps {
  label: string;
  children: JSX.Element;
}

export const GFormReviewItem: React.FC<ItemProps> = ({ label, children }) => {
  const inRowGroup = useContext(RowGroupContext);
  if (!inRowGroup) {
    return (
      <Grid data-type='item-grid' container spacing={2}>
        {
          /*
           * TODO::: admir error:         <Grid data-type='item-label' item xs={3} fontWeight={PortalStyles.SipooTheme.typography.subtitle1.fontWeight}>{label}</Grid>
           */
        }
        <Grid data-type='item-label' item xs={3}><strong>{label}</strong></Grid>
        <Grid data-type='item-value' item xs={9}>{children}</Grid>
      </Grid>
    );
  } else {
    return (<>
      {children}
    </>);
  }

}