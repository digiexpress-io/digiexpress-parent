import {HdesApi} from '../../../client';

const dataTypes = [
  { key: 'true', value: 'true', text: 'true' },
  { key: 'false', value: 'false', text: 'false' }
];

class BooleanBuilder {
  private _props: { header: HdesApi.TypeDef, value: string };
  private _valid: boolean;
  constructor(props: { header: HdesApi.TypeDef, value: string }) {
    this._props = props;
    this._valid = !props.value || this.validate();
  }

  get value() {
    return this._props.value;
  }
  
  getDataTypes() {
    return dataTypes;
  }
  validate(): boolean {
    let value = this._props.value;
    return value === 'true' || value === 'false';
  }
  get valid(): boolean {
    return this._valid;
  }
}

export default BooleanBuilder
