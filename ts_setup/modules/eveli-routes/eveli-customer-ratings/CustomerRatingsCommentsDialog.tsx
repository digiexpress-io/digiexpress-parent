import React from 'react';
import { useIntl } from 'react-intl';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, IconButton, ListItem, ListItemIcon, ListItemText, Typography } from '@mui/material';
import { ArrowDownward as ArrowDownwardIcon, ArrowUpward as ArrowUpwardIcon } from '@mui/icons-material';
import { DateTime } from 'luxon';

import { EveliCustomerRatingsDialogFilter, EveliCustomerRatingsDialogRoot, useUtilityClasses, getRpsColumnProps, RatingKey } from './useUtilityClasses';
import { RpsComment, RATING_ICONS } from './types';


const EMPTY_SELECTION = new Set<number>();

const RATING_LABEL_IDS: Record<number, string> = {
  5: 'rps.rating.excellent',
  4: 'rps.rating.good',
  3: 'rps.rating.okay',
  2: 'rps.rating.poor',
  1: 'rps.rating.terrible',
};

const CommentListItem: React.FC<{ comment: RpsComment }> = ({ comment }) => {
  const classes = useUtilityClasses();
  const RatingIcon = RATING_ICONS[comment.rating];
  return (
    <ListItem disableGutters divider className={classes.dialogListItem}>
      <ListItemIcon className={classes.dialogListItemIcon} {...getRpsColumnProps(`count${comment.rating}` as RatingKey)}>
        <RatingIcon />
      </ListItemIcon>
      <ListItemText
        primary={comment.text}
        secondary={DateTime.fromISO(comment.createdAt).setZone('Europe/Helsinki').setLocale('fi').toLocaleString(DateTime.DATETIME_SHORT)}
        secondaryTypographyProps={{ variant: 'caption' }}
      />
    </ListItem>
  );
};

const RatingsFilter: React.FC<{
  selected: Set<number>;
  available: Set<number>;
  onToggle: (rating: number) => void;
}> = ({ selected, available, onToggle }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  return (
    <>
      {([5, 4, 3, 2, 1] as const).map(rating => {
        const Icon = RATING_ICONS[rating];
        const isSelected = selected.has(rating);
        const isAvailable = available.has(rating);
        return (
          <Box key={rating} className={classes.dialogRatingsFilterItem}>
            <IconButton
              size='small'
              disabled={!isAvailable}
              className={`${classes.dialogRatingsFilterButton}${isSelected ? ` ${classes.dialogRatingsFilterButtonSelected}` : ''}`}
              {...getRpsColumnProps(`count${rating}` as RatingKey)}
              onClick={() => onToggle(rating)}
            >
              <Icon />
            </IconButton>
            <Typography variant='caption'>{intl.formatMessage({ id: RATING_LABEL_IDS[rating] })}</Typography>
          </Box>
        );
      })}
    </>
  );
};

export const CustomerRatingsCommentsDialog: React.FC<{
  data: { workflowName: string; formName: string; comments: RpsComment[] } | undefined;
  onClose: () => void;
}> = ({ data, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const [selectedRatings, setSelectedRatings] = React.useState<Set<number>>(EMPTY_SELECTION);
  const [sortDir, setSortDir] = React.useState<'desc' | 'asc'>('desc');

  React.useEffect(() => {
    if (data === undefined) {
      setSelectedRatings(EMPTY_SELECTION);
      setSortDir('desc');
    }
  }, [data]);

  function toggleRating(rating: number): void {
    setSelectedRatings(prev => {
      const next = new Set(prev);
      if (next.has(rating)) {
        next.delete(rating);
      } else {
        next.add(rating);
      }
      return next;
    });
  }

  function toggleSort(): void {
    setSortDir(prev => prev === 'desc' ? 'asc' : 'desc');
  }

  const availableRatings = new Set(data?.comments.map(comment => comment.rating) ?? []);
  const filteredComments = selectedRatings.size === 0
    ? (data?.comments ?? [])
    : (data?.comments.filter(comment => selectedRatings.has(comment.rating)) ?? []);
  const visibleComments = [...filteredComments].sort((a, b) => {
    const diff = new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
    return sortDir === 'desc' ? diff : -diff;
  });

  return (
    <Dialog open={data !== undefined} onClose={onClose} maxWidth='lg' fullWidth slotProps={{ paper: { sx: { height: '90vh' } } }}>
      <DialogTitle>
        {data?.workflowName}: {data?.formName}
        <EveliCustomerRatingsDialogFilter>
          <RatingsFilter selected={selectedRatings} available={availableRatings} onToggle={toggleRating} />
          <Button variant='text' size='small' className={classes.dialogSortButton}
            startIcon={sortDir === 'desc' ? <ArrowDownwardIcon /> : <ArrowUpwardIcon />}
            onClick={toggleSort}
          >
            {intl.formatMessage({ id: sortDir === 'desc' ? 'rps.sort.newest' : 'rps.sort.oldest' })}
          </Button>
        </EveliCustomerRatingsDialogFilter>
      </DialogTitle>
      <DialogContent>
        <EveliCustomerRatingsDialogRoot>
          {visibleComments.map((comment, index) => (
            <CommentListItem key={index} comment={comment} />
          ))}
        </EveliCustomerRatingsDialogRoot>
      </DialogContent>
      <DialogActions>
        <Button variant='contained' onClick={onClose}>{intl.formatMessage({ id: 'button.close' })}</Button>
      </DialogActions>
    </Dialog>
  );
};
