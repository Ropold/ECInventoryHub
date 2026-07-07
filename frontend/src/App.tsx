import { useEffect, useState } from 'react'
import './App.css'
//import {DefaultUser, type UserModel} from "./components/models/UserModel.ts";
import axios from "axios";
import Navbar from "./components/Navbar.tsx";
import {Route, Routes} from "react-router-dom";
import NotFound from "./components/NotFound.tsx";
import Welcome from "./components/Welcome.tsx";
import ProtectedRoute from "./components/ProtectedRoute.tsx";

export default function App() {
  const [user, setUser] = useState<string>("anonymousUser");
  // const [userDetails, setUserDetails] = useState<UserModel | null>(DefaultUser);
  // const [language, setLanguage] = useState<string>("de");

  function getUser() {
    axios.get("/api/users/me")
        .then((response) => {
          setUser(response.data.toString());
        })
        .catch((error) => {
          console.error(error);
          setUser("anonymousUser");
        });
  }

  useEffect(() => {
    getUser();
  }, []);

    useEffect(() => {
        if(user !== "anonymousUser"){
            //getUserDetails();
        }
    }, [user]);

  return (
    <>
      <Navbar user={user} getUser={getUser}/>
      <Routes>
          <Route path="*" element={<NotFound />} />
          <Route path="/" element={<Welcome />}/>
              <Route element={<ProtectedRoute user={user}/>}>

              </Route>
      </Routes>
    </>
  )
}