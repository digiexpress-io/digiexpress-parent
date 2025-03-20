
# `components-eveli` Customization Guide

This file contains the customization mechanism for the components used in the **Eveli-IDE** project. It uses Material-UI's component overriding system, allowing you to customize various components within the application. Specifically, **Eveli-IDE components are fully overrideable**, enabling you to adjust them according to your design requirements.

## Customization Overview

The file exports a set of **components** wrapped in the `components_eveli` object, which is used to define and customize the appearance of different UI elements. The overriding works by using **variants** to apply specific styles based on the component properties.

### Example: Overriding the Logo

One of the most common customizations is overriding the **logo**. The example below demonstrates how to override the logo in different sizes and themes.

### Step 1: Copy and Modify the Relevant Block

If you want to override the **EveliLogo**, simply copy the relevant block and make adjustments. The logo has different variants that can be customized with different sizes and image sources.

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

Example of the `EveliLogo` customization:

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