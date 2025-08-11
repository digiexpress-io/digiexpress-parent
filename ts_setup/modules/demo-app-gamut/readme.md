# `components-g` Customization Guide

This file contains the customization mechanism for the components used in the `gamut` project. It leverages Material-UI's component overriding system, allowing you to customize various components within the application. Specifically, **Gamut components are fully overrideable**, enabling you to adjust them according to your design requirements.

## Customization Overview

The file exports a set of **components** wrapped in the `components_g` object, which is used to define and customize the appearance of different UI elements. The overriding works by using **variants** to apply specific styles based on the component properties.

### Example: Overriding the Logo

One of the most common customizations is overriding the **logo**. The example below demonstrates how to override the logo in different sizes and themes.

### Step 1: Copy and Modify the Relevant Block

If you want to override the **GLogo**, simply copy the relevant block and make adjustments. The logo has different variants that can be customized with different sizes and image sources.

Example for overriding the logo:

```bash
{
  props: { variant: 'black_lg', img: user_logo_light },
  style: { width: '200px', height: '70px' }
}
```

This block sets the logo image for the `black_lg` variant and specifies the width and height.

### Step 2: Use the Correct Logo Variants

There are two types of logos: **lg** (large) and **sm** (small), and they can be adjusted according to your needs. The variants are as follows:

1. **Large Logo (lg)**
   - Used for larger spaces.
   - Example: 
     ```bash
     {
       props: { variant: 'black_lg', img: user_logo_light },
       style: { width: '200px', height: '70px' }
     }
     ```

2. **Small Logo (sm)**
   - Used for smaller spaces.
   - Example:
     ```bash
     {
       props: { variant: 'black_sm', img: user_logo_light },
       style: { width: '150px', height: '50px' }
     }
     ```

3. **Small Logo for Mobile (sm_mob)**
   - Optimized for mobile devices with a smaller size.
   - Example:
     ```bash
     {
       props: { variant: 'black_sm_mob', img: user_logo_light },
       style: { width: '120px', height: '40px' }
     }
     ```


### ⚠️ Important: Logo Dimensions Matter

**Do not use custom logo dimensions.** The layout—especially on mobile—is sensitive to the height of the logo. If a different size is used, elements like the locale selector, login button, or "logged in as" text may become misaligned, clipped, or overly compressed.

Please **always use the provided dimensions** when overriding the logo variants:

```tsx
{
  props: { variant: 'black_lg', img: user_logo_light },
  style: { width: '200px', height: 'auto' }
},
{
  props: { variant: 'black_sm', img: user_logo_light },
  style: { width: '150px', height: 'auto' }
},
{
  props: { variant: 'black_sm_mob', img: user_logo_light },
  style: { width: '120px', height: 'auto' }
}
```

- Ensure `height: 'auto'` is preserved to maintain proportional scaling.
- Avoid specifying arbitrary height or width values that deviate from these defaults.

These values are carefully chosen to ensure proper alignment of the entire AppBar across all screen sizes.


### Step 3: Modify the `components_g` Object

The `components_g` object contains the components and their variants. You can modify any of the properties and styles as required for your project.

Example of the `GLogo` customization:

```bash
export const components_g: Components<Omit<Theme, 'components'>> = {

  GLogo: {
    defaultProps: {
      // default properties if needed
    },
    variants: [
      {
        props: { variant: 'black_lg', img: user_logo_light },
        style: { width: '200px', height: '70px' }
      },
      {
        props: { variant: 'black_sm', img: user_logo_light },
        style: { width: '150px', height: '50px' }
      },
      {
        props: { variant: 'black_sm_mob', img: user_logo_light },
        style: { width: '120px', height: '40px' }
      }
    ]
  },
}
```

### Step 4: Putting the whole material-ui theme together will look something like this

```bash
import React from 'react';

import { StyledEngineProvider } from "@mui/material/styles";
import { ThemeProvider, createTheme, ThemeOptions } from '@mui/material';

import { components_g } from './components-g';
import { components_mui } from './components-mui';
import { palette } from './palette';
import { typography } from './typography';


export const themeOptions: ThemeOptions = { palette, typography, components: { ...components_g, ...components_mui } };
const siteTheme = createTheme(themeOptions);


export const DemoTheme: React.FC<{ children: React.ReactNode }> = ({ children }) => {

  return (
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={siteTheme}>
        {children}
      </ThemeProvider>
    </StyledEngineProvider>);
}
```


# Customising Browser Text and Icon for Gamut

Customising the browser tab name (title) and favicon in **Gamut** is simple and works as follows:

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
  <title>Gamut</title>
</head>
```

- In this case, the browser tab is initially set to `Gamut`.

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

To customise the favicon for the Gamut project:

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
  "short_name": "Gamut",
  "name": "Gamut",
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
