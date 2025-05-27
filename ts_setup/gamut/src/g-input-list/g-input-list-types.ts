import { OverridableStringUnion } from "@mui/types";
import { DialobApi } from "../api-dialob";

// extension hook for adding custom input types
export interface GInputListPropsVariantOverrides { };


export interface GInputListProps {
  id: string;
  value: string;
  datasource: DialobApi.ActionValueSet | undefined;

  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: DialobApi.ControlLabelPosition,
  description: string | undefined;

  errors?: DialobApi.ActionError[] | undefined;
  undefinedValue: string;
  keys?: boolean | undefined; // display keys

  variant: OverridableStringUnion<'list' | 'list-radio', GInputListPropsVariantOverrides> | undefined;
  slots?: Record<OverridableStringUnion<'list' | 'list-radio', GInputListPropsVariantOverrides>, React.ElementType>;

  component?: React.ElementType<GInputListProps>;
}
