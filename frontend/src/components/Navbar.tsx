import {useNavigate} from "react-router-dom";
import axios from "axios";
import "./styles/Navbar.css";
import companyLogo from "../assets/ec-logo.png";
import "./styles/Buttons.css";

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
            <button className="button-group-button" onClick={() => navigate("/")}>Home</button>
            {props.user !== "anonymousUser" ? (
                <>
                    <div
                        className="clickable-header padding-left-5"
                        onClick={() => {
                            navigate("/employees");
                        }}
                    >
                        <img src={companyLogo} alt="Company Logo" className="logo-image" />
                        <h2 className="header-title">Employees</h2>
                    </div>
                    <div
                        className="clickable-header padding-left-5"
                        onClick={() => {
                            navigate("/assignments");
                        }}
                    >
                        <img src={companyLogo} alt="Company Logo" className="logo-image" />
                        <h2 className="header-title">Assignments</h2>
                    </div>
                    <div
                        className="clickable-header padding-left-5"
                        onClick={() => {
                            navigate("/devices");
                        }}
                    >
                        <img src={companyLogo} alt="Company Logo" className="logo-image" />
                        <h2 className="header-title">Devices</h2>
                    </div>
                    <button className="button-group-button" onClick={() => navigate("/profile")}>Profile</button>
                    <button className="button-group-button" onClick={logoutFromGithub}>logout</button>
                </>
            ) : (
                <>
                    <div
                        className="clickable-header padding-left-5"
                        onClick={() => {
                            navigate("/employees");
                        }}
                    >
                        <img src={companyLogo} alt="Company Logo" className="logo-image" />
                        <h2 className="header-title">Employees</h2>
                    </div>
                    <div
                        className="clickable-header padding-left-5"
                        onClick={() => {
                            navigate("/assignments");
                        }}
                    >
                        <img src={companyLogo} alt="Company Logo" className="logo-image" />
                        <h2 className="header-title">Assignments</h2>
                    </div>
                    <div
                        className="clickable-header padding-left-5"
                        onClick={() => {
                            navigate("/devices");
                        }}
                    >
                        <img src={companyLogo} alt="Company Logo" className="logo-image" />
                        <h2 className="header-title">Devices</h2>
                    </div>
                    <button className="button-group-button" onClick={loginWithGithub}>Login GitHub</button>
                </>

            )}
        </nav>
    )
}
