import React from 'react';

import Box from '@mui/material/Box';
import TableCell from '@mui/material/TableCell';

import TableSortLabel from '@mui/material/TableSortLabel';
import { visuallyHidden } from '@mui/utils';
import { useIntl } from "react-intl";

import { GFeedbackTableArticleReducer } from './GArticleFeedbackTableReducer';
import { SiteApi } from '../api-site';


export type GArticleFeedbackTableHeadProps = [GFeedbackTableArticleReducer, React.Dispatch<React.SetStateAction<GFeedbackTableArticleReducer>>];


export const GArticleFeedbackTableHead: React.FC<{
  ownerState: GArticleFeedbackTableHeadProps;
  cellName: keyof SiteApi.Feedback;
  children: React.ReactNode;

}> = ({ ownerState, cellName, children }) => {

  const [state, setState] = ownerState;
  const intl = useIntl();
  const order = state.order;
  const orderBy = state.orderBy;
  const active = orderBy === cellName;
  const direction = active ? order : 'asc';
  const tooltip = intl.formatMessage({ id: order === 'desc' ?'gamut.feedback.table.sorted.desc' : 'gamut.feedback.table.sorted.asc'});

  function handleSorting() {
    setState(prev => prev.withOrderBy(cellName, direction))
  }

  return (
  <TableCell align='left' padding='none' sortDirection={active ? order : false}>
    <TableSortLabel active={active} direction={direction} onClick={handleSorting}>
      {children}
      { active ? (<Box component="span" sx={visuallyHidden}>{tooltip}</Box>) : null }
    </TableSortLabel>
  </TableCell>
  );
}