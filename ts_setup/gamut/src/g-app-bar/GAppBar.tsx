import React from 'react';
import { Toolbar, useThemeProps } from '@mui/material';
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


export interface GNavSlotProps { }
export interface GSearchSlotProps { }

export interface GAppBarProps {
  locale: string;
  viewId?: GUserOverviewMenuView;
  onLocale(newLocale: string): void;
  component?: GOverridableComponent<GAppBarProps>;

  onLogoClick?: (view: GUserOverviewMenuView | undefined) => void;
  slots?: {
    nav?: React.ElementType<GNavSlotProps> | undefined,
    search?: React.ElementType<GSearchSlotProps> | undefined,
  },
}


export const GAppBar: React.FC<GAppBarProps> = (initProps) => {
  const iam = useIam();
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }

  const Nav = props.slots?.nav;
  const Search = props.slots?.search;

  function handleClick() {
    if (props.onLogoClick) {
      props.onLogoClick(props.viewId);
    }
  }

  const Root = props.component ?? GAppBarRoot;

  return (
    <Root ownerState={ownerState} className={classes.root}>
      <Toolbar className={GShellClassName}>
        <GLayout variant={'toolbar-n-rows-2-columns'}>
          <GLogo variant='black_lg' onClick={handleClick} />
          <GLocales value={props.locale} onClick={props.onLocale} />
          {iam.authType === 'ANON' ? <GLogin /> : <GLogout />}
          <>
            {Nav && <Nav />}
          </>
          {Search && <Search />}
        </GLayout>
      </Toolbar>
    </Root>
  )
}