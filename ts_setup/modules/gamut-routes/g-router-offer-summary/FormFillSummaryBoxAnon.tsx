import React from 'react';
import { Box, Button, List, ListItem, ListItemIcon, ListItemText, Typography } from "@mui/material";
import { PhoneEnabled as PhoneEnabledIcon, SupportAgent as SupportAgentIcon } from '@mui/icons-material';
import { FormFillNpsRating } from '../g-router-offer-summary-rps';

import { useIntl } from "react-intl";
import { SiteApi } from "@dxs-ts/gamut-api";
import { useUtilityClasses } from "./useUtilityClasses";


export const FormFillSummaryBoxAnon: React.FC<{
  topicLink: SiteApi.TopicLink | undefined,
  productId: string;
  buttonBackToMsg: string,
  onNav: () => void
}> = ({ topicLink, buttonBackToMsg, onNav, productId }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const [rating, setRating] = React.useState<number | undefined>(undefined);
  const [comment, setComment] = React.useState('');

  return (
    <Box className={classes.summaryLayout}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'gamut.forms.filling.summary.thank-you' })}</Typography>
      <div className={classes.spacer} />

      <Typography className={classes.subTitle}>
        {intl.formatMessage({ id: 'gamut.forms.filling.summary' })}
        {intl.formatMessage({ id: 'gamut.textSeparator', defaultMessage: ' ' })}
        {topicLink?.name ?? "-"}
      </Typography>
      <List disablePadding dense>
        <ListItem dense>
          <ListItemIcon><SupportAgentIcon className={classes.icon} /></ListItemIcon>
          <ListItemText>
            <Typography className={classes.bodyText}>{intl.formatMessage({ id: 'gamut.forms.filling.summary.info1' })}</Typography>
          </ListItemText>
        </ListItem>
        <ListItem>
          <ListItemIcon><PhoneEnabledIcon className={classes.icon} /></ListItemIcon>
          <ListItemText>
            <Typography className={classes.bodyText}>{intl.formatMessage({ id: 'gamut.forms.filling.summary.info6' })}</Typography>
          </ListItemText>
        </ListItem>
      </List>

      <FormFillNpsRating rating={rating} onRatingChange={setRating} comment={comment} onCommentChange={setComment} />

      <Box className={classes.button}>
        <Button variant='contained' onClick={() => {
          console.log({ rating, comment, productId });
          onNav();
        }}>
          {intl.formatMessage({ id: buttonBackToMsg })}
        </Button>
      </Box>
    </Box>
  )
}