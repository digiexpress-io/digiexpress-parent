import { HdesApi } from './api';

const getErrorMsg = (error: any) => {
  if (error.msg) {
    return error.msg;
  }
  if (error.value) {
    return error.value
  }
  if (error.message) {
    return error.message;
  }
}
const getErrorId = (error: any) => {
  if (error.id) {
    return error.id;
  }
  if (error.code) {
    return error.code
  }
  return "";
}
const parseErrors = (props: any[]): HdesApi.ServiceErrorMsg[] => {
  if (!props) {
    return []
  }

  const result: HdesApi.ServiceErrorMsg[] = props.map(error => ({
    id: getErrorId(error),
    value: getErrorMsg(error)
  }));

  return result;
}

export class StoreErrorImpl extends Error {
  private _props: HdesApi.ServiceErrorProps;
  constructor(props: HdesApi.ServiceErrorProps) {
    super(props.text);
    this._props = {
      text: props.text,
      status: props.status,
      errors: parseErrors(props.errors)
    };
  }
  get name() {
    return this._props.text;
  }
  get status() {
    return this._props.status;
  }
  get errors() {
    return this._props.errors;
  }
}