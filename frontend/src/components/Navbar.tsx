import {useNavigate} from "react-router-dom";
import axios from "axios";
import "./styles/Navbar.css";
import companyLogo from "../assets/ec-logo.png";
import assignmentLogo from "../assets/assignment-logo.svg";
import laptopLogo from "../assets/laptop-logo.png";
import employeeLogo from "../assets/employee-logo.png";
import githubLogo from "../assets/github-logo.webp"
import worldLogo from "../assets/world-quartet-logo.jpg";
import userIcon from "../assets/user-icon.png";
import "./styles/Buttons.css";
import "./styles/Popup.css";

type NavbarProps = {
    user:string;
    getUser: () => void;
}

function loginWithGithub() {
    const host = window.location.host === "localhost:5173" ? "http://localhost:8080" : window.location.origin;
    window.open(host + "/oauth2/authorization/github", "_self");
}

export default function Navbar(props: Readonly<NavbarProps>)
{

    const navigate = useNavigate();

    function logoutFromGithub() {
        axios
            .post("/api/users/logout")
            .then(() => {
                props.getUser();
                navigate("/");
            })
            .catch((error) => {
                console.error("Logout failed:", error);
            });
    }

    return (
        <nav className="navbar">
            <div
                className="clickable-header padding-left-5"
                onClick={() => {
                    navigate("/");
                }}
            >
                <img src={companyLogo} alt="Company Logo" className="logo-image" />
                <h2 className="header-title">Home</h2>
            </div>
            {props.user !== "anonymousUser" ? (
                <>
                    <div
                        className="clickable-header padding-left-5"
                        onClick={() => {
                            navigate("/employees");
                        }}
                    >
                        <img src={employeeLogo} alt="Employee Logo" className="logo-image" />
                        <h2 className="header-title">Employees</h2>
                    </div>
                    <div
                        className="clickable-header padding-left-5"
                        onClick={() => {
                            navigate("/assignments");
                        }}
                    >
                        <img src={assignmentLogo} alt="Assignment Logo" className="logo-image" />
                        <h2 className="header-title">Assignments</h2>
                    </div>
                    <div
                        className="clickable-header padding-left-5"
                        onClick={() => {
                            navigate("/devices");
                        }}
                    >
                        <img src={laptopLogo} alt="Laptop Logo" className="logo-image" />
                        <h2 className="header-title">Devices</h2>
                    </div>
                    <div
                        className="clickable-header padding-left-5"
                        onClick={() => {
                            navigate("/locations");
                        }}
                    >
                        <img src={worldLogo} alt="World Logo" className="logo-image" />
                        <h2 className="header-title">Locations</h2>
                    </div>
                    <div
                        className="clickable-header padding-left-5"
                        onClick={() => {
                            navigate("/profile");
                        }}
                    >
                        <img src={userIcon} alt="Profile Icon" className="logo-image" />
                        <h2 className="header-title">Profile</h2>
                    </div>

                    <button
                        className="clickable-header padding-left-5"
                        onClick={logoutFromGithub}
                    >
                        <img src={githubLogo} alt="GitHub Logo" className="logo-image" />
                        <h2 className="header-title">Logout</h2>
                    </button>
                </>
            ) : (
                <>
                    <div
                        className="clickable-header padding-left-5"
                        onClick={() => {
                            navigate("/employees");
                        }}
                    >
                        <img src={employeeLogo} alt="Employee Logo" className="logo-image" />
                        <h2 className="header-title">Employees</h2>
                    </div>
                    <div
                        className="clickable-header padding-left-5"
                        onClick={() => {
                            navigate("/assignments");
                        }}
                    >
                        <img src={assignmentLogo} alt="Assignment Logo" className="logo-image" />
                        <h2 className="header-title">Assignments</h2>
                    </div>
                    <div
                        className="clickable-header padding-left-5"
                        onClick={() => {
                            navigate("/devices");
                        }}
                    >
                        <img src={laptopLogo} alt="Laptop Logo" className="logo-image" />
                        <h2 className="header-title">Devices</h2>
                    </div>
                    <div
                        className="clickable-header padding-left-5"
                        onClick={() => {
                            navigate("/locations");
                        }}
                    >
                        <img src={worldLogo} alt="World Logo" className="logo-image" />
                        <h2 className="header-title">Locations</h2>
                    </div>
                    <button
                        className="clickable-header padding-left-5"
                        onClick={loginWithGithub}
                    >
                        <img src={githubLogo} alt="GitHub Logo" className="logo-image" />
                        <h2 className="header-title">Login GitHub</h2>
                    </button>
                </>

            )}
        </nav>
    )
}
