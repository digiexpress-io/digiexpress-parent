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
export const parseErrors = (props: any[]): HdesApi.ServiceErrorMsg[] => {
  if (!props) {
    return []
  }

  const result: HdesApi.ServiceErrorMsg[] = props.map(error => ({
    id: getErrorId(error),
    value: getErrorMsg(error)
  }));

  return result;
}
