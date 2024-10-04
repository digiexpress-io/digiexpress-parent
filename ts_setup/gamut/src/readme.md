
//TODO NOTE
```typescript

/**
 *  MUI theme TYPE integration
 */
export interface GLinkHyperClasses { // establish classes
  root: string;
}
export type GLinkHyperClassKey = keyof GLinkHyperClasses; // establish class key for creating list of slots 


export interface GLinkHyperProps {
  label: string;
  value: string;
  onClick?: () => void;
  component?: React.ElementType<GLinkHyperProps>; // 1. Make possible to override with another component completely
}

const useUtilityClasses = (ownerState: GLinkHyperProps) => {
  const slots = { root: ['root'] }; // 2. Connect 'root' slot from GLinkHyperClasses to root slot in theme
  const getUtilityClass = (slot: string) => generateUtilityClass('GLinkHyper', slot); // 3. Create new Class with Base Name + slot name
  return composeClasses(slots, getUtilityClass, {});
}

/**
 * Combines styles with data + material props overrides
 */
export const GLinkHyper: React.FC<GLinkHyperProps> = (initProps) => {
  const theme = useTheme();
  const props = useThemeProps({
    props: initProps,
    name: 'GLinkHyper',
  });
  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }

  return (
    /* <GLinkHyperRoot>
    4. Connect className to slots
    5. Pass ownerState (all props, variants)  
    6. Provide override prop "as" to override with different component
    */
    <GLinkHyperRoot className={classes.root} ownerState={ownerState} as={props.component}>
      <Link href={props.value} target='_blank'>
        <span style={{ wordBreak: 'break-word' }}>
          {props.label}
          <OpenInNewIcon fontSize='small' sx={{ verticalAlign: 'sub', ml: theme.spacing(0.5), color: theme.palette.text.disabled }} />
        </span>
      </Link>
    </GLinkHyperRoot>
  )
}

const GLinkHyperRoot = styled("div", {
  name: 'GLinkHyper',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})<{ ownerState: GLinkHyperProps }>(({ theme }) => {
  return {
  // 3. Generated hash+GLink-root

  backgroundColor: 'pink'

  /* dev console output 
    .css-14w8m2x-GLinkHyper-root {
        background-color: pink;
    }
  */
  };
});

```
3. **Generated hash+GLink-root**


**Connect Class to theme**

```typescript
//g-props.ts

  GLinkHyper?: {
    defaultProps?: GComponentsProps['GLinkHyper'];
    styleOverrides?: GComponentsOverrides<Theme>['GLinkHyper'];
    variants?: GComponentsVariants['GLinkHyper'];
  },
```

```typescript
const GLinkRoot = styled("div", {
  name: 'GLink',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const { ownerState } = props;
    return [
      styles.root, // 1. take the root from Theme.GLinkHyper.styleOverrides.root
      styles[ownerState.variant] // 2. ownerState = all props + styles + variants
    ];
  },
})<{ ownerState: GLinkProps }>(({ theme }) => {
  return {
 // 3. Generated hash+GLink-root
  backgroundColor: 'pink'
  };
});
```

Targeting child G-Classes within a parent G-Class

```typescript
const GLinksRoot = styled("div", {
  name: 'GLinks',
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },
})(({ theme }) => {
  return {
    borderLeft: `3px solid ${theme.palette.primary.main}`,
    marginTop: theme.spacing(2),
    marginLeft: theme.spacing(1),
    paddingLeft: theme.spacing(1),

    '& .GLinkHyper-root': {
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1)
    },
    '& a': {
      fontWeight: theme.typography.fontWeightMedium,
    },
  };
});

```