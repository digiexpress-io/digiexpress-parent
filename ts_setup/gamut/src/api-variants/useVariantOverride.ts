import { CSSInterpolation } from '@mui/material';

export function useVariantOverride(props: any, styles: Record<string, CSSInterpolation>) {
  const { ownerState } = props;
  if (!ownerState) {
    return [];
  }

  const overrides: {
    style: CSSInterpolation,
    props: {
      variant: string
    }
  }[] = styles['variant'] as any;

  if (!overrides) {
    return [];
  }

  // overrides from variant array
  const target = overrides
    .filter(({ props }) => props.variant === ownerState.variant)
    .map(item => item.style);

  // overrides from nested structure based on key
  const additional = styles[ownerState.variant];

  return [...(additional ? [additional] : []), ...target]
}