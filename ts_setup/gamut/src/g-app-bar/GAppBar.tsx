import React from 'react';
import { Toolbar, useThemeProps, useMediaQuery, useTheme } from '@mui/material';
import { GShellClassName } from '../g-shell';
import { GLayout } from '../g-layout';
import { GLogo } from '../g-logo';
import { GLocales } from '../g-locales';
import { GLogin } from '../g-login';
import { GUserOverviewMenuView } from '../g-user-overview-menu';
import { GLogout } from '../g-logout';
import { MUI_NAME, useUtilityClasses, GAppBarRoot } from './useUtilityClasses';
import { useIam } from '../api-iam';
import { GOverridableComponent } from '../g-override';
import { FormattedMessage } from 'react-intl';

export interface GNavSlotProps {}
export interface GSearchSlotProps {}

export interface GAppBarProps {
  locale: string;
  viewId?: GUserOverviewMenuView;
  onLocale(newLocale: string): void;
  component?: GOverridableComponent<GAppBarProps>;

  onLogoClick?: (view: GUserOverviewMenuView | undefined) => void;
  slots?: {
    nav?: React.ElementType<GNavSlotProps> | undefined;
    search?: React.ElementType<GSearchSlotProps> | undefined;
  };
}

export const GAppBar: React.FC<GAppBarProps> = (initProps) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  const iam = useIam();
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses();
  const ownerState = {
    ...props,
  };

  const Nav = props.slots?.nav;
  const Search = props.slots?.search;

  function handleClick() {
    if (props.onLogoClick) {
      props.onLogoClick(props.viewId);
    }
  }

  const Root = props.component ?? GAppBarRoot;

  const GUserIdentity = ({
    userName,
    classes,
  }: {
    userName: string;
    classes: ReturnType<typeof useUtilityClasses>;
  }) => {
    return (
      <div className={classes.userIdentityLabel}>
        <div className={classes.userIdentityText}>
          <FormattedMessage id="header.userIdentity.label" />
        </div>
        <div className={classes.userDisplayName}>{userName}</div>
      </div>
    );
  };

  return (
    <Root ownerState={ownerState} className={classes.root}>
      <Toolbar className={GShellClassName}>
        <GLayout variant={'toolbar-n-rows-2-columns'}>
          <GLogo variant="black_lg" onClick={handleClick} />
          <div className={classes.rightSideLayout}>
              {iam.authType !== 'ANON' && (
                <GUserIdentity userName={iam.userName ?? ''} classes={classes} />
              )}
            <div className={classes.buttonLayout}>
              <GLocales value={props.locale} onClick={props.onLocale} showOnlyFlag={isMobile} />
              {iam.authType === 'ANON'
                ? <GLogin hideStartIcon={isMobile} />
                : <GLogout hideStartIcon={isMobile} />}
            </div>
          </div>
          <>
            {Nav && <Nav />}
          </>
          {Search && <Search />}
        </GLayout>
      </Toolbar>
    </Root>
  );
};




export const GAppBarSpacer: React.FC<{}> = () => {
  const classes = useUtilityClasses();
  return (
    <GAppBarRoot className={classes.root}>
      <Toolbar className={GShellClassName}></Toolbar>
    </GAppBarRoot>
  );
};