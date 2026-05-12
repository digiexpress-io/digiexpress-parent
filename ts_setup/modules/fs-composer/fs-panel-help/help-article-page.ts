export const helpArticlePage = `
# Article Page

* An Article Page is a container for localised markdown content. It holds the translated text that is displayed on the client portal for its parent Article.
* Each Article Page belongs to exactly one Article and represents a single language version of that article's content.
* An Article Page cannot be moved between articles. To display the same content under a different article, a new page must be created there.

---

## Associated Assets

An Article Page has no directly associated assets. It is a child of an Article and exists solely to hold the markdown content for one locale.

---

## Config Options

- **Development mode**: The page is only visible in development environments. It will not appear in production builds.
`;
