import React from 'react';
import { useThemeProps, TextField, Typography, Chip, Grid2 } from '@mui/material';
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
  const [phoneNumbers, setPhoneNumbers] = React.useState<string[] | undefined>();
  const [topicNames, setTopicNames] = React.useState<string[] | undefined>();
  const [linkNames, setLinkNames] = React.useState<string[] | undefined>();


  function findAllUniqueFormNames() {
    const formNames = Object.values(views)
      .flatMap(topicView => topicView.links
        .filter((l) => l.type === 'workflow')
        .map((l) => l.name));
    const uniqueFormNames = [...new Set(formNames)];
    setFormNames(uniqueFormNames);
  }

  function findAllUniqueLinkNames() {
    const links = Object.values(views)
      .flatMap(topicView => topicView.links)
      .filter((link) => link.type === 'external' || link.type === 'internal')
      .map((link) => link.name);
    const uniqueLinkNames = [...new Set(links)];
    setLinkNames(uniqueLinkNames);
  }

  function findAllPhoneNumbers() {
    const phones = Object.values(views)
      .flatMap(topicView => topicView.links
        .filter((link) => link.type === 'phone'))
      .map(phone => phone.name);
    setPhoneNumbers(phones);
  }

  function findAllTopics() {
    const topics = Object.values(views)
      .flatMap(topicView => topicView.name)
      .map((t) => t);
    setTopicNames(topics);
  }

  const handleChipClick = (type: 'forms' | 'phones' | 'topics' | 'links') => {
    setFormNames(undefined);
    setPhoneNumbers(undefined);
    setTopicNames(undefined);
    setLinkNames(undefined);

    if (type === 'forms') {
      findAllUniqueFormNames();
    } else if (type === 'phones') {
      findAllPhoneNumbers();
    } else if (type === 'topics') {
      findAllTopics();
    } else if (type === 'links') {
      findAllUniqueLinkNames();
    } else {
      console.log('something happened')
    }
  };

  const Root = props.component ?? GPopoverSearchRoot;

  return (
    <Root className={classes.root} ownerState={props}>
      <GPopoverButton onClick={anchor.onClick}
        label={<FormattedMessage id='gamut.buttons.search' />}
        icon={<SearchIcon />}
      />
      <GSearchMuiPopover {...anchor.anchorProps} open={anchor.anchorProps.open}>
        <Grid2 spacing={1} sx={{ alignItems: 'center' }}>
          <Grid2 size={{ lg: 3 }}>
            <Typography className={classes.title}>{intl.formatMessage({ id: 'gamut.search.popover.title' })}</Typography>
          </Grid2>

          <Grid2 size={{ lg: 9 }}>
            <TextField className={classes.inputField} placeholder={intl.formatMessage({ id: 'gamut.search.popover.input.placeholder' })} />
          </Grid2>

          <Grid2 size={{ lg: 3 }} />

          <Grid2 size={{ lg: 9 }}>
            <Chip label={intl.formatMessage({ id: 'gamut.search.popover.allForms' })} onClick={() => handleChipClick('forms')} sx={{ mx: 0.5 }} />
            <Chip label={intl.formatMessage({ id: 'gamut.search.popover.allPhones' })} onClick={() => handleChipClick('phones')} sx={{ mx: 0.5 }} />
            <Chip label={intl.formatMessage({ id: 'gamut.search.popover.allServices' })} onClick={() => handleChipClick('topics')} sx={{ mx: 0.5 }} />
            <Chip label={intl.formatMessage({ id: 'gamut.search.popover.allLinks' })} onClick={() => handleChipClick('links')} sx={{ mx: 0.5 }} />
          </Grid2>
        </Grid2>

        <Grid2>
          <Grid2 size={{ lg: 3, xl: 3 }} />
          <Grid2 size={{ lg: 9 }} sx={{ mt: 3 }}>
            {formNames && formNames.map((form, index) => <Typography key={index}>{form}</Typography>)}
            {phoneNumbers && phoneNumbers.map((phone, index) => <Typography key={index}>{phone}</Typography>)}
            {topicNames && topicNames.map((topic, index) => <Typography key={index}>{topic}</Typography>)}
            {linkNames && linkNames.map((link, index) => <Typography key={index}>{link}</Typography>)}
          </Grid2>
        </Grid2>
      </GSearchMuiPopover>

    </Root>
  );
}


