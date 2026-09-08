import { Box, Button, Divider, List, ListItem, ListItemIcon, ListItemText, Typography } from "@mui/material";
import { Update as UpdateIcon } from '@mui/icons-material';
import { MailOutline as MailOutlineIcon } from '@mui/icons-material';
import { FilePresent as FilePresentIcon } from '@mui/icons-material';
import { PhoneEnabled as PhoneEnabledIcon } from '@mui/icons-material';

import { useIntl } from "react-intl";
import { SiteApi } from "@dxs-ts/gamut-api";
import { useUtilityClasses } from "./useUtilityClasses";
import { FormFillNpsRating } from '../g-router-offer-summary-rps';


export const FormFillSummaryBox: React.FC<{
  topicLink: SiteApi.TopicLink | undefined,
  buttonBackToMsg: string,
  onNav: () => void,
  rating: number | undefined,
  onRatingChange: (value: number) => void,
  comment: string,
  onCommentChange: (value: string) => void,
}> = ({ topicLink, buttonBackToMsg, onNav, rating, onRatingChange, comment, onCommentChange }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <Box className={classes.summaryLayout}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'gamut.forms.filling.summary.thank-you' })}</Typography>

      <Typography className={classes.subTitle}>{intl.formatMessage({ id: 'gamut.forms.filling.summary' })}{intl.formatMessage({ id: 'gamut.textSeparator', defaultMessage: ' ' })}{topicLink?.name ?? "-"}</Typography>
      <Typography className={classes.bodyText}>{intl.formatMessage({ id: 'gamut.forms.filling.summary.info1' })}</Typography>

      <div className={classes.spacer} />
      <div className={classes.spacer} />
      <Divider className={classes.spacer} />
      <div className={classes.spacer} />
      <div className={classes.spacer} />

      <Typography className={classes.subTitle}>{intl.formatMessage({ id: 'gamut.forms.filling.summary.info2' })}</Typography>
      <List disablePadding dense>
        <ListItem dense>
          <ListItemIcon><UpdateIcon className={classes.icon} /></ListItemIcon>
          <ListItemText>
            <Typography className={classes.bodyText}>{intl.formatMessage({ id: 'gamut.forms.filling.summary.info3' })}</Typography>
          </ListItemText>
        </ListItem>

        <ListItem>
          <ListItemIcon><MailOutlineIcon className={classes.icon} /></ListItemIcon>
          <ListItemText>
            <Typography className={classes.bodyText}>{intl.formatMessage({ id: 'gamut.forms.filling.summary.info4' })}</Typography>
          </ListItemText>
        </ListItem>

        <ListItem>
          <ListItemIcon><FilePresentIcon className={classes.icon} /></ListItemIcon>
          <ListItemText>
            <Typography className={classes.bodyText}>{intl.formatMessage({ id: 'gamut.forms.filling.summary.info5' })}</Typography>
          </ListItemText>
        </ListItem>

        <ListItem>
          <ListItemIcon><PhoneEnabledIcon className={classes.icon} /></ListItemIcon>
          <ListItemText>
            <Typography className={classes.bodyText}>{intl.formatMessage({ id: 'gamut.forms.filling.summary.info6' })}</Typography>
          </ListItemText>
        </ListItem>
      </List>

      <div className={classes.spacer} />
      {(topicLink?.rps &&
        <FormFillNpsRating rating={rating} onRatingChange={onRatingChange} comment={comment} onCommentChange={onCommentChange} />
      )}
      <Box className={classes.button}>
        <Button variant='contained' onClick={onNav}>
          {intl.formatMessage({ id: buttonBackToMsg })}
        </Button>
      </Box>
    </Box>
  )
}
