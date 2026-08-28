export interface GFormGroupClasses {
  root: string;
}
export type GFormGroupClassKey = keyof GFormGroupClasses;

export interface GFormGroupProps {
  id: string;
  label: string | undefined;
  description: string | undefined;
  children: React.ReactNode;
  columns?: string | undefined; // numerical string

  /**
   - if true, render an accordion, otherwise flat group
   - Set in Composer properties collapsible = true
   */
  collapsible?: boolean | undefined;

  /**
  - Starts from 1 (Page)
  - Indicates how deep the item is nested
   */
  level?: number | undefined,

  /**
  - Styles for parent and child group items, resembling MUI Paper, which include a border, elevation, and padding/margins   
  - For every level of nesting of a group within other groups, it will have additional margins calculated from its level property
  - Set in Composer properties: border = true  
   */
  border?: boolean | undefined;

  disabled: boolean;
  readOnly?: boolean;

  component?: React.ElementType<GFormGroupProps>;
  slots?: {
    label?: GFormGroupSlot | undefined;
    body?: GFormGroupSlot | undefined;
    collapsible?: GFormGroupSlot | undefined;
  };
}

export type GFormGroupSlot = React.ElementType<{ ownerState: GFormGroupProps, className: string, children: React.ReactNode }>;