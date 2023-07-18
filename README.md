# git config
git update-index --assume-unchanged tsconfig.json
git update-index --no-assume-unchanged tsconfig.json



# installation
yarn add @the-wrench-io/hdes-ide


# dependencies
```
"@emotion/react": "^11.0.0",
"@emotion/styled": "^11.0.0",
"@material-ui/core": "^5.0.0-alpha.37",
"@material-ui/icons": "^5.0.0-alpha.37",
"@material-ui/styles": "^5.0.0-alpha.35",
"react": "^17.0.1",
"react-dom": "^17.0.1",
"react-intl": "^5.17.2"
```
  

# react integration

## imports
```
import { Resource, Hdes } from '@the-wrench-io/hdes-ide';
```

## backend integration
```
const store: Hdes.Store = {
  fetch<T>(path: string, req?: RequestInit): Promise<T> {
    if (!path) {
      throw new Error("can't fetch with undefined url")
    }

    const defRef: RequestInit = {
      method: "GET",
      credentials: 'same-origin',
      headers: {
        "Content-Type": "application/json;charset=UTF-8"
      }
    };
    
    if(init.csrf) {
      const headers: Record<string, string> = defRef.headers as any;
      headers[init.csrf.key] = init.csrf.value;
    }

    const url = init.url;
    const finalInit: RequestInit = Object.assign(defRef, req ? req : {});


    return fetch(url + path, finalInit)
      .then(response => {
        if (response.status === 302) {
          return null;
        }
        if (!response.ok) {
          return response.json().then(data => {
            console.error(data);
            throw new Hdes.StoreError({
              text: response.statusText,
              status: response.status,
              errors: data
            });
          });
        }
        return response.json();
      })
  }
}
```

## Material-UI, intl, editor integration
```
<StyledEngineProvider injectFirst>
    <ThemeProvider theme={theme}>
      <IntlProvider locale={init.locale} messages={messages[init.locale]}>
        <Resource.Editor store={store} theme='dark' />
      </IntlProvider>
    </ThemeProvider>
  </StyledEngineProvider> 
```
