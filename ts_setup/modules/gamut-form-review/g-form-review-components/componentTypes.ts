export interface ItemProps {
  item: any,
  answerId?: string | null,
  answer?: any

  component?: React.ElementType<{
    ownerState: ItemProps;
    className: string;
    children?: React.ReactNode | undefined
  }>;
  className?: string | undefined;
}


