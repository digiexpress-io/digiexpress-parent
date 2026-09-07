import { Box, Button, Divider, List, ListItem, ListItemIcon, ListItemText, Typography } from "@mui/material";
import { PhoneEnabled as PhoneEnabledIcon } from '@mui/icons-material';

import { useIntl } from "react-intl";
import { SiteApi } from "@dxs-ts/gamut-api";
import { useUtilityClasses } from "./useUtilityClasses";


export const FormFillSummaryBoxAnon: React.FC<{
  topicLink: SiteApi.TopicLink | undefined,
  buttonBackToMsg: string,
  onNav: () => void
}> = ({ topicLink, buttonBackToMsg, onNav }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <Box className={classes.summaryLayout}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'gamut.forms.filling.summary.thank-you' })}</Typography>

      <Typography className={classes.subTitle}>
        {intl.formatMessage({ id: 'gamut.forms.filling.summary' })}
        {intl.formatMessage({ id: 'gamut.textSeparator', defaultMessage: ' ' })}
        {topicLink?.name ?? "-"}
      </Typography>
      <Typography className={classes.bodyText}>{intl.formatMessage({ id: 'gamut.forms.filling.summary.info1' })}</Typography>

      <div className={classes.spacer} />
      <div className={classes.spacer} />
      <Divider className={classes.spacer} />
      <div className={classes.spacer} />
      <div className={classes.spacer} />

      <List disablePadding dense>
        <ListItem dense>
          <ListItemIcon><PhoneEnabledIcon className={classes.icon} /></ListItemIcon>
          <ListItemText>
            <Typography className={classes.bodyText}>{intl.formatMessage({ id: 'gamut.forms.filling.summary.info6' })}</Typography>
          </ListItemText>
        </ListItem>
      </List>

      <div className={classes.spacer} />

      <Box className={classes.button}>
        <Button variant='contained' onClick={onNav}>
          {intl.formatMessage({ id: buttonBackToMsg })}
        </Button>
      </Box>
    </Box>
  )
}