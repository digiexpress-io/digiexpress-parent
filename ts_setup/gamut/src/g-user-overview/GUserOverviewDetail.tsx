import React from 'react';
import { useThemeProps, Typography, Avatar, CardContent } from '@mui/material';
import { GUserOverviewMenuView } from '../';
import { GUserOverviewDetailRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';


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
    <GUserOverviewDetailRoot ownerState={ownerState} className={classes.root} onClick={handleClick}>
      <Typography className={classes.title}>{props.title}</Typography>

      {props.children && <CardContent>{props.children}</CardContent>}
      {props.count === undefined ? <></> :
        <div className={classes.count}>
          <Avatar className={classes.countAvatar}>
            <Typography className={classes.countAvatarLabel}>{props.count}</Typography>
          </Avatar>
          <Typography className={classes.buttonLabel}>{props.buttonLabel}</Typography>
        </div>
      }
    </GUserOverviewDetailRoot>)
}





