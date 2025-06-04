import { DialobApi } from "../api-dialob";

export interface GInputAutoCompleteProps {
  id: string;
  multiple: boolean;
  value: string | string[];
  datasource: DialobApi.ActionValueSet | undefined;
  onChange: (selectEvent: React.ChangeEvent<any>) => void;
}
