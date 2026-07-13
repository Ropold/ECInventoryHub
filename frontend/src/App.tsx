import { useEffect, useState } from 'react'
import './App.css'
import axios from "axios";
import Navbar from "./components/Navbar.tsx";
import {Route, Routes} from "react-router-dom";
import NotFound from "./components/NotFound.tsx";
import Welcome from "./components/Welcome.tsx";
import ProtectedRoute from "./components/ProtectedRoute.tsx";
import Footer from "./components/Footer.tsx";
import Profile from "./components/Profile.tsx";
import type {UserDetails} from "./components/models/UserModel.ts";

export default function App() {
  const [user, setUser] = useState<string>("anonymousUser");
    const [userDetails, setUserDetails] = useState<UserDetails | null>(null);
  const [language, setLanguage] = useState<string>("de");

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

    function getUserDetails() {
        axios.get("/api/users/me/details")
            .then((response) => {
                setUserDetails(response.data as UserDetails);
            })
            .catch((error) => {
                console.error(error);
                setUserDetails(null);
            });
    }

  useEffect(() => {
    getUser();
  }, []);

    useEffect(() => {
        if(user !== "anonymousUser"){
            getUserDetails();
        }
    }, [user]);

  return (
    <>
      <Navbar user={user} getUser={getUser}/>
      <Routes>
          <Route path="*" element={<NotFound />} />
          <Route path="/" element={<Welcome />}/>
              <Route element={<ProtectedRoute user={user}/>}>
                  <Route path="/profile" element={<Profile user={user} userDetails={userDetails} language={language}/>} />
              </Route>
      </Routes>
      <Footer language={language} setLanguage={setLanguage}/>
    </>
  )
}