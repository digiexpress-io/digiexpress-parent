import React from 'react';
import { useThemeProps, Table, TableContainer, TableCell, TableHead, TableRow, TableBody, TablePagination, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { SiteApi, useSite } from '../api-site';
import { GOverridableComponent } from '../g-override';
import { GFeedbackTableArticleReducer } from './GArticleFeedbackTableReducer';
import { useUtilityClasses, GArticleFeedbackRoot, MUI_NAME } from './useUtilityClasses';
import { GArticleFeedbackTableHead } from './GArticleFeedbackTableHead';
import { GArticleFeedbackVote } from './GArticleFeedbackVote';
import { GArticleFeedbackTableToolbar } from './GArticleFeedbackTableToolbar';


export interface GArticleFeedbackProps {
  children: SiteApi.TopicView | undefined;
  enabled?: (view: SiteApi.TopicView) => boolean;
  slots?: {

  };
  component?: GOverridableComponent<GArticleFeedbackProps>;
}

function isEnabled(view: SiteApi.TopicView) {
  return view.id === '000_index';
}

export const GArticleFeedback: React.FC<GArticleFeedbackProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })

  const { feedback } = useSite();
  const reducer = React.useState(new GFeedbackTableArticleReducer({
    data: [],
    order: 'asc',
    orderBy: 'labelValue',
    page: 0,
    rowsPerPage: undefined
  }));

  React.useEffect(() => {
    reducer[1](prev => prev.withData(feedback))
  }, [feedback]);


  const classes = useUtilityClasses(props);
  const slots = props.slots;
  const ownerState = {
    ...props,
    ...slots,
    enabled: props.enabled ?? isEnabled,
    reducer,
    noData: feedback.length === 0
  }
  const topic: SiteApi.TopicView | undefined = props.children;
  const Root = props.component ?? GArticleFeedbackRoot;

  function handleOnRowClick(feedback: SiteApi.Feedback) {

  }

  const handleChangePage = (_event: unknown, newPage: number) => {
    reducer[1](prev => prev.withPage(newPage));
  }

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    reducer[1](prev => prev.withRowsPerPage(event.target.value));
  }


  return (
    <Root ownerState={ownerState} className={classes.root}>
      <GArticleFeedbackTableToolbar className={classes.toolbar}/>
      <TableContainer>
        <Table size='small'>
          <TableHead>
            <TableRow>
              <GArticleFeedbackTableHead cellName='labelValue' ownerState={reducer}>
                <FormattedMessage id='gamut.feedback.table.topic'/>
              </GArticleFeedbackTableHead>

              <GArticleFeedbackTableHead cellName='subLabelValue' ownerState={reducer}>
                <FormattedMessage id='gamut.feedback.table.subtopic'/>
              </GArticleFeedbackTableHead>

              <TableCell align='right' padding='none'>
                <FormattedMessage id='gamut.feedback.table.xx1'/>
              </TableCell>

              <TableCell align='right' padding='none'>
                <FormattedMessage id='gamut.feedback.table.xx2'/>
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {reducer[0].visibleRows.map((row) => (
              <TableRow hover tabIndex={-1} key={row.id} onClick={(event) => handleOnRowClick(row)} className={classes.filledRow}>
                <TableCell component="th" scope="row" padding="none">{row.labelValue}</TableCell>
                <TableCell align="right">{row.subLabelValue}</TableCell>
                <TableCell align="right">{"xx1"}</TableCell>
                <TableCell align="right">{"xx2"}</TableCell>
                <TableCell align="right"><GArticleFeedbackVote feedback={row}/></TableCell>
              </TableRow>))
            }
            {reducer[0].emptyRows > 0 && (
              <TableRow className={classes.emptyRow}>
                <TableCell colSpan={5} className={ownerState.noData ? classes.noData : undefined}>
                  {ownerState.noData && <Typography><FormattedMessage id='gamut.feedback.table.nodata'/></Typography>}
                </TableCell>
              </TableRow>)}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination component='div'
        className={classes.pagination}
        rowsPerPageOptions={[5, 20, 40]}
        count={reducer[0].data.length}
        rowsPerPage={reducer[0].rowsPerPage}
        page={reducer[0].page}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage} />
    </Root>
  )
}
