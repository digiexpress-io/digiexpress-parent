import React from 'react';
import { useThemeProps, Divider, Link } from '@mui/material';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';

import { FormattedMessage } from 'react-intl'
import { GDivider } from '../g-divider';
import { SiteApi, useSite } from '../api-site';
import { useAnchor } from './useAnchor';
import { GPopoverButton } from '../g-popover-button';
import { GPopoverTopicsRoot, GTopicsMuiPopover, GTopics, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';

export interface GPopoverTopicsProps {
  itemsInColumn?: number | undefined;
  onTopic: (topic: SiteApi.TopicView, event: React.MouseEvent) => void;
  slots?: { link?: React.ElementType<GTopicLinkProps> }
  component?: GOverridableComponent<GPopoverTopicsProps>
}

export interface GTopicLinkProps {
  children: SiteApi.TopicView
  onClick?: (topic: SiteApi.TopicView, event: React.MouseEvent<HTMLAnchorElement, MouseEvent> | React.MouseEvent<HTMLSpanElement, MouseEvent>) => void;
}


export const GPopoverTopics: React.FC<GPopoverTopicsProps> = (initProps) => {
  const { getTopicGroups } = useSite();
  const anchor = useAnchor();

  const [iconRotated, setIconRotated] = React.useState(false);

  const themeProps = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(themeProps);

  const GTopicLinkSlot: React.ElementType<GTopicLinkProps> = themeProps.slots?.link ?? (
    (props: GTopicLinkProps) => (<Link onClick={(event) => {
      props.onClick ? props.onClick(props.children, event) : null;
    }}>{props.children.name}</Link>)
  );

  function handleOnTopic(topic: SiteApi.TopicView, event: React.MouseEvent<HTMLAnchorElement, MouseEvent> | React.MouseEvent<HTMLSpanElement, MouseEvent>) {
    themeProps.onTopic(topic, event);
    anchor.anchorProps.onClose();
  }

  const topics = getTopicGroups(themeProps.itemsInColumn);

  React.useEffect(() => {
    setIconRotated(anchor.anchorProps.open);
  }, [anchor.anchorProps.open])

  const Root = themeProps.component ?? GPopoverTopicsRoot;

  return (
    <Root ownerState={themeProps} className={classes.root}>
      <GPopoverButton 
        onClick={anchor.onClick} iconRotated={iconRotated}
        label={<FormattedMessage id='gamut.buttons.serviceSelect' />}
        icon={<KeyboardArrowDownIcon />} />

      <GTopicsMuiPopover {...anchor.anchorProps} open={anchor.anchorProps.open} className={classes.popover}>
        <GTopics className={classes.topics}>
          {topics.map((column, index) => (
            <React.Fragment key={column.column}>
              <div className={classes.topicsLayout}>
                {column.topics.map(topic => <GTopicLinkSlot key={topic.id} children={topic} onClick={handleOnTopic} />)}
              </div>
              <GDivider index={index} total={topics.length}><Divider flexItem orientation='vertical' /></GDivider>
            </React.Fragment>
          ))}
        </GTopics>
      </GTopicsMuiPopover>
    </Root>);
}
