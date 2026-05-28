import { Fs } from '@dxs-ts/fs-api';

class IntlBuilder {
  private _value: string;
  private _type: Fs.DecisionTypeDef;
  private _intl: Record<string, string>;

  constructor(props: { header: Fs.DecisionTypeDef; value: string }) {
    this._value = props.value;
    this._type = props.header;
    try { this._intl = JSON.parse(props.value || '{}'); }
    catch { this._intl = {}; }
  }

  get header() { return this._type; }
  get value() { return this._value; }

  withLocale(locale: string, value: string): IntlBuilder {
    return new IntlBuilder({ header: this._type, value: JSON.stringify({ ...this._intl, [locale]: value }) });
  }

  getLocaleValue(locale: string): string { return this._intl[locale] ?? ''; }
}

export default IntlBuilder;
