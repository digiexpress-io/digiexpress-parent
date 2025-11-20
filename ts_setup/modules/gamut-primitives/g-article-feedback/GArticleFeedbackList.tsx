import React from 'react';
import { Box, Divider, List, ListItemButton, Typography } from '@mui/material';
import { ThumbDown as ThumbDownIcon } from '@mui/icons-material';
import { ThumbUp as ThumbUpIcon } from '@mui/icons-material';
import { DateTime } from 'luxon';

import { SiteApi } from '@dxs-ts/gamut-api';
import { useUtilityClasses } from './useUtilityClasses';

export interface GArticleFeedbackListProps {
  items: SiteApi.CustomerFeedback[];
  locale: string;
  onRowClick: (row: SiteApi.CustomerFeedback) => void;
}

export const GArticleFeedbackList: React.FC<GArticleFeedbackListProps> = ({
  items,
  locale,
  onRowClick,

}) => {
  const classes = useUtilityClasses();

  if (!items.length) {
    return null;
  }

  return (
    <List className={classes.mobileList}>
      {items.map((row) => (
        <React.Fragment key={row.feedback.id}>
          <ListItemButton
            alignItems="flex-start"
            onClick={() => onRowClick(row)}
            className={classes.mobileListItem}
          >
            <Box sx={{ flexGrow: 1 }}>
              <Typography
                component="div"
                className={classes.mobileListHeader}
              >
                {row.feedback.customerTitle || '-'}
              </Typography>

              <Typography variant="body2" color="text.secondary">
                {row.feedback.labelValue}
              </Typography>

              <Box className={classes.mobileListMeta}>
                <Typography variant="caption">
                  {DateTime.fromJSDate(new Date(row.feedback.updatedOnDate))
                    .setLocale(locale)
                    .toLocaleString(DateTime.DATE_SHORT)}
                </Typography>

                <Box className={classes.vote}>
                  <div className="vote-item">
                    <ThumbDownIcon fontSize="small" />
                    <Typography className="vote-count">
                      {row.feedback.thumbsDownCount}
                    </Typography>
                  </div>
                  <div className="vote-item">
                    <ThumbUpIcon fontSize="small" />
                    <Typography className="vote-count">
                      {row.feedback.thumbsUpCount}
                    </Typography>
                  </div>
                </Box>
              </Box>
            </Box>
          </ListItemButton>
          <Divider component="li" />
        </React.Fragment>
      ))}
    </List>
  );
};
