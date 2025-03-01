import { en } from "./en";
import { fi } from "./fi";
import { sv } from "./sv";

interface IntlRecord {
  key: string,
  info: string,
  en: string,
  fi: string,
  sv: string
}

class LocaleVisitor {
  private _localeKeysAndValues: Record<string, IntlRecord> = {};

  accept(localizationCode: string, localeKeyAndValue: Record<string, string>): LocaleVisitor {
    Object.entries(localeKeyAndValue).forEach(([key, value]) => this.visitEntry(key, value, localizationCode));
    return this;
  }
  visitEntry(identifier: string, initial: string, localizationCode: string) {
    let translatedValue = initial.replace(/"/g, '\"').replace(/(['"])/g, "\\$1");
    if (translatedValue.indexOf(";") > -1 || translatedValue.indexOf(",") > -1) {
      translatedValue = '"' + translatedValue + '"'
    }


    let prev: IntlRecord | undefined = this._localeKeysAndValues[identifier];

    if (!prev) {
      prev = {
        en: '',
        fi: '',
        info: '',
        sv: '',
        key: identifier
      }
      this._localeKeysAndValues[prev.key] = prev;
    }

    //@ts-ignore
    prev[localizationCode] = translatedValue;


    //@ts-ignore
    if (prev && prev[localizationCode] !== translatedValue) {
      //@ts-ignore
      console.log(`Dup: ${identifier}`, prev[localizationCode]);
      console.log(`Dup: ${identifier}`, translatedValue);
    }
  }
  close() {
    return ("ID,Info,en,fi,sv,ma\r\n") +
      Object.values(this._localeKeysAndValues)
        .map(entry => [entry.key, entry.info, entry.en, entry.fi, entry.sv].join(',')).join('\r\n');
  }
}

  export function parseTs() {
    const visitor = new LocaleVisitor()
    .accept("en", en)
    .accept("fi", fi)
    .accept("sv", sv)

    console.log(visitor.close())
  }

  