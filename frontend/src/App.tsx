import { useEffect, useState } from 'react'
import './App.css'
import axios from "axios";
import Navbar from "./components/Navbar.tsx";
import {Route, Routes} from "react-router-dom";
import NotFound from "./components/NotFound.tsx";
import Welcome from "./components/Welcome.tsx";
import ProtectedRoute from "./components/ProtectedRoute.tsx";
import {DefaultUser, type UserModel} from "./components/models/UserModel.ts";
import Footer from "./components/Footer.tsx";

export default function App() {
  const [user, setUser] = useState<string>("anonymousUser");
  const [userDetails, setUserDetails] = useState<UserModel | null>(DefaultUser);
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
                setUserDetails(response.data as UserModel);
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

              </Route>
      </Routes>
      <Footer language={language}/>
    </>
  )
}