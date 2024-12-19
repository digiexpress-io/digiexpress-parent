import { GOverridableComponent } from "../g-override";

export interface ItemProps {
  item: any,
  answerId?: string | null,
  answer?: any

  component?: GOverridableComponent<ItemProps>;
  className?: string | undefined;
}
