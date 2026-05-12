export const helpArticleLink = `
# Article Link

* An Article Link is a navigable URL entry point that can be associated with one or more Articles.
* It appears in the client portal in the context of the articles it is connected to.
* Each link has a type that determines how it behaves: internal links point to pages within the portal, external links point to pages outside the portal, and phone links contain a telephone number.
* Links support localised labels — each locale can have its own display name for the link.

---

## Associated Assets

- **Article**: A link must be associated with at least one Article to appear on the client portal. It can be connected to multiple articles.

---

## Config Options

- **Development mode**: The link is only visible in development environments. It will not appear in production builds.
- **Disabled mode**: The link is hidden from all users regardless of environment.
`;
