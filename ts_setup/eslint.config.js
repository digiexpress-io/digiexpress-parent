// Import React plugins (you'll need to install these)
import react from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';
import typescript from '@typescript-eslint/eslint-plugin';
import typescriptParser from '@typescript-eslint/parser';


import configs from './.modules/eslint/modules.config.js'


export default [

  {
    ignores: [
      'vite-plugins/*',
      'vite.*',
      'gamut/*',
      'eveli-ide/*'
    ]
  },
  ...configs.map(config => addReactSupport(config))
];





function addReactSupport(existingConfig) {
  
  return {
      ...existingConfig,
      plugins: {
        react,
        'react-hooks': reactHooks,
        '@typescript-eslint': typescript,
      },
      languageOptions: {
        parser: typescriptParser,
        parserOptions: {
          ecmaFeatures: {
            jsx: true,
          },
        },
      },
      settings: {
        react: {
          version: 'detect',
        },
      },
      rules: {
        // Keep your existing rules
        ...existingConfig.rules,
        
        // Add React rules
        ...react.configs.recommended.rules,
        ...reactHooks.configs.recommended.rules,

         ...typescript.configs.recommended.rules,
        
        '@typescript-eslint/no-empty-object-type': 'off',
        '@typescript-eslint/no-unused-vars': 'off',
        'react-hooks/exhaustive-deps': 'off',
        '@typescript-eslint/no-explicit-any': 'off',
        'react-hooks/rules-of-hooks': 'off',
        '@typescript-eslint/no-namespace': 'off',
        '@typescript-eslint/ban-ts-comment': 'off',
        'react/no-unescaped-entities': 'off',
        'react/no-children-prop': 'off',
        '@typescript-eslint/no-unused-expressions': 'off',
        '@typescript-eslint/no-non-null-asserted-optional-chain': 'off',
        'react/display-name': 'off',
        'react/jsx-no-comment-textnodes': 'off',

        // Common React overrides
        'react/react-in-jsx-scope': 'off', // Not needed in React 17+
        'react/prop-types': 'off', // If using TypeScript
      },
    };
}