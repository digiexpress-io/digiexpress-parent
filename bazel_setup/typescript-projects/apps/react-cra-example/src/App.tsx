import logo from './logo.svg';
import './App.css';
import Super from './super';
import First from '@digiexpress/first';

function App() {
  console.log("pnpm-1-2222fdskjhfsdhfjk-43398576", First);
  return (
    <div className="App">
        <img src={logo} className="App-logo" alt="logo" />
      <header className="App-header">
      

        <Super></Super>
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
