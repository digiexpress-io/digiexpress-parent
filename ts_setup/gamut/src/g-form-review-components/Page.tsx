import React, { useContext } from 'react';
import { Typography, Grid, styled } from '@mui/material';
import { GFormReviewContext } from './GFormReviewContext';


export interface PageItemProps {
  item: {
    label: Record<string, string>;
    items?: string[];
  };
}

const FormLabelTypography = styled(Typography)({

});

export const GFormReviewPage: React.FC<PageItemProps> = ({ item }) => {
  const dC = useContext(GFormReviewContext);;
  const items = item.items ? item.items.map(id => dC.createItem(id)) : null;
  const label = dC.getTranslated(item.label);
  return (
    <>
      <Grid container spacing={1}>
        <Grid item xs={12}>
          <FormLabelTypography variant='h2'>{label}</FormLabelTypography>
        </Grid>
        {items?.map((i, k) => <Grid key={k} item xs={12}>{i}</Grid>)}
      </Grid>

    </>
  );
}
