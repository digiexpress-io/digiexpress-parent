export const helpArticle = `
# Article

* An Article is like a container for content. It represents a section of the client-facing portal that contains Article Pages and their localised content, links, workflows, etc. 
* Articles can be nested inside folders or other articles to form the client portal site hierarchy.
* Articles have a three-digit order number, which determines the order in which they are rendered in the client portal's menu system.

---

## Associated Assets

- **Article Page**: A localized content page belonging to this article. Each language version is a separate page asset. An Article cannot be displayed on the client portal if it has no Article Pages — there would be no content to show.
- **Article**: Articles can be nested inside other articles to create sub-sections.
- **Article Link**: Provides a navigable URL entry point — internal (within the portal), external (outside the portal), or a phone number — that appears in the context of the articles it is connected to. A link can be associated with multiple articles.
- **Article Workflow**: Places a fillable Dialob form on this article's client portal page, allowing you to embed an interactive form — such as a service request or application — directly within a specific section of the site. A workflow can be connected to multiple articles, and a single article can have multiple workflows.

---

## Config Options

- **Development mode**: The article is only visible in development environments. It will not appear in production builds.
`;

