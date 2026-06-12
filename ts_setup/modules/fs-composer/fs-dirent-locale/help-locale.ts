export const helpLocale = `
# Locale

* A Locale represents a site-wide language. All content — Articles, Pages, Links, Workflows, etc. — uses the declared locales to separate and organise content into different language versions.
* Locales directly determine which languages are available on the client portal. A Locale must be created **before** any content can be created in that language.
* A Locale is identified by a two-letter ISO language code (e.g. **fi** for Finnish, **en** for English). For a full reference list, see [ISO 2-letter language codes](https://www.sitepoint.com/iso-2-letter-language-codes/)

---

## Associated Assets

Locales are referenced across the entire asset tree. Any asset that supports localised content (labels, pages, etc.) uses the declared locales to determine which language versions are available.

The **Locale Overview** tab in the right panel provides a site-wide view of all locales and shows which Articles have pages written in each language.

---

## Config Options

- **Disabled mode**: The locale is deactivated site-wide. Nothing associated with this language will appear in the client portal.
`;
