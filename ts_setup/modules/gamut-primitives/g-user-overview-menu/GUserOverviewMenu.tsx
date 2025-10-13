import React from 'react';
import { useThemeProps, ListItemButton, ListItemIcon, Avatar, Typography, Box } from '@mui/material';

import { Search as SearchIcon } from '@mui/icons-material';
import { HomeOutlined as HomeOutlinedIcon } from '@mui/icons-material';
import { LogoutOutlined as LogoutOutlinedIcon } from '@mui/icons-material';
import { LoginOutlined as LoginOutlinedIcon } from '@mui/icons-material';
import { AdminPanelSettingsOutlined as AdminPanelSettingsOutlinedIcon } from '@mui/icons-material';
import { BusinessOutlined as BusinessOutlinedIcon } from '@mui/icons-material';
import { AccountCircleOutlined as AccountCircleOutlinedIcon } from '@mui/icons-material';


import { FormattedMessage } from 'react-intl';
import { OverridableStringUnion } from '@mui/types';
import { useContracts } from '@dxs-ts/gamut-api';
import { useOffers } from '@dxs-ts/gamut-api';
import { useComms } from '@dxs-ts/gamut-api';
import { useBookings } from '@dxs-ts/gamut-api';
import { GAuthUn } from '../g-auth-un';
import { GAuthUnRepPerson } from '../g-auth-un-rep-person';
import { GAuthUnRepCompany } from '../g-auth-un-rep-company';
import { useIam } from '@dxs-ts/gamut-api';
import { GAuthRepPerson } from '../g-auth-rep-person';
import { GAuthRepCompany } from '../g-auth-rep-company';

import { useUtilityClasses, GUserOverviewMenuRoot, GUserOverviewMenuItem, MUI_NAME } from './useUtilityClasses';
import { GOverridableComponent } from '@dxs-ts/gamut-api';


export interface GUserOverviewMenuViewOverrides { };

export type GUserOverviewMenuView = OverridableStringUnion<
  'user-overview' |
  'services' |
  'requests-in-progress' |
  'awaiting-decision' | 'with-decision' |
  'bookings' | 'inbox' |
  'product' |

  'login-representative' | 'login-company' |
  'logout-representative' | 'logout-company' | 'logout',
  GUserOverviewMenuViewOverrides>;

export interface GUserOverviewMenuItemSlotProps {
  id: GUserOverviewMenuView;
  endAdornment?: React.ReactNode | undefined;
  userOrRepOrCompanyName?: string | undefined;
  onClick: (view: GUserOverviewMenuView) => void;
  disabled?: boolean;
  active: GUserOverviewMenuView
}

export interface GUserOverviewMenuProps {
  onClick(type: GUserOverviewMenuView): void | false;
  component?: GOverridableComponent<GUserOverviewMenuProps>;
  slotProps?: Partial<Record<GUserOverviewMenuView, Partial<GUserOverviewMenuItemSlotProps>>>
  defaultView: GUserOverviewMenuView;
  slots?: {
    extra?: React.ElementType | undefined
  }
}



