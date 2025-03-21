
# `components-eveli` Customisation Guide

This file contains the customisation mechanism for the components used in the **Eveli-IDE** project. It uses Material-UI's component overriding system, allowing you to customise various components within the application. Specifically, **Eveli-IDE components are fully overrideable**, enabling you to adjust them according to your design requirements.

## Customisation Overview

The file exports a set of **components** wrapped in the `components_eveli` object, which is used to define and customise the appearance of different UI elements. The overriding works by using **variants** to apply specific styles based on the component properties.

### Example: Overriding the Logo

One of the most common customisations is overriding the **logo**. The example below demonstrates how to override the logo in different sizes and themes.

### Step 1: Copy and Modify the Relevant Block

If you want to override the **EveliLogo**, simply copy the relevant block and make adjustments. The logo has different variants that can be customised with different sizes and image sources.

Example for overriding the logo:

```bash
{
  props: { variant: 'black_lg', img: black_log_lg },
  style: { width: '160px', height: '45px' }
}
```

This block sets the logo image for the `black_lg` variant and specifies the width and height.

### Step 2: Use the Correct Logo Variants

There are different logo variants for different display needs, such as the large version. The example provided is for a large black logo.

- **Large Logo (black_lg)**
   - Used for larger spaces.
   - Example:
     ```bash
     {
       props: { variant: 'black_lg', img: black_log_lg },
       style: { width: '160px', height: '45px' }
     }
     ```

### Step 3: Modify the `components_eveli` Object

The `components_eveli` object contains all the components and their variants. You can modify any of the properties and styles as required for your project.

Example of the `EveliLogo` customisation:

```bash
export const components_eveli: Components<Omit<Theme, 'components'>> = {
  EveliLogo: {
    defaultProps: { },
    variants: [
      {
        props: { variant: 'black_lg', img: black_log_lg },
        style: { width: '160px', height: '45px' }
      }
    ]
  }
}
```

# Customising Browser Text and Icon for Eveli-IDE

Customising the browser tab name (title) and favicon in **Eveli-IDE** is simple and works as follows:

## 1. Text Customising (Browser Tab Name)

To customise the browser tab name:

- The initial name for the browser tab is set in the `index.html` file found in the root of the project. It contains the default `<title>` tag, which is shown as the tab name when the application first loads.

Example from `index.html`:

```html
<head>
  <meta charset="utf-8" />
  <base href="">

  <link rel="shortcut icon" type="image/png" role="asset" href="favicon.png" />
  <link rel="manifest" role="asset" href="manifest.json" />
  <link rel="stylesheet" href="/src/styles.css" />
  <title>Eveli IDE</title>
</head>
```

- In this case, the browser tab is initially set to `Eveli IDE`.

However, the tab name will change dynamically based on the value in the **routes** configuration.

### Dynamic Browser Tab Name

The title of the browser tab is dynamically updated based on the internationalization configuration in the project. Here's how it's handled:

1. In `routes/_root.tsx`, the route is configured as follows:

```tsx
function RouteComponent() {
  const intl = useIntl();
  const title = intl.formatMessage({ id: 'document.title' });

  React.useEffect(() => {
    document.title = title; // This dynamically sets the document title.
  }, [title]);

  return <Outlet />
}
```

- The `intl.formatMessage` function is used to get the translated title, based on the language and the `document.title` key in the internationalization files.

- When the page is loaded, the title of the browser tab will automatically be updated based on the translation key's value (`document.title`).

## 2. Icon Customising (Favicon)

To customise the favicon for the Eveli-IDE project:

1. The favicon image is specified in the `index.html` file, using a `<link>` tag with the `rel="shortcut icon"` attribute:

```html
<link rel="shortcut icon" type="image/png" role="asset" href="favicon.png" />
```

- You can replace `favicon.png` with your custom image path if you want to use a different icon. This will be displayed in the browser tab and bookmarks.

2. Additionally, the `manifest.json` file, which is linked in the `index.html`, provides further customisation options for the favicon, especially for different device resolutions:

```html
<link rel="manifest" role="asset" href="manifest.json" />
```

In the `manifest.json` file, you can define various icons with different sizes and resolutions to ensure compatibility across different devices.

Example from `manifest.json`:

```json
{
  "short_name": "Eveli",
  "name": "Eveli",
  "icons": [
    {
      "src": "favicon.png",
      "type": "image/png",
      "sizes": "16x16"
    }
  ],
  "start_url": ".",
  "display": "standalone",
  "theme_color": "#000000",
  "background_color": "#ffffff"
}
```

- Please refer to this link for further info on ['manifest.json'](https://developer.mozilla.org/en-US/docs/Web/Progressive_web_apps/Manifest)


# Eveli-IDE Localization

Localization in **Eveli-IDE** is managed through **CSV files**, which are used to generate localization TypeScript files for different languages (e.g., `localization = en,sv,fi`...etc.).

## Key Points

- **Localization values** are initially provided via CSV files, which are then used to generate the corresponding TypeScript files.
- These localization values **can be overwritten** in your user project to meet specific requirements.
  
  For example, you may need to adjust text strings, error messages, or other UI elements to fit the needs of your project.

## Commonly Overwritten Localization Properties

Some of the most commonly overwritten localization properties include:

### 1. **Login Dialog**
   - **Title**:
     - `login.dialog.title` = **"Unauthenticated User"**
   - **Message**:
     - `login.dialog.message` = **"Descriptive message for unauthenticated user in the dialog"**

These keys can be found in the CSV files and are typically adjusted based on project requirements.
