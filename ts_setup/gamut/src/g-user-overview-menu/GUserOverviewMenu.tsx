import React from 'react';
import { useThemeProps, ListItemButton, ListItemIcon, Avatar } from '@mui/material';

import SearchIcon from '@mui/icons-material/Search';
import HomeOutlinedIcon from '@mui/icons-material/HomeOutlined';
import LogoutOutlinedIcon from '@mui/icons-material/LogoutOutlined';
import LoginOutlinedIcon from '@mui/icons-material/LoginOutlined';
import AdminPanelSettingsOutlinedIcon from '@mui/icons-material/AdminPanelSettingsOutlined';
import BusinessOutlinedIcon from '@mui/icons-material/BusinessOutlined';
import AccountCircleOutlinedIcon from '@mui/icons-material/AccountCircleOutlined';


import { FormattedMessage } from 'react-intl';
import { OverridableStringUnion } from '@mui/types';
import { useContracts } from '../api-contract';
import { useOffers } from '../api-offer';
import { useComms } from '../api-comms';
import { useBookings } from '../api-bookings';
import { GAuthUn } from '../g-auth-un';
import { GAuthUnRepPerson } from '../g-auth-un-rep-person';
import { GAuthUnRepCompany } from '../g-auth-un-rep-company';
import { useIam } from '../api-iam';
import { GAuthRepPerson } from '../g-auth-rep-person';
import { GAuthRepCompany } from '../g-auth-rep-company';

import { useUtilityClasses, GUserOverviewMenuRoot, GUserOverviewMenuItemRoot, MUI_NAME } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';


export interface GUserOverviewMenuViewOverrides { };

export type GUserOverviewMenuView = OverridableStringUnion<
  'user-overview' |
  'services' | 'service-select' |
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
  onClick: (view: GUserOverviewMenuView) => void;
  hidden?: boolean;
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
    if(props.onClick('login-representative') === false) {
      return;
    }
    authRepresentativeRef.current?.click();
  }

  function handleLoginCompany() {
    if(props.onClick('login-company') === false) {
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
      <Item id='inbox' onClick={handleItemClick} endAdornment={<FormCount total={subjectStats.exchanges} />} ownerState={ownerState} active={active} />
      <Item id='bookings' onClick={handleItemClick} endAdornment={<FormCount total={bookingStats.total} />} ownerState={ownerState} active={active} />

      {iam.authType === 'REP_PERSON' && (
        <GAuthUnRepPerson ref={authUnRepresentativeRef}>
          <Item id='logout-representative' onClick={handleLogoutRepresentative} endAdornment={<><AdminPanelSettingsOutlinedIcon /><LogoutOutlinedIcon /></>} ownerState={ownerState} active={active} />
        </GAuthUnRepPerson>)
      }

      {iam.authType === 'REP_COMPANY' && (
        <GAuthUnRepCompany ref={authUnCompanyRef}>
          <Item id='logout-company' onClick={handleLogoutCompany} endAdornment={<><BusinessOutlinedIcon /><LogoutOutlinedIcon /></>} ownerState={ownerState} active={active} />
        </GAuthUnRepCompany>)
      }

      {iam.authType === 'USER' && (
        <>
          <GAuthRepPerson ref={authRepresentativeRef} >
            <Item id='login-representative' onClick={handleLoginRepresentative} endAdornment={<><AdminPanelSettingsOutlinedIcon /><LoginOutlinedIcon /></>} ownerState={ownerState} active={active} />
          </GAuthRepPerson>
          <GAuthRepCompany ref={authCompanyRef} >
            <Item id='login-company' onClick={handleLoginCompany} endAdornment={<><BusinessOutlinedIcon /><LoginOutlinedIcon /></>} ownerState={ownerState} active={active} />
          </GAuthRepCompany>
        </>)
      }

      <GAuthUn ref={authUnRef}>
        <Item id='logout' onClick={handleLogout} endAdornment={<><AccountCircleOutlinedIcon /><LogoutOutlinedIcon /></>} ownerState={ownerState} active={active} />
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
  const { id, endAdornment, onClick, hidden, active } = props;

  function handleOnClick() {
    onClick(id);
  }
  if (hidden) {
    return <></>;
  }
  return (
    <GUserOverviewMenuItemRoot className={classes.item}>
      <ListItemButton onClick={handleOnClick} selected={props.id === active} className={classes.menuButton}>
        <FormattedMessage id={`gamut.userOverview.buttons.${id}`} />
        {endAdornment && <ListItemIcon className={classes.icon}>{endAdornment}</ListItemIcon>}
      </ListItemButton>
    </GUserOverviewMenuItemRoot>
  )
}



