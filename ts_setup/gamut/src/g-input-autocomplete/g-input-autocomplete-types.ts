import { DialobApi } from "../api-dialob";

export interface GInputAutoCompleteProps {
  id: string;
  multiple: boolean;
  disabled: boolean;
  value: string | string[] | undefined;
  datasource: DialobApi.ActionValueSet | undefined;
  onChange: (selectEvent: React.ChangeEvent<any>) => void;
}
