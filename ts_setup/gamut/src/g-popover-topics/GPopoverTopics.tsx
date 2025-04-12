import React from 'react';
import { useThemeProps, Divider, Link, Box, useMediaQuery, useTheme, Theme } from '@mui/material';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import CloseIcon from '@mui/icons-material/Close';

import { FormattedMessage } from 'react-intl'
import { GDivider } from '../g-divider';
import { SiteApi, useSite } from '../api-site';
import { AnchorProps, useAnchor } from './useAnchor';
import { GPopoverButton } from '../g-popover-button';
import { GPopoverTopicsRoot, GTopicsMuiPopover, GTopics, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';
import { GLogo } from '../g-logo';

export interface GPopoverTopicsProps {
  itemsInColumn?: number | undefined;
  onTopic: (topic: SiteApi.TopicView, event: React.MouseEvent) => void;
  filterTopic?: (topic: SiteApi.TopicView) => boolean;
  groupTopics?: (topic: SiteApi.TopicView[], itemsInColumn?: number | undefined) => SiteApi.TopicGroup[];
  slots?: {
    link?: React.ElementType<GTopicLinkProps>
    popover?: (query: typeof useMediaQuery, theme: Theme) => React.ElementType<GPopoverTopicsSlotProps> | undefined
  }
  component?: GOverridableComponent<GPopoverTopicsProps>
}

export interface GTopicLinkProps {
  children: SiteApi.TopicView
  onClick?: (topic: SiteApi.TopicView, event: React.MouseEvent<HTMLAnchorElement, MouseEvent> | React.MouseEvent<HTMLSpanElement, MouseEvent>) => void;
}

export type GPopoverTopicsSlotProps = AnchorProps & {
  topics: SiteApi.TopicView[]
}

export const GPopoverTopics: React.FC<GPopoverTopicsProps> = (initProps) => {
  const {themeProps, classes, anchor, groups, topics, iconRotated, handleOnTopic, PopoverSlot, isPopoverSlotEnabled} = useOwnerState(initProps);

  const GTopicLinkSlot: React.ElementType<GTopicLinkProps> = themeProps.slots?.link ?? (
    (props: GTopicLinkProps) => (<Link onClick={(event) => {
      props.onClick ? props.onClick(props.children, event) : null;
    }}>{props.children.name}</Link>)
  );

  const Root = themeProps.component ?? GPopoverTopicsRoot;
  
  return (
    <Root ownerState={themeProps} className={classes.root}>
      <GPopoverButton
        onClick={anchor.onClick} iconRotated={iconRotated}
        label={<FormattedMessage id='gamut.buttons.serviceSelect' />}
        icon={<KeyboardArrowDownIcon />} />

      {isPopoverSlotEnabled ? 
        (<PopoverSlot {...anchor.anchorProps} topics={topics}/>) :
        (<GTopicsMuiPopover {...anchor.anchorProps} marginThreshold={0} open={anchor.anchorProps.open} className={classes.popover} anchorReference="anchorEl">
          <Box className={classes.logoBox}>
            <GLogo variant='black_sm' />
            <Box onClick={() => anchor.anchorProps.onClose()}><CloseIcon /></Box>
          </Box>
          <GTopics className={classes.topics}>
            {groups.map((column, index) => (
              <React.Fragment key={column.column}>
                <div className={classes.topicsLayout}>
                  {column.topics.map(topic => <GTopicLinkSlot key={topic.id} children={topic} onClick={handleOnTopic} />)}
                </div>
                <GDivider index={index} total={topics.length}><Divider flexItem orientation='vertical' /></GDivider>
              </React.Fragment>
            ))}
          </GTopics>
        </GTopicsMuiPopover>)
        }
    </Root>);
}


function useOwnerState(initProps: GPopoverTopicsProps ) {
  
  const theme = useTheme();
  const { getTopicGroups, topics: allTopics } = useSite();
  const anchor = useAnchor();
  const themeProps = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(themeProps);
  const [iconRotated, setIconRotated] = React.useState(false);

  function handleOnTopic(topic: SiteApi.TopicView, event: React.MouseEvent<HTMLAnchorElement, MouseEvent> | React.MouseEvent<HTMLSpanElement, MouseEvent>) {
    themeProps.onTopic(topic, event);
    anchor.anchorProps.onClose();
  }

  const topics = themeProps.filterTopic ? allTopics.filter(themeProps.filterTopic) : allTopics;
  const groups = themeProps.groupTopics ? themeProps.groupTopics(topics, themeProps.itemsInColumn) : getTopicGroups(topics, themeProps.itemsInColumn);

  React.useEffect(() => {
    setIconRotated(anchor.anchorProps.open);
  }, [anchor.anchorProps.open])


  const resolvedPopoverSlot = themeProps.slots?.popover ? themeProps.slots?.popover(useMediaQuery, theme) : undefined;  
  const PopoverSlot: React.ElementType<GPopoverTopicsSlotProps> = resolvedPopoverSlot ? resolvedPopoverSlot : () => <></>;
  const isPopoverSlotEnabled: boolean = !!resolvedPopoverSlot;

  return {
    topics, groups, classes, anchor, iconRotated, handleOnTopic, themeProps, PopoverSlot, isPopoverSlotEnabled
  }
}
