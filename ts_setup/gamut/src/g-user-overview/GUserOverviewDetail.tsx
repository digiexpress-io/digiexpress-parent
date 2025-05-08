import React from 'react';
import { useThemeProps, Typography, Avatar, CardContent } from '@mui/material';
import { GUserOverviewMenuView } from '../';
import { GUserOverviewDetail as OverviewDetail, MUI_NAME, useUtilityClasses } from './useUtilityClasses';


export interface GUserOverviewDetailProps {
  title?: string;
  buttonLabel?: string;
  count?: number;
  children?: React.ReactNode;
  viewId?: GUserOverviewMenuView;
  onClick?: (view: GUserOverviewMenuView | undefined) => void;
}


export const GUserOverviewDetail: React.FC<GUserOverviewDetailProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })

  const classes = useUtilityClasses();
  const ownerState = {
    ...props,
  }


  function handleClick() {
    if (props.onClick) {
      props.onClick(props.viewId);
    }
  }

  return (
    <OverviewDetail ownerState={ownerState} className={classes.overviewItem} onClick={handleClick}>
      <Typography className={classes.overviewItemTitle}>{props.title}</Typography>

      {props.children && <CardContent>{props.children}</CardContent>}
      {props.count === undefined ? <></> :
        <div className={classes.overviewItemCount}>
          <Avatar className={classes.overviewItemCountAvatar}>
            <Typography className={classes.overviewItemCountAvatarLabel}>{props.count}</Typography>
          </Avatar>
          <Typography className={classes.overviewItemButtonLabel}>{props.buttonLabel}</Typography>
        </div>
      }
    </OverviewDetail>)
}





