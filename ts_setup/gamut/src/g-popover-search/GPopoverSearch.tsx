import React from 'react';
import { useThemeProps, TextField, Typography, Chip, Grid2, Link } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { FormattedMessage, useIntl } from 'react-intl';

import { SiteApi, useSite } from '../api-site';
import { useAnchor } from './useAnchor';
import { GLinkFormUnsecured, GLinkPhone, GPopoverButton } from '../';
import { useUtilityClasses, GPopoverSearchRoot, GSearchMuiPopover, MUI_NAME } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';
import { GLinkHyper } from '../';


export interface GPopoverSearchProps {
  itemsInColumn?: number | undefined;
  onFormLink: (target: { pageId: string, productId: string }) => void;
  pageId: SiteApi.TopicId;
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

  const [forms, setForms] = React.useState<{ linkToForm: SiteApi.TopicLink, topic: SiteApi.TopicView, label: string }[] | undefined>();
  const [phoneNumbers, setPhoneNumbers] = React.useState<SiteApi.TopicLink[] | undefined>();
  const [topics, setTopics] = React.useState<SiteApi.TopicView[] | undefined>();
  const [links, setLinks] = React.useState<SiteApi.TopicLink[] | undefined>();


  function handleOnTopic(topic: SiteApi.TopicView, event: React.MouseEvent<HTMLAnchorElement, MouseEvent> | React.MouseEvent<HTMLSpanElement, MouseEvent>) {
    props.onTopic(topic, event);
    anchor.anchorProps.onClose();
  }

  function findAllUniqueFormNames() {
    const forms = Object.values(views)
      .flatMap(
        topic => topic.links.filter((l) => l.type === 'workflow')
          .map((linkToForm) => ({ linkToForm, topic, label: linkToForm.name + intl.formatMessage({ id: 'gamut.noValueIndicatorColon' }) + topic.name }))
      ).sort((a, b) => a.label.localeCompare(b.label));
    setForms(forms);
  }

  function findAllUniqueLinkNames() {
    const links = Object.values(views)
      .flatMap(topicView => topicView.links)
      .filter((link) => link.type === 'external' || link.type === 'internal')
      .map((link) => link)
      .sort((a, b) => a.name.localeCompare(b.name));
    const uniqueLinkNames = [...new Map(links.map(link => [link.name, link])).values()];
    setLinks(uniqueLinkNames);
  }

  function findAllPhoneNumbers() {
    const phones = Object.values(views)
      .flatMap(topicView => topicView.links
        .filter((link) => link.type === 'phone'))
      .map(phone => phone);
    setPhoneNumbers(phones);
  }

  function findAllTopics() {
    const topics = Object.values(views)
      .map((topic) => topic);
    setTopics(topics);
  }

  const handleFilterByType = (type: 'forms' | 'phones' | 'topics' | 'links') => {
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
              <Chip label={intl.formatMessage({ id: 'gamut.search.popover.allForms' })} onClick={() => handleFilterByType('forms')} className={classes.quickSearchFilterItem} />
              <Chip label={intl.formatMessage({ id: 'gamut.search.popover.allPhones' })} onClick={() => handleFilterByType('phones')} className={classes.quickSearchFilterItem} />
              <Chip label={intl.formatMessage({ id: 'gamut.search.popover.allServices' })} onClick={() => handleFilterByType('topics')} className={classes.quickSearchFilterItem} />
              <Chip label={intl.formatMessage({ id: 'gamut.search.popover.allLinks' })} onClick={() => handleFilterByType('links')} className={classes.quickSearchFilterItem} />
            </Grid2>
          </Grid2>

          <Grid2>
            <Grid2 size={{ lg: 3, xl: 3 }} />
            <Grid2 size={{ lg: 9, xl: 9 }} className={classes.resultsContainer}>
              {forms && forms.map((form, index) => <GLinkFormUnsecured key={index} label={form.label} value={form.linkToForm.value}
                onClick={() => {
                  props.onFormLink({ pageId: form.topic.id, productId: form.linkToForm.id })
                }} />)}
              {phoneNumbers && phoneNumbers.map((phone, index) => <GLinkPhone key={index} label={phone.name} value={phone.value} />)}
              {topics && topics.map((topic, index) => <Link key={index} onClick={(event) => handleOnTopic(topic, event)}>{topic.name}</Link>)}
              {links && links.map((link, index) => <GLinkHyper label={link.name} value={link.value} key={index} />)}
            </Grid2>
          </Grid2>
        </div>
      </GSearchMuiPopover>

    </Root>
  );
}


