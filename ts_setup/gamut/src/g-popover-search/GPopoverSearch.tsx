import React from 'react';
import { useThemeProps, TextField, Typography, Chip, Grid2, Link } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { FormattedMessage, useIntl } from 'react-intl';

import { SiteApi, useSite } from '../api-site';
import { useAnchor } from './useAnchor';
import { GLinkFormUnsecured, GPopoverButton } from '../';
import { useUtilityClasses, GPopoverSearchRoot, GSearchMuiPopover, MUI_NAME } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';
import { GLinkHyper } from '../';


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

  const [forms, setForms] = React.useState<SiteApi.TopicLink[] | undefined>();
  const [phoneNumbers, setPhoneNumbers] = React.useState<string[] | undefined>();
  const [topics, setTopics] = React.useState<SiteApi.TopicView[] | undefined>();
  const [links, setLinks] = React.useState<SiteApi.TopicLink[] | undefined>();


  function findAllUniqueFormNames() {
    const forms = Object.values(views)
      .flatMap(topicView => topicView.links
        .filter((l) => l.type === 'workflow')
        .map((form) => form));
    const uniqueFormNames = [...new Map(forms.map(form => [form.name, form])).values()];
    setForms(uniqueFormNames);
  }

  function findAllUniqueLinkNames() {
    const links = Object.values(views)
      .flatMap(topicView => topicView.links)
      .filter((link) => link.type === 'external' || link.type === 'internal')
      .map((link) => link);
    const uniqueLinkNames = [...new Map(links.map(link => [link.name, link])).values()];
    setLinks(uniqueLinkNames);
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
      .map((topic) => topic);
    setTopics(topics);
  }

  const handleChipClick = (type: 'forms' | 'phones' | 'topics' | 'links') => {
    setForms(undefined);
    setPhoneNumbers(undefined);
    setTopics(undefined);
    setLinks(undefined);

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
      <GPopoverButton onClick={anchor.onClick} label={<FormattedMessage id='gamut.buttons.search' />} icon={<SearchIcon />} />
      <GSearchMuiPopover {...anchor.anchorProps} open={anchor.anchorProps.open}>
        <div className={classes.layoutContainer}>
          <Grid2>
            <Grid2 size={{ lg: 12, xl: 12 }} className={classes.titleContainer}>
            <TextField className={classes.inputField} placeholder={intl.formatMessage({ id: 'gamut.search.popover.input.placeholder' })} />
            <Typography className={classes.title}>{intl.formatMessage({ id: 'gamut.search.popover.title' })}</Typography>
          </Grid2>

          <Grid2 size={{ lg: 3, xl: 3 }} />

            <Grid2 size={{ lg: 9, xl: 9 }} className={classes.quickSearch}>
              <Chip label={intl.formatMessage({ id: 'gamut.search.popover.allForms' })} onClick={() => handleChipClick('forms')} className={classes.quickSearchFilterItem} />
              <Chip label={intl.formatMessage({ id: 'gamut.search.popover.allPhones' })} onClick={() => handleChipClick('phones')} className={classes.quickSearchFilterItem} />
              <Chip label={intl.formatMessage({ id: 'gamut.search.popover.allServices' })} onClick={() => handleChipClick('topics')} className={classes.quickSearchFilterItem} />
              <Chip label={intl.formatMessage({ id: 'gamut.search.popover.allLinks' })} onClick={() => handleChipClick('links')} className={classes.quickSearchFilterItem} />
          </Grid2>
        </Grid2>

        <Grid2>
          <Grid2 size={{ lg: 3, xl: 3 }} />
            <Grid2 size={{ lg: 9, xl: 9 }} className={classes.resultsContainer}>
            {forms && forms.map((form, index) => <GLinkFormUnsecured key={index} label={form.name} value={form.value} onClick={() => { console.log('form', form.name) }} />)}
            {phoneNumbers && phoneNumbers.map((phone, index) => <Typography key={index}>{phone}</Typography>)}
            {topics && topics.map((topic, index) => <Typography key={index}>{topic.name}</Typography>)}
            {links && links.map((link, index) => <GLinkHyper label={link.name} value={link.value} key={index} />)}
          </Grid2>
        </Grid2>
        </div>
      </GSearchMuiPopover>

    </Root>
  );
}


