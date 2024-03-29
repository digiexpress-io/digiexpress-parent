import * as React from "react";
import { styled } from "@mui/material/styles";
import { Typography, Box, useTheme} from "@mui/material";
import TreeItem, { TreeItemProps, treeItemClasses } from "@mui/lab/TreeItem";
import { SvgIconProps } from "@mui/material/SvgIcon";
import { blueberry_whip, green_teal, sambucus } from "components-colors";


const StyledTreeItemRoot = styled(TreeItem)(({ theme }) => ({
  color: `var(--tree-view-text-color, ${theme.palette.text.secondary})`,
  [`& .${treeItemClasses.content}`]: {
    color: `var(--tree-view-text-color, ${blueberry_whip})`,
    borderTopRightRadius: theme.spacing(2),
    borderBottomRightRadius: theme.spacing(2),
    paddingRight: theme.spacing(1),
    fontWeight: theme.typography.fontWeightMedium,
    "&.Mui-expanded": {
      fontWeight: theme.typography.fontWeightRegular
    },
    "&:hover": {
      backgroundColor: `var(--tree-view-hover-color, ${theme.palette.action.hover})`,
    },
    "&.Mui-focused, &.Mui-selected, &.Mui-selected.Mui-focused": {
      backgroundColor: `var(--tree-view-bg-color, ${theme.palette.action.selected})`,
      color: `var(--tree-view-color, ${green_teal})` //"var(--tree-view-color)"
    },
    [`& .${treeItemClasses.label}`]: {
      //fontWeight: "inherit",
      color: "inherit"
    }
  },
  [`& .${treeItemClasses.group}`]: {
    marginLeft: 0,
    [`& .${treeItemClasses.content}`]: {
      paddingLeft: theme.spacing(2)
    }
  }
}));

type StyledTreeItemProps = TreeItemProps & {
  color?: string;
  bgcolor?: string;
  labelcolor?: string;
  hovercolor?: string;
  textcolor?: string;
  labelIcon?: React.ElementType<SvgIconProps>;
  labelButton?: React.ReactChild;
  labelInfo?: string | React.ReactChild;
  labelText: string | React.ReactChild;
};

const StyledTreeItem: React.FC<StyledTreeItemProps> = (props) => {
  const theme = useTheme();
  const {
    labelButton,
    labelcolor,
    color,
    bgcolor,
    hovercolor,
    textcolor,
    labelIcon: LabelIcon,
    labelInfo,
    labelText,
    ...other
  } = props;

  let resolvedLabelcolor = "inherit";
  if(labelcolor) {
    // @ts-ignore
    resolvedLabelcolor = theme.palette[labelcolor]?.main;
    if(!resolvedLabelcolor && labelcolor.indexOf(".") > -1) {
      const coolors = labelcolor.split(".");
      // @ts-ignore
      resolvedLabelcolor = theme.palette[coolors[0]][coolors[1]];
    }
  }

  return (
    <StyledTreeItemRoot
      sx={{ backgroundColor: sambucus }}
      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
          {labelButton ? labelButton : <Box component={LabelIcon}
            sx={{
              mr: 1,
              color: resolvedLabelcolor,
            }} />}
          <Typography noWrap={true} maxWidth="300px"
            variant="body2"
            sx={{ flexGrow: 1 }}
          >
            {labelText}
          </Typography>
          <Typography variant="caption" color="inherit" >
            {labelInfo}
          </Typography>
        </Box>
      }

      {...other}
      style={{                                                                           // @ts-ignore
        '--tree-view-text-color': textcolor ? theme.palette[textcolor].main : textcolor, // @ts-ignore
        '--tree-view-color': color ? theme.palette[color].main : color,                  // @ts-ignore
        '--tree-view-bg-color': bgcolor ? theme.palette[bgcolor].main : bgcolor,         // @ts-ignore
        '--tree-view-hover-color': hovercolor ? theme.palette[hovercolor].main : hovercolor,
      }}
    />
  );
}

const StyledTreeItemOption: React.FC<{
  labelText: React.ReactNode;
  nodeId: string;
  color: string;
  icon?: React.ElementType<SvgIconProps>;
  onClick: () => void
}> = (props) => {

  return (
    <StyledTreeItemRoot
      onClick={props.onClick}
      nodeId={props.nodeId}

      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.2, pr: 0}} > 
          <Box component={props.icon}  color={props.color} sx={{ pl: 1, mr: 1}} />
          <Typography 
            variant="body2"
            sx={{ fontWeight: "inherit", flexGrow: 1 }}
          >
            {props.labelText}
          </Typography>
        </Box>
      }
    />
  );
}


export type { StyledTreeItemProps };
export { StyledTreeItem, StyledTreeItemRoot, StyledTreeItemOption };