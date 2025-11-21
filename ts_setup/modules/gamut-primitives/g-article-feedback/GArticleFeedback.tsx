import React from 'react';
import { useThemeProps, Table, TableContainer, TableCell, TableHead, TableRow, TableBody, TablePagination, Typography, useMediaQuery, Button, Popover, List, ListItem, ListItemButton, Box } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import { ThumbDown as ThumbDownIcon } from '@mui/icons-material';
import { ThumbUp as ThumbUpIcon } from '@mui/icons-material';
import TableSortLabel from '@mui/material/TableSortLabel';
import { FormattedMessage, useIntl } from 'react-intl';

import { SiteApi, useSite } from '@dxs-ts/gamut-api';
import { GOverridableComponent } from '@dxs-ts/gamut-api';
import { GFeedbackTableArticleReducer } from './GArticleFeedbackTableReducer';
import { useUtilityClasses, GArticleFeedbackRoot, MUI_NAME } from './useUtilityClasses';
import { GArticleFeedbackTableHead } from './GArticleFeedbackTableHead';
import { GArticleFeedbackTableToolbar } from './GArticleFeedbackTableToolbar';
import { GArticleFeedbackViewer } from '../g-article-feedback-viewer';

import { DateTime } from 'luxon';
import { useLocale } from '@dxs-ts/gamut-api';
import { GArticleFeedbackList } from './GArticleFeedbackList';
import { useAnchor } from '../g-locales/useAnchor';

export interface GArticleFeedbackProps {
  children: SiteApi.TopicView;
  enabled?: (view: SiteApi.TopicView) => boolean;
  slots?: {

  };
  component?: GOverridableComponent<GArticleFeedbackProps>;
}

function isEnabled(view: SiteApi.TopicView) {
  return true;//view.id === '000_index';
}

