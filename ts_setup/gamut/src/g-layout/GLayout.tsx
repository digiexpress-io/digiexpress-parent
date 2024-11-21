import React from 'react';
import { Box, useThemeProps, Grid } from '@mui/material';
import { useUtilityClasses, useGLayoutRows, GLayoutRoot, GLayoutRow, MUI_NAME } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';


export interface GLayoutClasses {
  root: string;
  row: string;
}
export type GLayoutClassKey = keyof GLayoutClasses;
export interface GLayoutProps {
  children?: React.ReactNode | undefined;
  variant: 'toolbar-n-rows-2-columns' | 'secured-1-row-2-columns' | 'secured-1-row-1-column' | 'secured-1-row-1-column-small' | 'fill-session-start-end';
  slots?: {
    left?: React.ElementType,
    right?: React.ElementType;
    center?: React.ElementType;
    topTitle?: React.ElementType;
    breadcrumbs?: React.ElementType;
  };

  component?: GOverridableComponent<GLayoutProps>;
}

export const GLayout: React.FC<GLayoutProps> = (initProps) => {
  const themeProps = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(themeProps);
  const Root = themeProps.component ?? GLayoutRoot


  if (themeProps.variant === 'secured-1-row-2-columns') {
    const Left: React.ElementType = themeProps.slots?.left ?? (() => <></>);
    const Right: React.ElementType = themeProps.slots?.right ?? (() => <></>);
    const Breadcrumbs: React.ElementType = themeProps.slots?.breadcrumbs ?? (() => <></>);

    return (
      <Root ownerState={themeProps} className={classes.root}>
        <Grid container>
          <Grid item xs={12} sm={12} md={12} lg={12} className={classes.breadcrumbs}>
            <Breadcrumbs className={classes.breadcrumbs} />
          </Grid>

          <Grid item xs={12} sm={12} md={12} lg={8} className={classes.left}>
            <Left />
          </Grid>

          <Grid item xs={12} sm={12} md={12} lg={4} className={classes.right}>
            <Right />
          </Grid>

          {themeProps.children}
        </Grid>
      </Root>)
  }

  else if (themeProps.variant === 'secured-1-row-1-column') {
    const Left: React.ElementType = themeProps.slots?.left ?? (() => <></>);
    const TopTitle: React.ElementType = themeProps.slots?.topTitle ?? (() => <></>);
    const Breadcrumbs: React.ElementType = themeProps.slots?.breadcrumbs ?? (() => <></>);

    return (
      <Root ownerState={themeProps} className={classes.root}>
        <Grid container>

          <Grid item xs={12} sm={12} md={12} lg={12} className={classes.breadcrumbs}>
            <Breadcrumbs className={classes.breadcrumbs} />
          </Grid>

          <Grid item xs={12} sm={12} md={12} lg={12} className={classes.topTitle}>
            <TopTitle className={classes.topTitle} />
          </Grid>

          <Grid item xs={12} sm={12} md={12} lg={12} className={classes.oneColContent}>
            <Left />
          </Grid>
          {themeProps.children}
        </Grid>

      </Root>)
  }

  else if (themeProps.variant === 'fill-session-start-end') {
    const Center: React.ElementType = themeProps.slots?.center ?? (() => <></>);
    const TopTitle: React.ElementType = themeProps.slots?.topTitle ?? (() => <></>);
    const Breadcrumbs: React.ElementType = themeProps.slots?.breadcrumbs ?? (() => <></>);

    return (
      <Root ownerState={themeProps} className={classes.root}>
        <Grid container className={classes.fillSessionStartEndLayout}>
          <Grid item xs={12} sm={12} md={12} lg={12} className={classes.breadcrumbs}>
            <Breadcrumbs className={classes.breadcrumbs} />
          </Grid>

          <div className={classes.fillSessionStartEnd}>
            <Grid item xs={12} sm={12} md={12} lg={12} className={classes.fillSessionStartEndTopTitle}>
              <TopTitle />
            </Grid>

            <Grid item xs={12} sm={12} md={12} lg={12} className={classes.fillSessionStartEndChildren}>
              <Center />
            </Grid>
            {themeProps.children}
          </div>

        </Grid>
      </Root>)
  }


  else if (themeProps.variant === 'secured-1-row-1-column-small') {
    const Left: React.ElementType = themeProps.slots?.left ?? (() => <></>);
    const TopTitle: React.ElementType = themeProps.slots?.topTitle ?? (() => <></>);
    const Breadcrumbs: React.ElementType = themeProps.slots?.breadcrumbs ?? (() => <></>);

    return (
      <Root ownerState={themeProps} className={classes.root}>
        <Grid container>

          <Grid item xs={12} sm={12} md={12} lg={12} className={classes.breadcrumbs}>
            <Breadcrumbs className={classes.breadcrumbs} />
          </Grid>

          <Grid item xs={12} sm={12} md={12} lg={12} className={classes.topTitle}>
            <TopTitle className={classes.topTitle} />
          </Grid>

          <Grid item xs={12} sm={12} md={12} lg={12} className={classes.oneColContentSmall}>
            <Left />
          </Grid>
          {themeProps.children}
        </Grid>

      </Root>)
  }

  else {
    const children = useGLayoutRows(themeProps.children);
    return (
      <Root ownerState={themeProps} className={classes.root}>
        {children.map((row: any, index: any) => {
          return (
            <React.Fragment key={index}>
              <GLayoutRow className={classes.toolbar}>
                <div >{row.left}</div>
                <Box flexGrow={1} />
                <div className={classes.buttonRow}>{row.right}</div>
              </GLayoutRow>
            </React.Fragment>);
        })}
      </Root>
    )
  }
}


