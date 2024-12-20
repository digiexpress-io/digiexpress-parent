import { Box, Button, Divider, generateUtilityClass, List, ListItem, ListItemIcon, ListItemText, styled, Typography } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import UpdateIcon from '@mui/icons-material/Update';
import MailOutlineIcon from '@mui/icons-material/MailOutline';
import FilePresentIcon from '@mui/icons-material/FilePresent';
import PhoneEnabledIcon from '@mui/icons-material/PhoneEnabled';

import { useIntl } from "react-intl";
import { SiteApi } from "api-site";

export const MUI_NAME = 'GRouterOfferSummary';

export interface GRouterOfferSummaryClasses {
  root: string,
  summaryLayout: string,
  button: string,
  spacer: string,
  title: string,
  subTitle: string,
  bodyText: string,
  icon: string
}
export type GRouterOfferSummaryClassKey = keyof GRouterOfferSummaryClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    summaryLayout: ['summaryLayout'],
    button: ['button'],
    spacer: ['spacer'],
    title: ['title'],
    subTitle: ['subTitle'],
    bodyText: ['bodyText'],
    icon: ['icon']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GRouterOfferSummaryRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.summaryLayout,
      styles.button,
      styles.spacer,
      styles.title,
      styles.subTitle,
      styles.bodyText,
      styles.icon
    ];
  },
})(({ theme }) => {
  return {
    justifyContent: 'center',

    '.GRouterOfferSummary-summaryLayout': {
      [theme.breakpoints.up('md')]: {
        padding: theme.spacing(5),
        marginTop: theme.spacing(4),
        width: '55%',
      },
      [theme.breakpoints.down('md')]: {
        padding: theme.spacing(5),
        margin: theme.spacing(1),
        width: '85%'
      },
      display: 'flex',
      flexDirection: 'column',

      justifyItems: 'center',
      justifySelf: 'center',
      border: `1px solid ${theme.palette.divider}`,
      backgroundColor: theme.palette.background.default
    },
    '.GRouterOfferSummary-button': {
      marginTop: theme.spacing(1),
      justifySelf: 'center',
    },
    '.GRouterOfferSummary-spacer': {
      marginTop: theme.spacing(0.5),
      marginBottom: theme.spacing(0.5)
    },
    '.GRouterOfferSummary-title': {
      ...theme.typography.h1,
      marginBottom: theme.spacing(1),
      textAlign: 'center',
    },
    '.GRouterOfferSummary-subTitle': {
      ...theme.typography.h2,
      marginBottom: theme.spacing(1)
    },
    '.GRouterOfferSummary-bodyText': {
      ...theme.typography.body1,
    },
    '.GRouterOfferSummary-icon': {
      color: theme.palette.primary.main
    },

  }
});


export const SummaryBox: React.FC<{ topicLink: SiteApi.TopicLink | undefined, buttonBackToMsg: string, onNav: () => void }> = ({ topicLink, buttonBackToMsg, onNav }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <Box className={classes.summaryLayout}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'gamut.forms.filling.summary.thank-you' })}</Typography>

      <Typography className={classes.subTitle}>{intl.formatMessage({ id: 'gamut.forms.filling.summary' })}{intl.formatMessage({ id: 'gamut.textSeparator' })}{topicLink?.name ?? "-"}</Typography>
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

      <Button className={classes.button} variant='contained' onClick={onNav}>{intl.formatMessage({ id: buttonBackToMsg })}</Button>
    </Box>
  )
}