export const GArticleFeedback: React.FC<GArticleFeedbackProps> = (initProps) => {
  const [selectedFeedback, setSelectedFeedback] = React.useState<SiteApi.CustomerFeedback | undefined>();
  const { locale } = useLocale();
  const theme = useTheme();
  const intl = useIntl();
  const { anchorProps: sortAnchorProps, onClick: sortAnchorOnClick } = useAnchor();

  const isSmallScreen = useMediaQuery(theme.breakpoints.down('sm'));

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })

  const { feedback } = useSite();

  const reducer = React.useState(new GFeedbackTableArticleReducer({
    data: [],
    order: 'desc',
    orderBy: 'updatedOnDate',
    page: 0,
    rowsPerPage: undefined
  }));  

  React.useEffect(() => {
    reducer[1](prev => prev.withData(feedback))
  }, [feedback]);


  const classes = useUtilityClasses();
  const slots = props.slots;
  const ownerState = {
    ...props,
    ...slots,
    enabled: props.enabled ?? isEnabled,
    reducer,
    noData: feedback.length === 0,
    feedbackId: selectedFeedback?.feedback.id,
    isViewFeedback: selectedFeedback?.feedback.id ? true : false
  }

  const Root = props.component ?? GArticleFeedbackRoot;
  function handleOnRowClick(row: SiteApi.CustomerFeedback) {
    setSelectedFeedback(row);
  }

  const handleChangePage = (_event: unknown, newPage: number) => {
    reducer[1](prev => prev.withPage(newPage));
  }

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    reducer[1](prev => prev.withRowsPerPage(event.target.value));
  }

  const [state] = reducer;
  const visibleRows = state.visibleRows;

  const currentSortLabelId =
    state.orderBy === 'subLabelValue'
      ? 'gamut.feedback.table.topicTitle'
      : state.orderBy === 'labelValue'
      ? 'gamut.feedback.table.topic'
      : 'gamut.feedback.table.updatedOnDate';

  const currentSortLabel = intl.formatMessage({ id: currentSortLabelId });

  const handleMobileSortChange = (
    property: 'subLabelValue' | 'labelValue' | 'updatedOnDate',
  ) => {
    reducer[1]((prev) => {
      const userOrder =
        property === prev.orderBy
          ? (prev.order === 'asc' ? 'desc' : 'asc')
          : prev.order;
  
      return prev.withOrderBy(property, userOrder as any);
    });
  
    sortAnchorProps.onClose();
  };
  
  const handleMobileToggleOrder = () => {
    reducer[1]((prev) =>
      prev.withOrderBy(prev.orderBy as any),
    );
  };

  return (
    <>
      {ownerState.isViewFeedback && ownerState.feedbackId && (
        <GArticleFeedbackViewer
          feedbackId={ownerState.feedbackId}
          onClose={() => setSelectedFeedback(undefined)}
        />
      )}
      <Root ownerState={ownerState} className={classes.root}>
        <GArticleFeedbackTableToolbar className={classes.toolbar} />

        {isSmallScreen && (
          <>
            <Box className={classes.mobileSortBar}>
              <Button
                onClick={sortAnchorOnClick}
                variant="text"
                endIcon={<span className={classes.mobileSortChevron}>▼</span>}
              >
                {currentSortLabel}
              </Button>

              <Box className={classes.mobileSortArrow}>
                <TableSortLabel
                  active
                  direction={state.order}
                  onClick={handleMobileToggleOrder}
                />
              </Box>
            </Box>

            <Popover {...sortAnchorProps}>
              <List disablePadding>
                <ListItem disablePadding>
                  <ListItemButton
                    onClick={() => handleMobileSortChange('subLabelValue')}
                  >
                    <FormattedMessage id="gamut.feedback.table.topicTitle" />
                  </ListItemButton>
                </ListItem>
                <ListItem disablePadding>
                  <ListItemButton
                    onClick={() => handleMobileSortChange('labelValue')}
                  >
                    <FormattedMessage id="gamut.feedback.table.topic" />
                  </ListItemButton>
                </ListItem>
                <ListItem disablePadding>
                  <ListItemButton
                    onClick={() => handleMobileSortChange('updatedOnDate')}
                  >
                    <FormattedMessage id="gamut.feedback.table.updatedOnDate" />
                  </ListItemButton>
                </ListItem>
              </List>
            </Popover>
          </>
        )}

        {isSmallScreen ? (
          <GArticleFeedbackList
            items={visibleRows}
            locale={locale}
            onRowClick={handleOnRowClick}
          />
        ) : (
            <TableContainer>
              <Table size='small'>
                <TableHead>
                  <TableRow>
                    <GArticleFeedbackTableHead cellName='subLabelValue' ownerState={reducer}>
                      <FormattedMessage id='gamut.feedback.table.topicTitle' />
                    </GArticleFeedbackTableHead>

                    <GArticleFeedbackTableHead cellName='labelValue' ownerState={reducer}>
                      <FormattedMessage id='gamut.feedback.table.topic' />
                    </GArticleFeedbackTableHead>

                    <GArticleFeedbackTableHead cellName='updatedOnDate' ownerState={reducer}>
                      <FormattedMessage id='gamut.feedback.table.updatedOnDate' />
                    </GArticleFeedbackTableHead>

                    <TableCell></TableCell>
                  </TableRow>
                </TableHead>

                <TableBody>
                  {visibleRows.map((row) => (
                    <TableRow hover tabIndex={-1} key={row.feedback.id} onClick={(_event) => handleOnRowClick(row)} className={classes.filledRow}>
                      <TableCell className={classes.colWidth} component="th" scope="row" padding="none">{row.feedback.customerTitle ? row.feedback.customerTitle : "-"}</TableCell>
                      <TableCell className={classes.colWidth} component="th" scope="row" padding="none">{row.feedback.labelValue}</TableCell>
                      <TableCell component="th" scope="row" align="left" padding="none">
                        {DateTime.fromJSDate(new Date(row.feedback.updatedOnDate))
                          .setLocale(locale)
                          .toLocaleString(DateTime.DATE_SHORT)}
                      </TableCell>
                      <TableCell align="right">
                        <div className={classes.vote}>
                          <div className="vote-item">
                            <ThumbDownIcon fontSize='small' />
                            <Typography className="vote-count">{row.feedback.thumbsDownCount}</Typography>
                          </div>
                          <div className="vote-item">
                            <ThumbUpIcon fontSize='small' />
                            <Typography className="vote-count">{row.feedback.thumbsUpCount}</Typography>
                          </div>
                        </div>
                      </TableCell>
                    </TableRow>))
                }
                {state.emptyRows > 0 && (
                  <TableRow className={classes.emptyRow}>
                    <TableCell
                      colSpan={5}
                      className={ownerState.noData ? classes.noData : undefined}
                    >
                      {ownerState.noData && (
                        <Typography>
                          <FormattedMessage id="gamut.feedback.table.nodata" />
                        </Typography>
                      )}
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        )}

        <TablePagination
          component="div"
          className={classes.pagination}
          rowsPerPageOptions={[5, 20, 40]}
          count={state.data.length}
          rowsPerPage={state.rowsPerPage}
          page={state.page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage} />
      </Root>
    </>
  )
}
