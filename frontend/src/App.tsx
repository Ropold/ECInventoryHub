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
import Employees from "./components/employee/Employees.tsx";
import EmployeeDetails from "./components/employee/EmployeeDetails.tsx";
import EditEmployee from "./components/employee/EditEmployee.tsx";
import type {EmployeeModel} from "./components/models/EmployeeModel.ts";
import AddNewEmployee from "./components/employee/AddNewEmployee.tsx";
import type {AssignmentModel} from "./components/models/AssignmentModel.ts";
import Assignments from "./components/assignment/Assignments.tsx";
import AddNewAssignment from "./components/assignment/AddNewAssignment.tsx";
import AssignmentDetails from "./components/assignment/AssignmentDetails.tsx";
import EditAssignment from "./components/assignment/EditAssignment.tsx";

export default function App() {
  const [user, setUser] = useState<string>("anonymousUser");
  const [userDetails, setUserDetails] = useState<UserDetails | null>(null);
  const [language, setLanguage] = useState<string>("de");
  const [role, setRole] = useState<string>("VIEWER");

  const [employees, setEmployees] = useState<EmployeeModel[]>([]);
  const [assignments, setAssignments] = useState<AssignmentModel[]>([]);

  function getUser() {
    axios.get("/api/users/me")
        .then((response) => {
          setUser(response.data.toString());
        })
        .catch((error) => {
          console.error(error);
          setUser("anonymousUser");
          setRole("VIEWER");
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

    function getPreferredLanguage() {
        axios.get("/api/users/me/language")
            .then((response) => {
                setLanguage(response.data.toString());
            })
            .catch((error) => {
                console.error(error);
            });
    }

    function getRole() {
        axios.get("/api/users/me/role")
            .then((response) => {
                setRole(response.data.toString());
            })
            .catch((error) => {
                console.error(error);
                setRole("VIEWER");
            });
    }

    function getAllEmployees() {
        axios.get("/api/employees")
            .then((response) => {
                setEmployees(response.data as EmployeeModel[]);
            })
            .catch((error) => {
                console.error("Error fetching employees:", error);
            });
    }

    function getAllAssignments() {
        axios.get("/api/assignments")
            .then((response) => {
                setAssignments(response.data as AssignmentModel[]);
            })
            .catch((error) => {
                console.error("Error fetching assignments:", error);
            });
    }

    function handleNewEmployee(newEmployee: EmployeeModel) {
        setEmployees((prevEmployees) => [...prevEmployees, newEmployee]);
    }

    function handleEmployeeUpdate(updatedEmployee: EmployeeModel) {
        setEmployees((prevEmployees) =>
            prevEmployees.map((employee) => employee.id === updatedEmployee.id ? updatedEmployee : employee)
        );
    }

    function handleEmployeeDelete(deletedEmployeeId: string) {
        setEmployees((prevEmployees) =>
            prevEmployees.filter((employee) => employee.id !== deletedEmployeeId)
        );
    }

    function handleNewAssignment(newAssignment: AssignmentModel) {
        setAssignments((prevAssignments) => [...prevAssignments, newAssignment]);
    }

    function handleAssignmentUpdate(updatedAssignment: AssignmentModel) {
        setAssignments((prevAssignments) =>
            prevAssignments.map((assignment) => assignment.id === updatedAssignment.id ? updatedAssignment : assignment)
        );
    }

    function handleAssignmentDelete(deletedAssignmentId: string) {
        setAssignments((prevAssignments) =>
            prevAssignments.filter((assignment) => assignment.id !== deletedAssignmentId)
        );
    }

  useEffect(() => {
    getUser();
    getAllEmployees();
    getAllAssignments();
  }, []);

    useEffect(() => {
        if(user !== "anonymousUser"){
            getUserDetails();
            getPreferredLanguage();
            getRole();
        }
    }, [user]);

  return (
    <>
      <Navbar user={user} getUser={getUser}/>
      <Routes>
          <Route path="*" element={<NotFound />} />
          <Route path="/" element={<Welcome />}/>
          <Route path="/employees" element={<Employees language={language} role={role} employees={employees}/>} />
          <Route path="/employees/add-new-employee" element={<AddNewEmployee language={language} role={role} handleNewEmployeeSubmit={handleNewEmployee}/>} />
          <Route path="/employees/:id" element={<EmployeeDetails language={language} role={role} handleEmployeeUpdate={handleEmployeeUpdate} handleEmployeeDelete={handleEmployeeDelete}/>} />
          <Route path="/employees/:id/edit" element={<EditEmployee language={language} handleEmployeeUpdate={handleEmployeeUpdate} />} />
          <Route path="/assignments" element={<Assignments language={language} role={role} assignments={assignments}/>} />
          <Route path="/assignments/add-new-assignment" element={<AddNewAssignment language={language} role={role} handleNewAssignmentSubmit={handleNewAssignment}/>} />
          <Route path="/assignments/:id" element={<AssignmentDetails language={language} role={role} handleAssignmentUpdate={handleAssignmentUpdate} handleAssignmentDelete={handleAssignmentDelete}/>} />
          <Route path="/assignments/:id/edit" element={<EditAssignment language={language} handleAssignmentUpdate={handleAssignmentUpdate} />} />
              <Route element={<ProtectedRoute user={user}/>}>
                  <Route path="/profile" element={<Profile user={user} userDetails={userDetails} language={language}/>} />
              </Route>
      </Routes>
      <Footer language={language} setLanguage={setLanguage} user={user}/>
    </>
  )
}