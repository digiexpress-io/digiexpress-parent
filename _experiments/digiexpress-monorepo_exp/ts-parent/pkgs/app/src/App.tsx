import React from 'react';
//import logo from './logo.svg';
//import './App.css';
import { pythagoras } from "./functions";
function main(): void {
    const a = 3;
    const b = 4;
    const result = pythagoras(a, b);
    console.log(`
==============================

Pythagoras Theorm:
${result} = √( (${a})^2  + (${b})^2 )

================================`);
     console.log("WHOOOOT")  
    console.log(pythagoras.toString());
}


  


function App() {
  main();
  console.log("XXXXXXXXXXXXXXXX");
  return (
    <div className="App">
      <header className="App-header">
        <img src={""} className="App-logo" alt="logo" />
        <p>
          Edit <code>src/App.tsx</code>
        </p>
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React
        </a>
      </header>
    </div>
  );
}

export default App;
