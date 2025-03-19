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