import { PrefsApi } from '@/api-prefs';
import { styled, generateUtilityClass, Box, Switch, Avatar } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'


export const EveliUserProfileClassName = 'EveliUserProfileBase';


export const MUI_NAME = 'EveliUserProfile';
export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    sectionTitle: ['sectionTitle'],
    divider: ['divider'],
    avatar: ['avatar'],
    avatarUserFirstLastName: ['avatarUserFirstLastName'],
    avatarUserName: ['avatarUserName']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const EveliUserProfileRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.sectionTitle,
      styles.divider,
      styles.avatar,
      styles.avatarUserFirstLastName,
      styles.avatarUserName
    ];
  },
})(({ theme }) => {

  return {
    marginBottom: theme.spacing(10),
    '& .EveliUserProfile-divider': {
      marginBottom: theme.spacing(3)
    },
    '& .EveliUserProfile-sectionTitle': {
      display: 'flex',
      marginBottom: theme.spacing(1),
      '& .MuiTypography-root': {
        flexDirection: 'row',
        fontWeight: 'bold',
      },
      '& .MuiSvgIcon-root': {
        color: theme.palette.primary.main,
        marginRight: theme.spacing(1)
      }
    }
  };
});


export const EveliUserOverviewDetail = styled(Box, {
  name: MUI_NAME,
  slot: 'OverviewItem',
  overridesResolver: (_props, styles) => {
    return [
      styles.overviewItem
    ];
  },
})(({ theme }) => {

  return {
    padding: theme.spacing(2),

  };
});

export const StyledNotificationSwitch = styled(Switch, {
  name: MUI_NAME,
  slot: 'NotificationSwitch',
  overridesResolver: (_props, styles) => {
    return [];
  },
})(({ theme }) => {

  return {

    width: 29,
    height: 18,
    padding: 0,
    marginRight: theme.spacing(3),

    '& .MuiSwitch-switchBase': {
      padding: 0,
      margin: 1.4,
      transitionDuration: '300ms',
      '&.Mui-checked': {
        transform: 'translateX(11px)',
        color: '#fff',
        '& + .MuiSwitch-track': {
          opacity: 1,
          border: 0,
        },
        '&.Mui-disabled + .MuiSwitch-track': {
          opacity: 0.5,
        },
      },
      '&.Mui-disabled .MuiSwitch-thumb': {
        color:
          theme.palette.mode === 'light'
            ? theme.palette.grey[100]
            : theme.palette.grey[600],
      },
      '&.Mui-disabled + .MuiSwitch-track': {
        opacity: theme.palette.mode === 'light' ? 0.7 : 0.3,
      },
    },
    '& .MuiSwitch-thumb': {
      boxSizing: 'border-box',
      width: 15.4,
      height: 15.4,
    },
    '& .MuiSwitch-track': {
      borderRadius: 9.1,
      opacity: 1,
      transition: theme.transitions.create(['background-color'], {
        duration: 500,
      }),
    },

  };
});




export const EveliAvatar = styled(Avatar, {
  shouldForwardProp: (prop) => prop !== 'bgColor',
})<{ bgColor: string }>(({ bgColor }) => ({
  marginRight: 8,
  backgroundColor: bgColor,
}));


export const EveliUserAvatar = styled(Box, {
  name: MUI_NAME,
  slot: 'UserAvatar',
  overridesResolver: (_props, styles) => {
    return [
      styles.avatar,
      styles.avatarUserFirstLastName,
      styles.avatarUserName
    ];
  },
})<{}>(({ theme }) => {

  return {
    display: "flex",
    alignItems: "center",
    border: `1px solid ${theme.palette.divider}`,
    minWidth: "25%",
    padding: theme.spacing(1),
    borderRadius: theme.spacing(3),

    '& .EveliUserProfile-avatarUserFirstLastName': {
      ...theme.typography.h6,
      fontWeight: 'bold'
    },
    '& .EveliUserProfile-avatarUserName': {
      ...theme.typography.body1,
      fontWeight: 'bold'
    },
  };
});


export const EveliUserProfileHeader = styled("div", {
  name: MUI_NAME,
  slot: 'Header',
  overridesResolver: (_props) => {
    return [];
  },
})<{ ownerState: PrefsApi.UserProfile }>(({ theme }) => {

  return {
    display: 'flex',
    justifyContent: 'space-between',
    marginBottom: 30,
    '& > .MuiTypography-root ': {
      ...theme.typography.h1
    }
  };
});



