// old code

// login = window.location.href = sec.state.login;
// login rep = window.location.href = sec.state.login;


// logout REP =  
// event.preventDefault();
// logout().then(() => {
//  window.location.href = getPortalRoot(config.secured.login);
//});
/*async logOut(citizen: Citizen): Promise<string> {
  let action: string = this._config.logout;
  let method = 'post';
  if (citizen?.representedCompany) {
    method = 'get';
    action = this._config.representativeCompany.logout;
  } else if (citizen?.representedPerson) {
    method = 'get';
    action = this._config.representativePerson.logout;
  }
  return this._store.fetch<string>(action, { method, body: undefined })
    .then(_r => "OK");
}
*/


// logout     = FORM POST <form action={config.secured.logout} method='POST' onSubmit={onSubmit}>{children}</form>


/**
 * REP LOGIN
 * 
  function handleRepresentativePerson() {
    if (config.dev) {
    } else {
      window.location.href = config.representativePerson.login;
    }
  }

  function handleRepresentativeCompany() {
    if (config.dev) {
    } else {
      window.location.href = config.representativeCompany.login;
    }
  }

 */
