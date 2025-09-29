import { OverridableStringUnion } from "@mui/types";
import { DialobApi } from "@dxs-ts/gamut-api";

// extension hook for adding custom input types
export interface GInputListPropsVariantOverrides { };


export interface GInputListProps {
  id: string;
  value: string;
  datasource: DialobApi.ActionValueSet | undefined;
  disabled: boolean;
  required: boolean;

  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: DialobApi.ControlLabelPosition,
  description: string | undefined;

  errors?: DialobApi.ActionError[] | undefined;
  undefinedValue: string;
  keys?: boolean | undefined; // display keys

  variant: OverridableStringUnion<'list' | 'list-radio' | 'autocomplete', GInputListPropsVariantOverrides> | undefined;
  slots?: Record<OverridableStringUnion<'list' | 'list-radio' | 'autocomplete', GInputListPropsVariantOverrides>, React.ElementType>;

  component?: React.ElementType<GInputListProps>;
}
