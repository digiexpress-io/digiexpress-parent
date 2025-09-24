import { HdesApi } from '@dxs-ts/wrench-api';


function validate(builder: IntlBuilder) {
  if (builder.header.direction === 'OUT') {
    return true;
  }
  try {
    JSON.parse(builder.value);
  } catch(error) {
    return false;
  }

  return true;
}

class IntlBuilder {
  private _value: string;
  private _type: HdesApi.TypeDef;
  private _valid: boolean;
  private _intl: Record<string, string>;

  constructor(props: { header: HdesApi.TypeDef, value: string }) {
    this._value = props.value
    this._type = props.header
    this._valid = !props.value || validate(this);
    try {
      this._intl = JSON.parse(!!props.value ? props.value : '{}');
    } catch (error) {
      this._valid = false;
      this._intl = {}
    }
  }

  get valid(): boolean {
    return this._valid;
  }
  get header() {
    return this._type;
  }
  get value() {
    return this._value;
  }
  withLocale(locale: string, value: string): IntlBuilder {
    const result = {...this._intl};
    result[locale] = value;
    return new IntlBuilder({ header: this._type, value: JSON.stringify(result) });
  }

  getLocaleValue(locale: string): string {
    return this._intl[locale] ?? '';
  }
}

export default IntlBuilder;

