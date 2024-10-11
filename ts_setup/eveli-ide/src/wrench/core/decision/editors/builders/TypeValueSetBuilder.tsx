import {HdesApi} from '../../../client';

class ValueSetBuilder {
  private _values: string[];
  private _header: HdesApi.TypeDef;

  constructor(props: { header: HdesApi.TypeDef, values: string[] }) {
    this._values = props.values;
    this._header = props.header;
  }

  get header() {
    return this._header;
  }
  get values() {
    return this._values;
  }

  withNewValue(value: string) {
    return this._values.concat(value);
  }

  remove(index: number) {
    return this._values.filter((_, i) => i !== index);
  }


}

export default ValueSetBuilder;