export const GUserOverviewMenu: React.FC<GUserOverviewMenuProps> = (initProps) => {
  const [active, setActive] = React.useState<GUserOverviewMenuView>(initProps.defaultView);
  const { contractStats } = useContracts();
  const { offers } = useOffers();
  const { subjectStats } = useComms();
  const { bookingStats } = useBookings();
  const iam = useIam();
  const authUnRef = React.useRef<HTMLInputElement>(null);
  const authUnCompanyRef = React.useRef<HTMLInputElement>(null);
  const authUnRepresentativeRef = React.useRef<HTMLInputElement>(null);
  const authCompanyRef = React.useRef<HTMLInputElement>(null);
  const authRepresentativeRef = React.useRef<HTMLInputElement>(null);

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })

  const classes = useUtilityClasses();
  const handleItemClick = (id: GUserOverviewMenuView) => {
    setActive(id);
    props.onClick(id);
  };

  const ownerState = {
    ...props,
  }

  const { slots } = props;
  const Extra = slots?.extra;

  function handleLogout() {
    if (props.onClick('logout') === false) {
      return;
    }
    authUnRef.current?.click();
  }

  function handleLogoutRepresentative() {
    if (props.onClick('logout-representative') === false) {
      return;
    }
    authUnRepresentativeRef.current?.click();
  }

  function handleLogoutCompany() {
    if (props.onClick('logout-company') === false) {
      return;
    }
    authUnCompanyRef.current?.click();
  }

  function handleLoginRepresentative() {
    if (props.onClick('login-representative') === false) {
      return;
    }
    authRepresentativeRef.current?.click();
  }

  function handleLoginCompany() {
    if (props.onClick('login-company') === false) {
      return;
    }
    authCompanyRef.current?.click();
  }

  const Root = initProps.component ?? GUserOverviewMenuRoot;

  return (
    <Root className={classes.root} ownerState={ownerState}>
      <Item id='user-overview' onClick={handleItemClick} endAdornment={<HomeOutlinedIcon />} ownerState={ownerState} active={active} />
      <Item id='services' onClick={handleItemClick} endAdornment={<SearchIcon />} ownerState={ownerState} active={active} />
      <Item id='requests-in-progress' onClick={handleItemClick} endAdornment={<FormCount total={offers.length} />} ownerState={ownerState} active={active} />
      <Item id='awaiting-decision' onClick={handleItemClick} endAdornment={<FormCount total={contractStats.awaitingDecision} />} ownerState={ownerState} active={active} />
      <Item id='with-decision' onClick={handleItemClick} endAdornment={<FormCount total={contractStats.decided} />} ownerState={ownerState} active={active} />
      <Item id='inbox' onClick={handleItemClick} endAdornment={<FormCount total={subjectStats.unread} />} ownerState={ownerState} active={active} />
      <Item id='bookings' onClick={handleItemClick} endAdornment={<FormCount total={bookingStats.total} />} ownerState={ownerState} active={active} />

      {iam.authType === 'REP_PERSON' && (
        <GAuthUnRepPerson ref={authUnRepresentativeRef}>
          <Item id='logout-representative'
            onClick={handleLogoutRepresentative}
            endAdornment={<><AdminPanelSettingsOutlinedIcon /><LogoutOutlinedIcon /></>}
            ownerState={ownerState}
            active={active}
            userOrRepOrCompanyName={iam.user?.representedPerson?.name}
          />
        </GAuthUnRepPerson>)
      }

      {iam.authType === 'REP_COMPANY' && (
        <GAuthUnRepCompany ref={authUnCompanyRef}>
          <Item id='logout-company' onClick={handleLogoutCompany}
            endAdornment={<><BusinessOutlinedIcon /><LogoutOutlinedIcon /></>}
            ownerState={ownerState} active={active}
            userOrRepOrCompanyName={iam.user?.representedCompany?.name}
          />
        </GAuthUnRepCompany>)
      }

      {iam.authType === 'USER' && (
        <>
          <GAuthRepPerson ref={authRepresentativeRef} >
            <Item id='login-representative' onClick={handleLoginRepresentative}
              endAdornment={<><AdminPanelSettingsOutlinedIcon /><LoginOutlinedIcon /></>}
              ownerState={ownerState}
              active={active}
            />
          </GAuthRepPerson>
          <GAuthRepCompany ref={authCompanyRef} >
            <Item id='login-company' onClick={handleLoginCompany}
              endAdornment={<><BusinessOutlinedIcon /><LoginOutlinedIcon /></>}
              ownerState={ownerState}
              active={active}
            />
          </GAuthRepCompany>
        </>)
      }

      <GAuthUn ref={authUnRef}>
        <Item id='logout' onClick={handleLogout}
          endAdornment={<><AccountCircleOutlinedIcon /><LogoutOutlinedIcon /></>}
          ownerState={ownerState} active={active}
          userOrRepOrCompanyName={`${iam.user?.firstName}` + " " + `${iam.user?.lastName}`}
        />
      </GAuthUn>

      {Extra && <Extra />}
    </Root>
  )
}



const FormCount: React.FC<{ total: number }> = ({ total }) => {
  const classes = useUtilityClasses();

  return (<Avatar className={classes.formCount}>{total}</Avatar>)
}

const Item: React.FC<GUserOverviewMenuItemSlotProps & { ownerState: GUserOverviewMenuProps }> = (initProps) => {
  const classes = useUtilityClasses();
  const slotProps = initProps.ownerState.slotProps;
  const overrides = slotProps && slotProps[initProps.id] ? slotProps[initProps.id] : {};
  const props = { ...initProps, ...overrides };
  const { id, endAdornment, onClick, disabled, active, userOrRepOrCompanyName } = props;

  function handleOnClick() {
    onClick(id);
  }
  if (disabled) {
    return <></>;
  }

  let label: React.ReactNode;
  switch (id) {
    case 'logout-representative':
      label = <FormattedMessage id="gamut.userOverview.buttons.logout-representative" />;
      break;
    case 'login-representative':
      label = <FormattedMessage id="gamut.userOverview.buttons.login-representative" />;
      break;
    case 'logout':
      label = <FormattedMessage id="gamut.userOverview.buttons.logout" />;
      break;
    case 'logout-company':
      label = <FormattedMessage id="gamut.userOverview.buttons.logout-company" />;
      break;
    case 'login-company':
      label = <FormattedMessage id="gamut.userOverview.buttons.login-company" />;
      break;
    case 'user-overview':
      label = <FormattedMessage id="gamut.userOverview.buttons.user-overview" />;
      break;
    case 'services':
      label = <FormattedMessage id="gamut.userOverview.buttons.services" />;
      break;
    case 'requests-in-progress':
      label = <FormattedMessage id="gamut.userOverview.buttons.requests-in-progress" />;
      break;
    case 'awaiting-decision':
      label = <FormattedMessage id="gamut.userOverview.buttons.awaiting-decision" />;
      break;
    case 'with-decision':
      label = <FormattedMessage id="gamut.userOverview.buttons.with-decision" />;
      break;
    case 'inbox':
      label = <FormattedMessage id="gamut.userOverview.buttons.inbox" />;
      break;
    case 'bookings':
      label = <FormattedMessage id="gamut.userOverview.buttons.bookings" />;
      break;
    default:
      label = id;
  }

  return (
    <GUserOverviewMenuItem className={classes.menuItem}>
      <ListItemButton onClick={handleOnClick} selected={props.id === active} className={classes.menuButton}>
        <Box className={classes.menuButtonLayout}>
          {label}
          {userOrRepOrCompanyName && (
            <Typography className={classes.userOrRepOrCompanyNameStyle}>
              {userOrRepOrCompanyName}
            </Typography>
          )}
        </Box>
        {endAdornment && <ListItemIcon className={classes.overviewMenuIcon}>{endAdornment}</ListItemIcon>}
      </ListItemButton>
    </GUserOverviewMenuItem>
  );
};


