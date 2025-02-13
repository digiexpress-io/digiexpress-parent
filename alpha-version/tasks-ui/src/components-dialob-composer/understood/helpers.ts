import { Format, Language, Translations, Metadata } from './types';

function mapFromLanguageToKey(translations: Translations) {
  const translationsByKey: Translations = {};

  Object.keys(translations).forEach(language => {
    Object.entries(translations[language]).forEach(([key, value]) => {
      if(!translationsByKey[key]) {
        translationsByKey[key] = {};
      }

      translationsByKey[key][language] = value;
    });
  });

  return translationsByKey;
}

export function parseTranslations(
  translations: Translations,
  format: Format,
): Translations {
  if(format === 'languageToKey') {
    return mapFromLanguageToKey(translations);
  } else if(format === 'keyToLanguage') {
    return translations;
  } else {
    throw new Error('Unexpected translations format!');
  }
}

export function parseAvailableLanguages(translations: Translations, format: Format) {
  if(format === 'languageToKey') {
    return Object.keys(translations);
  }

  const languageSet: Set<string> = new Set();
  Object.values(translations)
    .map(Object.keys)
    .forEach(langs => langs.forEach(lang => languageSet.add(lang)));
  return Array.from(languageSet);
}

export function pickLanguageValues(translations: Translations, shownLanguages: Language[], key: string): [string, string][] {
  if(shownLanguages[0] === '$key') {
    shownLanguages = shownLanguages.slice(1);
  }
  return shownLanguages.map(language => {
    return [language, translations[key][language] || ''];
  });
}

export interface ProblemKeyOpts {
  translations?: Translations;
  languages?: Language[];
  metadata?: Metadata;
}
export function createIsProblemKey(defaultTranslations: Translations, defaultLanguages: Language[], defaultMetadata: Metadata | undefined) {
  return (
    key: string,
    opts: ProblemKeyOpts = {}) => {
    const {
      translations = defaultTranslations,
      languages = defaultLanguages,
      metadata = defaultMetadata,
    } = opts;

    const keyMeta = metadata && metadata.key && metadata.key[key];
    if(keyMeta && keyMeta.needsWork) return true;
    return languages.some(lang => !translations[key][lang]);
  }
}

export function createFilterKeys(
  defaultTranslations: Translations,
  defaultFilterText: string,
  plaintextTranslation: (value: string) => string
) {
  return (opts: {
    translations?: Translations,
    filterText?: string,
  } = {}) => {
    let {
      translations = defaultTranslations,
      filterText = defaultFilterText,
    } = opts;

    if(!filterText) {
      return Object.keys(translations);
    }

    filterText = filterText.toLowerCase();
    return Object.keys(translations).filter(key => {
      return Object.keys(translations[key]).some(lang => {
        const text = translations[key][lang];
        if(!text) return false;
        return plaintextTranslation(text).toLowerCase().includes(filterText);
      });
    });
  }
}
