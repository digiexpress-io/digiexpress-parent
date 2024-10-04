import React from 'react';
import { useThemeProps, TextField, Typography, Chip, Grid } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { FormattedMessage, useIntl } from 'react-intl';

import { SiteApi, useSite } from '../api-site';
import { useAnchor } from './useAnchor';
import { GPopoverButton } from '../';
import { useUtilityClasses, GPopoverSearchRoot, GSearchMuiPopover, MUI_NAME } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';



export interface GPopoverSearchProps {
  itemsInColumn?: number | undefined;
  onTopic: (topic: SiteApi.TopicView, event: React.MouseEvent) => void;
  slots?: { link?: React.ElementType<GSearchResultProps> }
  component?: GOverridableComponent<GPopoverSearchProps>
}

export interface GSearchResultProps {
  children: SiteApi.TopicView
  onClick?: (
      topic: SiteApi.TopicView, 
      event: React.MouseEvent<HTMLAnchorElement, MouseEvent> | React.MouseEvent<HTMLSpanElement, MouseEvent>) => void;
}


export const GPopoverSearch: React.FC<GPopoverSearchProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const intl = useIntl();
  const anchor = useAnchor();

  const classes = useUtilityClasses(props);
  const { views } = useSite();
  const [formNames, setFormNames] = React.useState<string[] | undefined>();

  function findFormNames() {
    const formNames = Object.values(views).flatMap(topicView => topicView.links.filter((l) => l.type === 'workflow').map((l) => l.name));
    const uniqueFormNames = [...new Set(formNames)];
    setFormNames(uniqueFormNames)
    return uniqueFormNames;
  }

  const Root = props.component ?? GPopoverSearchRoot;

  return (
    <Root className={classes.root} ownerState={props}>
      <GPopoverButton onClick={anchor.onClick} 
        label={<FormattedMessage id='gamut.buttons.search' />}
        icon={<SearchIcon />}
      />
      <GSearchMuiPopover {...anchor.anchorProps} open={anchor.anchorProps.open}>
        <Grid container spacing={1} sx={{ alignItems: 'center' }}>
          <Grid item lg={3}>
            <Typography className={classes.title}>{intl.formatMessage({ id: 'gamut.search.popover.title' })}</Typography>
          </Grid>

          <Grid item lg={9}>
            <TextField className={classes.inputField} placeholder={intl.formatMessage({ id: 'gamut.search.popover.input.placeholder' })} />
          </Grid>

          <Grid item lg={3} />

          <Grid item lg={9}>
            <Chip label={intl.formatMessage({ id: 'gamut.search.popover.allForms' })} onClick={() => findFormNames()} sx={{ mx: 0.5 }} />
            <Chip label={intl.formatMessage({ id: 'gamut.search.popover.allPhones' })} onClick={() => { }} sx={{ mx: 0.5 }} />
            <Chip label={intl.formatMessage({ id: 'gamut.search.popover.allServices' })} onClick={() => { }} sx={{ mx: 0.5 }} />
            <Chip label={intl.formatMessage({ id: 'gamut.search.popover.allLinks' })} onClick={() => { }} sx={{ mx: 0.5 }} />
          </Grid>
        </Grid>

        <Grid container>
          <Grid item lg={3} />

          <Grid item lg={9} sx={{ mt: 3 }}>
            <Typography fontWeight='bold' marginBottom={2}>{intl.formatMessage({ id: 'gamut.search.popover.resultsCount' })}</Typography>
            {formNames && formNames.map((form) => <Typography>{form}</Typography>)}
          </Grid>
        </Grid>
      </GSearchMuiPopover>

    </Root>
  );
}


