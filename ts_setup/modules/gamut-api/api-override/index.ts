


export type OverrideProps<T> = {
  ownerState: T;
  className: string;
  children?: React.ReactNode | undefined
}

export type GOverridableComponent<T> = React.ElementType<OverrideProps<T>>