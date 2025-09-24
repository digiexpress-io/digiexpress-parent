import React from 'react';
import { Box, useThemeProps, Grid as Oldgrid } from '@mui/material';
import { useUtilityClasses, useEveliLayoutRows, EveliLayoutRoot, EveliLayoutRow, MUI_NAME } from './useUtilityClasses';
import { EveliOverridableComponent } from '@dxs-ts/eveli-api';


export interface EveliLayoutClasses {
  root: string;
  row: string;
}
export type EveliLayoutClassKey = keyof EveliLayoutClasses;
export interface EveliLayoutProps {
  children?: React.ReactNode | undefined;
  variant: 'toolbar-n-rows-2-columns' | 'secured-1-row-2-columns' | 'secured-1-row-1-column' | 'secured-1-row-1-column-small' | 'fill-session-start-end';
  slots?: {
    left?: React.ElementType,
    right?: React.ElementType;
    center?: React.ElementType;
    topTitle?: React.ElementType;
    breadcrumbs?: React.ElementType;
  };

  component?: EveliOverridableComponent<EveliLayoutProps>;
}

export const EveliLayout: React.FC<EveliLayoutProps> = (initProps) => {

  const themeProps = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(themeProps);
  const Root = themeProps.component ?? EveliLayoutRoot


  if (themeProps.variant === 'secured-1-row-2-columns') {
    const Left: React.ElementType = themeProps.slots?.left ?? (() => <></>);
    const Right: React.ElementType = themeProps.slots?.right ?? (() => <></>);
    const Breadcrumbs: React.ElementType = themeProps.slots?.breadcrumbs ?? (() => <></>);

    return (
      <Root ownerState={themeProps} className={classes.root}>
        <Oldgrid container>
          <Oldgrid item xs={12} sm={12} md={12} lg={12} className={classes.breadcrumbs}>
            <Breadcrumbs className={classes.breadcrumbs} />
          </Oldgrid>

          <Oldgrid item xs={12} sm={12} md={12} lg={8} className={classes.left}>
            <Left />
          </Oldgrid>

          <Oldgrid item xs={12} sm={12} md={12} lg={4} className={classes.right}>
            <Right />
          </Oldgrid>

          {themeProps.children}
        </Oldgrid>
      </Root>)
  }

  else if (themeProps.variant === 'secured-1-row-1-column') {
    const Left: React.ElementType = themeProps.slots?.left ?? (() => <></>);
    const TopTitle: React.ElementType = themeProps.slots?.topTitle ?? (() => <></>);
    const Breadcrumbs: React.ElementType = themeProps.slots?.breadcrumbs ?? (() => <></>);

    return (
      <Root ownerState={themeProps} className={classes.root}>
        <Oldgrid container>

          <Oldgrid item xs={12} sm={12} md={12} lg={12} className={classes.breadcrumbs}>
            <Breadcrumbs className={classes.breadcrumbs} />
          </Oldgrid>

          <Oldgrid item xs={12} sm={12} md={12} lg={12} className={classes.topTitle}>
            <TopTitle className={classes.topTitle} />
          </Oldgrid>

          <Oldgrid item xs={12} sm={12} md={12} lg={12} className={classes.oneColContent}>
            <Left />
          </Oldgrid>
          {themeProps.children}
        </Oldgrid>

      </Root>)
  }

  else if (themeProps.variant === 'fill-session-start-end') { //TODO is this variant needed anymore?
    const Center: React.ElementType = themeProps.slots?.center ?? (() => <></>);
    const TopTitle: React.ElementType = themeProps.slots?.topTitle ?? (() => <></>);
    const Breadcrumbs: React.ElementType = themeProps.slots?.breadcrumbs ?? (() => <></>);

    return (
      <Root ownerState={themeProps} className={classes.root}>
        <Oldgrid container className={classes.fillSessionStartEndLayout}>
          <Oldgrid item xs={12} sm={12} md={12} lg={12} className={classes.breadcrumbs}>
            <Breadcrumbs className={classes.breadcrumbs} />
          </Oldgrid>

          <div className={classes.fillSessionStartEnd}>
            <Oldgrid item xs={12} sm={12} md={12} lg={12} className={classes.fillSessionStartEndTopTitle}>
              <TopTitle />
            </Oldgrid>

            <Oldgrid item xs={12} sm={12} md={12} lg={12} className={classes.fillSessionStartEndChildren}>
              <Center />
            </Oldgrid>
            {themeProps.children}
          </div>

        </Oldgrid>
      </Root>)
  }


  else if (themeProps.variant === 'secured-1-row-1-column-small') {
    const Left: React.ElementType = themeProps.slots?.left ?? (() => <></>);
    const TopTitle: React.ElementType = themeProps.slots?.topTitle ?? (() => <></>);
    const Breadcrumbs: React.ElementType = themeProps.slots?.breadcrumbs ?? (() => <></>);

    return (
      <Root ownerState={themeProps} className={classes.root}>
        <Oldgrid container>

          <Oldgrid item xs={12} sm={12} md={12} lg={12} className={classes.breadcrumbs}>
            <Breadcrumbs className={classes.breadcrumbs} />
          </Oldgrid>

          <Oldgrid item xs={12} sm={12} md={12} lg={12} className={classes.topTitle}>
            <TopTitle className={classes.topTitle} />
          </Oldgrid>

          <Oldgrid item xs={12} sm={12} md={12} lg={12} className={classes.oneColContentSmall}>
            <Left />
          </Oldgrid>
          {themeProps.children}
        </Oldgrid>

      </Root>)
  }

  else {
    const children = useEveliLayoutRows(themeProps.children);
    return (
      <Root ownerState={themeProps} className={classes.root}>
        {children.map((row: any, index: any) => {
          return (
            <React.Fragment key={index}>
              <EveliLayoutRow className={classes.toolbar}>
                {row.left}
                <Box flexGrow={1} />
                <div className={classes.buttonRow}>{row.right}</div>
              </EveliLayoutRow>
            </React.Fragment>);
        })}
      </Root>
    )
  }
}


